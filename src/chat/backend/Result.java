package chat.backend;

import java.io.Serializable;

/**
 * This class represents the result of an operation on a key-value store.
 */
public class Result implements Serializable {
	/**
	 * Whether the operation was successful.
	 */
	public final boolean success;

	/**
	 * The message associated with the operation result.
	 */
	public final String message;

	/**
	 * Creates a new result with the given success status and message.
	 *
	 * @param success whether the operation was successful
	 * @param message the message associated with the operation result
	 */
	private Result(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	/**
	 * Creates a new successful result with the given message.
	 *
	 * @param message the message associated with the successful result
	 * @return a new successful result
	 */
	public static Result success(String message) {
		return new Result(true, message);
	}

	/**
	 * Creates a new failed result with the given message.
	 *
	 * @param message the message associated with the failed result
	 * @return a new failed result
	 */
	public static Result failure(String message) {
		return new Result(false, message);
	}
}
