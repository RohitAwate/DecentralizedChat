package chat.backend;

import java.io.Serializable;

public class Message implements Serializable {
	final String from;
	final String contents;

	final long timestamp;

	public Message(String from, String contents, long timestamp) {
		this.from = from;
		this.contents = contents;
		this.timestamp = timestamp;
	}

	public String getFrom() {
		return from;
	}

	public String getContents() {
		return contents;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
