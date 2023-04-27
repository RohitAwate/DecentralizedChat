package chat.backend;

import chat.backend.paxos.PaxosEngine;
import chat.backend.paxos.PaxosParticipant;
import chat.backend.paxos.PaxosProposal;
import chat.backend.paxos.PaxosResponse;
import chat.logging.Logger;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
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
        this.groups = new HashMap<>();  // TODO: Persist this and load on startup
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
    public boolean syncUp(Group group) {
        PaxosProposal proposal = new PaxosProposal(new Operation<>(SYNC_UP, group.name, group));
        try {
            Result<?> result = paxosEngine.run(proposal, group);
            // TODO: Show 
            if (result.success) {
                return true;
            }
        } catch (NotBoundException | RemoteException e) {
            return false;
        }
        return false;
    }

    @Override
    public void shutdown() {
        try {
            Naming.unbind(String.format("rmi://localhost:%d/DistributedChatPeer", address.getPort()));
            Logger.logInfo(String.format("Chat engine shut down on port %s", address));
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean sendMessage(String contents, Group group) {
        Message message = new Message(this.getDisplayName(), contents, System.nanoTime());
        PaxosProposal proposal = new PaxosProposal(new Operation<>(SEND_MSG, group.name, message));

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
    private long paxosMaxID = System.nanoTime();
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
            case SYNC_UP: {
                if (groups.containsKey(operation.groupName)) {
                    return Result.failure("Group not found: " + operation.groupName);
                }

                Group group = groups.get(operation.groupName);
                return Result.success(group.history);
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
