package chat.backend;

import java.io.Serializable;

/**
 * This class represents a message in the chat application.
 */
public class Message implements Serializable {

	/**
	 * The sender of the message.
	 */
	final String from;

	/**
	 * The contents of the message.
	 */
	final String contents;

	/**
	 * The timestamp of the message.
	 */
	final long timestamp;

	/**
	 * Constructs a new Message object with the given sender, contents, and timestamp.
	 *
	 * @param from the sender of the message.
	 * @param contents the contents of the message.
	 * @param timestamp the timestamp of the message.
	 */
	public Message(String from, String contents, long timestamp) {
		this.from = from;
		this.contents = contents;
		this.timestamp = timestamp;
	}

	/**
	 * Returns the sender of the message.
	 *
	 * @return the sender of the message.
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * Returns the contents of the message.
	 *
	 * @return the contents of the message.
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Returns the timestamp of the message.
	 *
	 * @return the timestamp of the message.
	 */
	public long getTimestamp() {
		return timestamp;
	}
}

