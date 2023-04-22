package chat.server;

import chat.logging.Logger;
import chat.server.paxos.PaxosEngine;
import chat.server.paxos.PaxosParticipant;
import chat.server.paxos.PaxosProposal;
import chat.server.paxos.PaxosResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.stream.Collectors;

import static chat.server.Operation.OpType.JOIN_GROUP;

public class ChatPeerImpl extends UnicastRemoteObject implements ChatPeer, PaxosParticipant {

    private final InetSocketAddress address;
    private final String displayName;
    private final Map<String, Group> groups;

    public ChatPeerImpl(String displayName, int port) throws RemoteException, MalformedURLException {
        super();

        this.displayName = displayName;
        this.groups = new HashMap<>();  // TODO: Persist this and load on startup
        address = new InetSocketAddress("localhost", port);

        LocateRegistry.createRegistry(port);
        Naming.rebind(String.format("rmi://localhost:%d/DistributedChatPeer", port), this);
        Logger.logMessage(String.format("Replica is up on port %d", port));

        paxosEngine = new PaxosEngine();
    }

    public boolean createGroup(String name) {
        if (groups.containsKey(name)) {
            return false;
        }

        groups.put(name, new Group(name));
        return true;
    }

    @Override
    public List<ChatPeer> joinGroup(String name, ChatPeer peer) throws RemoteException {
        if (!groups.containsKey(name)) {
            throw new IllegalArgumentException("No such group: " + name);
        }

        Group group = groups.get(name);
        PaxosProposal proposal = newProposal(new Operation<>(JOIN_GROUP, name, peer));

        try {
            Result result = paxosEngine.run(proposal, group);
            if (result.success) {
                return group.peers;
            }
        } catch (NotBoundException e) {
            // Just return an empty list
        }

        return new ArrayList<>();
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
        Logger.logMessage("Paxos Prepare: Received proposal");

        // Simulating a failure
        if (Math.random() <= PAXOS_FAILURE_PROBABILITY) {
            Logger.logError("Paxos Prepare: Simulated failure");
            return null;
        }

        if (paxosProposal.id > this.paxosMaxID) {
            // Update max Paxos ID
            this.paxosMaxID = paxosProposal.id;

            if (this.accepted != null) {
                Logger.logMessage("Paxos Prepare: Returning previously ACCEPTED proposal");
                return PaxosResponse.ACCEPTED(this.accepted);
            } else {
                Logger.logMessage("Paxos Prepare: Returning PROMISED for proposal");
                return PaxosResponse.PROMISED(paxosProposal);
            }
        } else {
            Logger.logError("Paxos Prepare: Returning REJECTED for proposal");
            return PaxosResponse.REJECTED(paxosProposal);
        }
    }

    @Override
    public PaxosResponse accept(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logMessage("Paxos Accept: Received proposal for acceptance");

        // Simulating a failure
        if (Math.random() <= PAXOS_FAILURE_PROBABILITY) {
            return null;
        }

        if (paxosProposal.id == this.paxosMaxID) {
            this.accepted = paxosProposal;
            Logger.logMessage("Paxos Accept: Accepting proposal");
            return PaxosResponse.ACCEPTED(paxosProposal);
        } else {
            Logger.logMessage("Paxos Accept: Rejecting proposal");
            return PaxosResponse.REJECTED(paxosProposal);
        }
    }

    @Override
    public PaxosResponse learn(PaxosProposal paxosProposal) throws RemoteException {
        Logger.logMessage("Paxos Learn: Received proposal for learning");

        Result result = this.dispatch(paxosProposal.operation);
        if (result.success) {
            this.accepted = null;
            Logger.logMessage("Paxos Learn: Learned proposal successfully");
            return PaxosResponse.OK(paxosProposal, result);
        } else {
            Logger.logMessage("Paxos Learn: Failed while learning proposal");
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
    /*
        Entry point for replica server.
     */

    public static void main(String[] args) throws IOException {
        // Check if the required command line arguments are provided
        if (args.length < 2) {
            System.err.println("Expected arguments: <self_port> <replica_port_1> <replica_port_2> ... <replica_port_n>");
            System.exit(1);
        }

        // Parse args and get self port and peer ports
        String[] peerPortsStr = Arrays.copyOfRange(args, 1, args.length);
        List<Integer> peerPorts = Arrays.stream(peerPortsStr)
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        int selfPort = Integer.parseInt(args[0]);

        // Create a server instance
        new ChatPeerImpl("Rohit", 8080);
    }
}
