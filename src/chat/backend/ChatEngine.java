package chat.backend;

import chat.backend.paxos.PaxosEngine;
import chat.backend.paxos.PaxosParticipant;
import chat.backend.paxos.PaxosProposal;
import chat.backend.paxos.PaxosResponse;
import chat.logging.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static chat.backend.Operation.OpType.*;

public class ChatEngine extends UnicastRemoteObject implements ChatPeer, ChatBackend, PaxosParticipant {

    /**
     * Socket address of the peer.
     */
    private final InetSocketAddress address;

    /**
     * Display name of the peer.
     */
    private final String displayName;

    /**
     * Groups that the peer is a part of.
     */
    private final Map<String, Group> groups;

    /**
     * Create a ChatEngine instance for the given display name and port.
     */
    public ChatEngine(String displayName, int port) throws RemoteException, MalformedURLException {
        super();

        // Load the previous state (groups) from disk
        Map<String, Group> tempGroups;
        String fileName = String.format("app_data/%s-%d/groups.dat", displayName, port);
        try (FileInputStream file = new FileInputStream(fileName)) {
            ObjectInputStream stream = new ObjectInputStream(file);
            tempGroups = (Map<String, Group>) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            tempGroups = new HashMap<>();
        }

        this.displayName = displayName;
        this.address = new InetSocketAddress("localhost", port);
        this.groups = tempGroups;

        LocateRegistry.createRegistry(port);
        Naming.rebind(String.format("rmi://localhost:%d/DistributedChatPeer", port), this);
        Logger.logInfo(String.format("Chat engine start on port %s", address));

        this.paxosEngine = new PaxosEngine();

        syncUp();
    }

    private void syncUp() {
        // Creating a copy since the original might get updated
        // as a part of sync up causing concurrent updates.
        Map<String, Group> copy = new HashMap<>(groups);

        for (Group group : copy.values()) {
            for (InetSocketAddress peerAddress : group.peerAddresses) {
                joinGroup(peerAddress.getHostString(), peerAddress.getPort(), group.name);
            }
        }
    }

    @Override
    public Optional<Group> joinGroup(String ip, int port, String groupName) {
        String url = String.format("rmi://%s:%d/DistributedChatPeer", ip, port);
        try {
            ChatPeer peer = (ChatPeer) Naming.lookup(url);
            Group group = peer.acceptJoin(groupName, this);
            if (group == null) {
                return Optional.empty();
            }

            groups.put(groupName, group);
            return Optional.of(group);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            return Optional.empty();
        }
    }

