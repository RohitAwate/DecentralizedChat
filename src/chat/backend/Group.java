package chat.backend;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represents a group chat in the application.
 */
public class Group implements Serializable {

    /**
     * The name of the group.
     */
    public final String name;

    /**
     * The list of peer addresses in the group.
     */
    public final Set<InetSocketAddress> peerAddresses;

    /**
     * The list of messages in the group's chat history.
     */
    public final List<Message> history;

    /**
     * Constructs a new Group object with the given name.
     *
     * @param name the name of the group.
     */
    public Group(String name) {
        this.name = name;
        this.peerAddresses = new HashSet<>();
        this.history = new ArrayList<>();
    }

    /**
     * Constructs a new Group object from an existing one.
     *
     * @param other the existing Group object to copy.
     */
    public Group(Group other) {
        this.name = other.name;
        this.peerAddresses = new HashSet<>(other.peerAddresses);
        this.history = new ArrayList<>(other.history);
    }

    /**
     * Returns the list of messages in the group's chat history.
     *
     * @return the list of messages in the group's chat history.
     */
    public List<Message> getHistory() {
        return history;
    }

    /**
     * Adds a message to the group's chat history.
     *
     * @param message the message to add to the group's chat history.
     */
    public void addMessageToGroupHistory(Message message) {
        if (history.size() > 1) {
            Message lastMessage = history.get(history.size() - 1);
            if (message.contents.equals(lastMessage.contents) && message.from.equals(lastMessage.from)) {
                return;
            }
        }

        history.add(message);
    }

    /**
     * Returns the name of the group as a string.
     *
     * @return the name of the group as a string.
     */
    @Override
    public String toString() {
        return name;
    }
}

