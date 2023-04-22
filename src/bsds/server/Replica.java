package bsds.server;

import bsds.logging.Logger;
import bsds.server.paxos.PaxosEngine;
import bsds.server.paxos.PaxosParticipant;
import bsds.server.paxos.PaxosProposal;
import bsds.server.paxos.PaxosResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * A key-value store replica in a cluster that runs the Paxos consensus algorithm to
 * maintain consistency of data.
 */
public class Replica extends UnicastRemoteObject implements KeyValueStore, PaxosParticipant {

    /**
     * The ConcurrentHashMap that stores the key-value pairs.
     */
    private final Map<String, String> map = new ConcurrentHashMap<>();

    /**
     * Constructs a new RMIKeyValueServer instance and binds it to the specified port.
     *
     * @param port the port to bind the server to
     * @throws RemoteException       if a remote error occurs
     * @throws MalformedURLException if the URL for the server is malformed
     */
    public Replica(int port, List<Integer> peerPorts) throws RemoteException, MalformedURLException {
        super();

        LocateRegistry.createRegistry(port);
        Naming.rebind(String.format("rmi://localhost:%d/Replica", port), this);
        Logger.logMessage(String.format("Replica is up on port %d", port));

        paxosEngine = new PaxosEngine(peerPorts.size(), peerPorts);
    }

    @Override
    public Result get(String key) throws RemoteException {
        Logger.logOperation(Operation.GET(key));

        if (map.containsKey(key)) {
            String message = map.get(key);
            Logger.logMessage(message);
            return Result.success(message);
        } else {
            String message = "Key not found: " + key;
            Logger.logMessage(message);
            return Result.failure(message);
        }
    }

    @Override
    public Result put(String key, String value) throws RemoteException {
        try {
            PaxosProposal paxosProposal = this.newProposal(Operation.PUT(key, value));
            Result result = paxosEngine.run(paxosProposal);
            if (result.success) {
                Logger.logMessage("Learning from proposal");
                this.dispatch(paxosProposal.operation);
            }

            return result;
        } catch (NotBoundException e) {
            String msg = "Something went wrong while running your transaction.";
            return Result.failure(msg);
        }
    }

    // Helper method for running the PUT operation
    private Result handlePut(String key, String value) throws RemoteException {
        String message;

        if (map.containsKey(key)) {
            message = String.format("Key updated: (%s: %s)", key, value);
        } else {
            message = String.format("Inserted: (%s: %s)", key, value);
        }

        map.put(key, value);
        Logger.logMessage(message);
        return Result.success(message);
    }

    @Override
    public Result delete(String key) throws RemoteException {
        try {
            PaxosProposal paxosProposal = this.newProposal(Operation.DELETE(key));
            Result result = paxosEngine.run(paxosProposal);
            if (result.success) {
                Logger.logMessage("Learning from proposal");
                this.dispatch(paxosProposal.operation);
            }

            return result;
        } catch (NotBoundException e) {
            String msg = "Something went wrong while running your transaction.";
            return Result.failure(msg);
        }
    }

    // Helper method for running the DELETE operation
    private Result handleDelete(String key) throws RemoteException {
        String value = map.remove(key);

        if (value != null) {
            String message = String.format("Key deleted: (%s: %s)", key, value);
            Logger.logMessage(message);
            return Result.success(message);
        } else {
            String message = String.format("Key not found: %s", key);
            Logger.logMessage(message);
            return Result.failure(message);
        }
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

    /**
     * Runs a given operation on the replica resulting in potential
     * updates to the underlying data structure.
     */
    private Result dispatch(Operation op) throws RemoteException {
        Logger.logOperation(op);

        switch (op.type) {
            case GET:
                // This should never happen, since GET is handled locally. But just in case!
                return get(op.args[0]);
            case PUT:
                return handlePut(op.args[0], op.args[1]);
            case DELETE:
                return handleDelete(op.args[0]);
            default:
                return Result.failure("Unrecognized operation: " + op.type);
        }
    }

    /**
     * Creates a new proposal and sets its ID as the latest (max) that
     * the replica has observed.
     *
     * @param operation - operation for which proposal is to be run
     * @return a new proposal
     */
    private PaxosProposal newProposal(Operation operation) {
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
        new Replica(selfPort, peerPorts);
    }
}
