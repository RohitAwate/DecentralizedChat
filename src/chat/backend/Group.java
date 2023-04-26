package chat.backend;

import java.util.ArrayList;
import java.util.List;

public class Group {

<<<<<<< Updated upstream
    public Group(String name) {
        this.name = name;
        this.peers = new ArrayList<>();
        this.history = new ArrayList<>();
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
=======
	public final String name;
	public final List<ChatPeer> peers;
	public final List<Message> history;

	public Group(String name) {
		this.name = name;
		this.peers = new ArrayList<>();
		this.history = new ArrayList<>();
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
>>>>>>> Stashed changes
}
