package bsds.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This interface defines the methods that a key-value store server should implement to handle requests
 * for getting, putting, and deleting key-value pairs. Clients can call these methods remotely over RMI
 * to interact with the server.
 */
public interface KeyValueStore extends Remote {
    /**
     * Retrieves the value associated with the specified key from the key-value store.
     *
     * @param key the key to retrieve the value for.
     * @return a {@link Result} object representing the result of the operation, including the retrieved value if successful.
     * @throws RemoteException if a remote exception occurs while executing the operation.
     */
    Result get(String key) throws RemoteException;

    /**
     * Puts the specified key-value pair in the key-value store.
     *
     * @param key   the key to store the value under.
     * @param value the value to store.
     * @return a {@link Result} object representing the result of the operation.
     * @throws RemoteException if a remote exception occurs while executing the operation.
     */
    Result put(String key, String value) throws RemoteException;

    /**
     * Deletes the specified key and its associated value from the key-value store.
     *
     * @param key the key to delete.
     * @return a {@link Result} object representing the result of the operation.
     * @throws RemoteException if a remote exception occurs while executing the operation.
     */
    Result delete(String key) throws RemoteException;
}

