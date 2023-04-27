package chat.backend;

import chat.backend.paxos.PaxosEngine;
import chat.backend.paxos.PaxosParticipant;
import chat.backend.paxos.PaxosProposal;
import chat.backend.paxos.PaxosResponse;
import chat.logging.Logger;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static chat.backend.Operation.OpType.*;

public class ChatEngine extends UnicastRemoteObject implements ChatPeer, ChatBackend, PaxosParticipant {

    private final InetSocketAddress address;
    private final String displayName;
    private final Map<String, Group> groups;

    public ChatEngine(String displayName, int port) throws RemoteException, MalformedURLException {
        super();

        this.displayName = displayName;

        Map<String, Group> tempGroups;
        try (FileInputStream file = new FileInputStream(String.format("%s-%s.dat", displayName, port))) {
            ObjectInputStream stream = new ObjectInputStream(file);
            tempGroups = (Map<String, Group>) stream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            tempGroups = new HashMap<>();
        }

        this.groups = tempGroups;
        address = new InetSocketAddress("localhost", port);

        LocateRegistry.createRegistry(port);
        Naming.rebind(String.format("rmi://localhost:%d/DistributedChatPeer", port), this);
        Logger.logInfo(String.format("Chat engine start on port %s", address));

        paxosEngine = new PaxosEngine();
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
        for (Group group : groups.values()) {
            PaxosProposal proposal = new PaxosProposal(new Operation<>(LOG_OFF, group.name, this));

            try {
                paxosEngine.run(proposal, group);
            } catch (NotBoundException | RemoteException e) {
                return;
            }
        }

        try (FileOutputStream file = new FileOutputStream(String.format("%s-%s.dat", displayName, address.getPort()))) {
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

    private static class FileTransferHandle implements Serializable {
        final String from;
        final String path;
        final byte[] bytes;

        public FileTransferHandle(String from, String path, byte[] bytes) {
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
                copy.peers.add(this);

                // Now add this peer to your own list
                group.peers.add(peer);

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

    private Result<?> dispatch(Operation<?> operation) {
        switch (operation.type) {
            case JOIN_GROUP: {
                ChatPeer peer = (ChatPeer) operation.payload;
                addToGroup(peer, operation.groupName);
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

                group.peers.removeIf(cp -> {
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
                try {
                    Path destinationPath = FileSystems.getDefault().getPath(handle.path);
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

    private void addToGroup(ChatPeer peer, String groupName) {
        Group group = groups.get(groupName);
        group.peers.add(peer);
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
