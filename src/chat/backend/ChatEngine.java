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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static chat.backend.Operation.OpType.JOIN_GROUP;

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
    public boolean joinGroup(String ip, int port, String groupName) {
        String url = String.format("rmi://%s:%d/DistributedChatPeer", ip, port);
        try {
            ChatPeer peer = (ChatPeer) Naming.lookup(url);
            Group group = peer.acceptJoin(groupName, this);
            if (group == null) {
                return false;
            }

            groups.put(groupName, group);
            return true;
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            return false;
        }
    }

    @Override
    public boolean sendMessage(String message, String groupName) {
        return true;
    }

    @Override
    public List<Group> getGroups() {
        return null;
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
            throw new IllegalArgumentException("No such group: " + name);
        }

        Group group = groups.get(name);
        PaxosProposal proposal = newProposal(new Operation<>(JOIN_GROUP, name, peer));

        try {
            Result result = paxosEngine.run(proposal, group);
            if (result.success) {
                return group;
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
    private final double PAXOS_FAILURE_PROBABILITY = 0.1;  // Replica fails 10% of the times

    private final PaxosEngine paxosEngine;

    @Override
    public PaxosResponse prepare(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logInfo("Paxos Prepare: Received proposal");

        // Simulating a failure
        if (Math.random() <= PAXOS_FAILURE_PROBABILITY) {
            Logger.logError("Paxos Prepare: Simulated failure");
            return null;
        }

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

        // Simulating a failure
        if (Math.random() <= PAXOS_FAILURE_PROBABILITY) {
            return null;
        }

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

        Result result = this.dispatch(paxosProposal.operation);
        if (result.success) {
            this.accepted = null;
            Logger.logInfo("Paxos Learn: Learned proposal successfully");
            return PaxosResponse.OK(paxosProposal, result);
        } else {
            Logger.logInfo("Paxos Learn: Failed while learning proposal");
            return PaxosResponse.FAILED(paxosProposal, result);
        }
    }

    private Result dispatch(Operation<?> operation) {
        switch (operation.type) {
            case JOIN_GROUP:
                ChatPeer peer = (ChatPeer) operation.payload;
                addToGroup(peer, operation.groupName);
                return Result.success("Added new peer to group");
            case SEND_MSG:
            case SYNC_UP:
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
