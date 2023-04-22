package bsds.client;

import java.io.IOException;

/**
 * Abstract class for a key-value store client that provides
 * APIs to connect, disconnect and issue commands to the server replica.
 */
abstract class Client {
    /**
     * Hostname or IP address of the server replica.
     */
    protected final String host;

    /**
     * Port number of the server replica.
     */
    protected final int port;

    /**
     * The number of milliseconds to wait for the server replica to respond before
     * timing out.
     */
    protected int timeout;

    /**
     * Create an instance of a client.
     *
     * @param host    - server replica hostname or IP address
     * @param port    - the port number of the server replica
     * @param timeout - milliseconds to wait for server replica to respond before timing out
     */
    Client(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    /**
     * Connect to the server replica.
     */
    abstract void connect() throws Exception;

    /**
     * Send a query command to the server replica.
     *
     * @return the string response
     * @throws IOException - if something goes wrong during this interaction
     */
    abstract String send(String command) throws IOException;

    /**
     * Disconnect from the server replica.
     */
    abstract void disconnect() throws IOException;
}
