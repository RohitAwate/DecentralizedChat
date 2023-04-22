package chat.backend;

import java.util.ArrayList;
import java.util.List;

public class Group {
    public final String name;
    public final List<ChatPeer> peers;
    public final List<Message> history;

    public Group(String name) {
        this.name = name;
        this.peers = new ArrayList<>();
        this.history = new ArrayList<>();
    }
}
