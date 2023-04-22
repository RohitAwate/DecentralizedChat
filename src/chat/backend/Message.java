package chat.backend;

public class Message {
    final ChatPeer from;
    final String contents;
    final long timestamp;

    public Message(ChatPeer from, String contents, long timestamp) {
        this.from = from;
        this.contents = contents;
        this.timestamp = timestamp;
    }
}
