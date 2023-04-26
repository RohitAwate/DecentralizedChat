package chat.backend;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {

    public final String name;
    public final List<ChatPeer> peers;
    public final List<Message> history;

    public Group(String name) {
        this.name = name;
        this.peers = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public Group(Group other) {
        this.name = other.name;
        this.peers = new ArrayList<>(other.peers);
        this.history = new ArrayList<>(other.history);
    }

    public String getName() {
        return name;
    }

    public List<ChatPeer> getPeers() {
        return peers;
    }

    public List<Message> getHistory() {
        return history;
    }

    public void addMessageToGroupHistory(Message message) {
        history.add(message);
    }

    public List<Message> getMessageHistory() {
        return history;
    }

    @Override
    public String toString() {
        return name;
    }
}