    @Override
    public void shutdown() {
        // Send a log off message to connected peers
        for (Group group : groups.values()) {
            PaxosProposal proposal = new PaxosProposal(new Operation<>(LOG_OFF, group.name, this));

            try {
                paxosEngine.run(proposal, group);
            } catch (NotBoundException | RemoteException e) {
                // Ignore, they're probably offline
            }
        }

        // Save groups to disk for next time
        String fileName = String.format("app_data/%s-%d/groups.dat", displayName, address.getPort());
        try (FileOutputStream file = new FileOutputStream(fileName)) {
            ObjectOutputStream stream = new ObjectOutputStream(file);
            stream.writeObject(groups);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Naming.unbind(String.format("rmi://localhost:%d/DistributedChatPeer", address.getPort()));
            Logger.logInfo(String.format("Chat engine shut down on port %s", address));
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public boolean sendMessage(String contents, Group group) {
        Message message = new Message(this.getDisplayName(), contents, System.currentTimeMillis());
        PaxosProposal proposal = new PaxosProposal(new Operation<>(SEND_MSG, group.name, message));

        try {
            Result<?> result = paxosEngine.run(proposal, group);
            return result.success;
        } catch (NotBoundException | RemoteException e) {
            return false;
        }
    }

    /**
     * Private wrapper class used for file transfers.
     */
    private static class FileTransferHandle implements Serializable {
        final String from;
        final String path;
        final byte[] bytes;

        private FileTransferHandle(String from, String path, byte[] bytes) {
            this.from = from;
            this.path = path;
            this.bytes = bytes;
        }
    }

    @Override
    public boolean sendFile(File file, Group group) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
        FileTransferHandle handle = new FileTransferHandle(displayName, file.getName(), fileBytes);
        PaxosProposal proposal = new PaxosProposal(new Operation<>(SEND_FILE, group.name, handle));

        try {
            Result<?> result = paxosEngine.run(proposal, group);
            return result.success;
        } catch (NotBoundException | RemoteException e) {
            return false;
        }
    }

    @Override
    public List<Group> getGroups() {
        return new ArrayList<>(groups.values());
    }

    @Override
    public boolean createGroup(String name) {
        if (groups.containsKey(name)) {
            return false;
        }

        groups.put(name, new Group(name));
        return true;
    }

    @Override
    public Group acceptJoin(String name, ChatPeer peer) throws RemoteException {
        if (!groups.containsKey(name)) {
            return null;
        }

        Group group = groups.get(name);
        PaxosProposal proposal = newProposal(new Operation<>(JOIN_GROUP, name, peer));

        try {
            Result<?> result = paxosEngine.run(proposal, group);
            if (result.success) {
                // Create a copy of own peers and send to caller
                // Add self to the list
                Group copy = new Group(group);
                copy.peerAddresses.add(this.address);

                // Now add this peer to your own list
                group.peerAddresses.add(peer.getAddress());

                return copy;
            }
        } catch (NotBoundException e) {
            // Just return an empty list
        }

        return null;
    }

    @Override
    public InetSocketAddress getAddress() throws RemoteException {
        return address;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    // Paxos Stuff
    private long paxosMaxID = System.currentTimeMillis();
    private PaxosProposal accepted;

    private final PaxosEngine paxosEngine;

    @Override
    public PaxosResponse prepare(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logInfo("Paxos Prepare: Received proposal");

        if (paxosProposal.id > this.paxosMaxID) {
            // Update max Paxos ID
            this.paxosMaxID = paxosProposal.id;

            if (this.accepted != null) {
                Logger.logInfo("Paxos Prepare: Returning previously ACCEPTED proposal");
                return PaxosResponse.ACCEPTED(this.accepted);
            } else {
                Logger.logInfo("Paxos Prepare: Returning PROMISED for proposal");
                return PaxosResponse.PROMISED(paxosProposal);
            }
        } else {
            Logger.logError("Paxos Prepare: Returning REJECTED for proposal");
            return PaxosResponse.REJECTED(paxosProposal);
        }
    }

    @Override
    public PaxosResponse accept(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logInfo("Paxos Accept: Received proposal for acceptance");

        if (paxosProposal.id == this.paxosMaxID) {
            this.accepted = paxosProposal;
            Logger.logInfo("Paxos Accept: Accepting proposal");
            return PaxosResponse.ACCEPTED(paxosProposal);
        } else {
            Logger.logInfo("Paxos Accept: Rejecting proposal");
            return PaxosResponse.REJECTED(paxosProposal);
        }
    }

    @Override
    public PaxosResponse learn(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logInfo("Paxos Learn: Received proposal for learning");

        Result<?> result = this.dispatch(paxosProposal.operation);
        if (result.success) {
            this.accepted = null;
            Logger.logInfo("Paxos Learn: Learned proposal successfully");
            return PaxosResponse.OK(paxosProposal, result);
        } else {
            Logger.logInfo("Paxos Learn: Failed while learning proposal");
            return PaxosResponse.FAILED(paxosProposal, result);
        }
    }

    /**
     * Helper method that actually runs operations on a peer.
     */
    private Result<?> dispatch(Operation<?> operation) throws RemoteException {
        switch (operation.type) {
            case JOIN_GROUP: {
                ChatPeer peer = (ChatPeer) operation.payload;
                Group group = groups.get(operation.groupName);
                group.peerAddresses.add(peer.getAddress());
                return Result.success("Added new peer to group");
            }
            case SEND_MSG: {
                Group group = groups.get(operation.groupName);
                Message message = (Message) operation.payload;
                group.addMessageToGroupHistory((Message) operation.payload);
                return Result.success(message);
            }
            case LOG_OFF: {
                Group group = groups.get(operation.groupName);
                ChatPeer peer = (ChatPeer) operation.payload;

                group.peerAddresses.removeIf(cp -> {
                    try {
                        return cp.getAddress().equals(peer.getAddress());
                    } catch (RemoteException e) {
                        Logger.logInfo("Could not log off peer.");
                        return false;
                    }
                });

                return Result.success("Logged off successfully!");
            }
            case SEND_FILE: {
                Group group = groups.get(operation.groupName);
                FileTransferHandle handle = (FileTransferHandle) operation.payload;

                String fileName = String.format("app_data/%s-%d/received_files/%s", displayName, address.getPort(), handle.path);
                Path destinationPath = FileSystems.getDefault().getPath(fileName);

                try {
                    Files.createDirectories(destinationPath.getParent());
                    Files.write(destinationPath, handle.bytes);
                    group.addMessageToGroupHistory(
                            new Message(handle.from,
                                    "Sent file: " + handle.path,
                                    System.currentTimeMillis())
                    );
                    return Result.success("File saved successfully!");
                } catch (IOException e) {
                    return Result.failure("File could not be saved");
                }
            }
            default:
                return Result.failure("Unknown operation: " + operation.type);
        }
    }

    /**
     * Creates a new proposal and sets its ID as the latest (max) that
     * the replica has observed.
     *
     * @param operation - operation for which proposal is to be run
     * @return a new proposal
     */
    private PaxosProposal newProposal(Operation<?> operation) {
        PaxosProposal paxosProposal = new PaxosProposal(operation);
        this.paxosMaxID = paxosProposal.id;
        return paxosProposal;
    }
}
