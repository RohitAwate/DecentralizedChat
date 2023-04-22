package chat.client;

import chat.server.Result;

import java.rmi.Naming;
import java.rmi.RemoteException;

/**
 * Client implementation that uses Java RMI to
 * connect to the key-value store replica.
 */
public class RMIClient extends Client {

    /**
     * Supported commands.
     */
    private final static String GET = "GET";
    private final static String PUT = "PUT";
    private final static String DELETE = "DELETE";

    private KeyValueStore replica;

    public RMIClient(String host, int port, int timeout) {
        super(host, port, timeout);
    }

    @Override
    void connect() throws Exception {
        String url = String.format("rmi://%s:%s/Replica", host, port);
        this.replica = (KeyValueStore) Naming.lookup(url);
    }

    @Override
    String send(String command) throws RemoteException {
        String[] tokens = command.trim().split("\\s+");

        // Check the first token to see which command it is.
        // Dispatch control to respective handler.
        // Syntax is case-insensitive.
        String op = tokens[0];
        switch (op.toUpperCase()) {
            case GET:
                return this.handleGet(tokens);
            case PUT:
                return this.handlePut(tokens);
            case DELETE:
                return this.handleDelete(tokens);
            default:
                return "Unrecognized command: " + op;
        }
    }

    @Override
    void disconnect() {
    }

    /**
     * Retrieves the value associated with the specified key from the RMI key-value store or
     * returns an error message if not found.
     *
     * @param tokens command tokens
     * @return the value associated with the specified key, or an error message if the key was not found
     * @throws IllegalArgumentException - if command syntax is invalid
     * @throws RemoteException          - if communication fails
     */
    private String handleGet(String[] tokens) throws RemoteException {
        // Error checking for number of tokens
        if (tokens.length != 2) {
            return "Invalid syntax for GET. Usage: GET <key>";
        }

        // Check if key is in store, else return error
        String key = tokens[1];
        Result result = this.replica.get(key);

        return result.message;
    }

    /**
     * Adds a key-value pair to the RMI key-value store or returns an error message
     * if the syntax of the request is invalid.
     *
     * @param tokens command tokens
     * @return a response string
     * @throws IllegalArgumentException - if command syntax is invalid
     * @throws RemoteException          - if communication fails
     */
    private String handlePut(String[] tokens) throws RemoteException {
        // Error checking for number of tokens
        if (tokens.length != 3) {
            return "Invalid syntax for PUT. Usage: PUT <key> <value>";
        }

        String key = tokens[1];
        String value = tokens[2];

        // Add the key-value pair to the store
        Result result = this.replica.put(key, value);
        return result.message;
    }

    /**
     * Deletes the key-value pair associated with the specified key from the RMI key-value
     * store or returns an error message if the key was not found.
     *
     * @param tokens command tokens
     * @return a response string
     * @throws IllegalArgumentException - if command syntax is invalid
     * @throws RemoteException          - if communication fails
     */
    private String handleDelete(String[] tokens) throws RemoteException {
        // Error checking for number of tokens
        if (tokens.length != 2) {
            return "Invalid syntax for DELETE. Usage: DELETE <key>";
        }

        String key = tokens[1];
        Result result = this.replica.delete(key);
        return result.message;
    }
}
