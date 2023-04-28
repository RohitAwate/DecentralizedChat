package chat.backend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * This interface represents a backend for a distributed P2P chat application.
 */
public interface ChatBackend {

    /**
     * Joins a group given its name and the IP address and port of one of the peers.
     *
     * @param ip the IP address of peer.
     * @param port the port of the peer.
     * @param groupName the name of the group.
     * @return an optional Group object representing the group if the join was successful, otherwise empty.
     */
    Optional<Group> joinGroup(String ip, int port, String groupName);

    /**
     * Sends a message to a group.
     *
     * @param message the message to send.
     * @param group the group to send the message to.
     * @return true if the message was sent successfully, false otherwise.
     */
    boolean sendMessage(String message, Group group);

    /**
     * Sends a file to a group.
     *
     * @param file the file to send.
     * @param group the group to send the file to.
     * @return true if the file was sent successfully, false otherwise.
     * @throws IOException if there was an error reading or sending the file.
     */
    boolean sendFile(File file, Group group) throws IOException;

    /**
     * Returns a list of all the connected groups.
     *
     * @return a list of Group objects representing the connected groups.
     */
    List<Group> getGroups();

    /**
     * Creates a new group with the given name.
     *
     * @param name the name of the group to create.
     * @return true if the group was created successfully, false otherwise.
     */
    boolean createGroup(String name);

    /**
     * Shuts down the chat backend and frees any resources used.
     */
    void shutdown();

    /**
     * Returns the display name of the peer.
     */
    String getDisplayName();
}

