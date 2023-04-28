package chat.backend;

import java.io.Serializable;

/**
 * This class represents the result of an Operation in Paxos.
 */
public class Result<T> implements Serializable {
    /**
     * Whether the operation was successful.
     */
    public final boolean success;

    /**
     * The message associated with the operation result.
     */
    public final T payload;

    /**
     * Creates a new result with the given success status and message.
     *
     * @param success whether the operation was successful
     */
    private Result(boolean success, T payload) {
        this.success = success;
        this.payload = payload;
    }

    /**
     * Creates a new successful result with the given message.
     *
     * @return a new successful result
     */
    public static <T> Result<T> success(T payload) {
        return new Result<>(true, payload);
    }

    /**
     * Creates a new failed result with the given message.
     *
     * @return a new failed result
     */
    public static <T> Result<T> failure(T payload) {
        return new Result<>(false, payload);
    }
}
