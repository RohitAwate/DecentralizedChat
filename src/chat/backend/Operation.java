package chat.backend;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the operation that is to be performed as a result of Paxos.
 * @param <T> type of the payload argument
 */
public class Operation<T> implements Serializable {
	/**
	 * The type of this operation.
	 */
	public final OpType type;

	/**
	 * The name of the group that this operation targets.
	 */
	public final String groupName;

	/**
	 * Payload argument required for operation.
	 */
	public final T payload;

	/**
	 * Creates a new operation with the given type and arguments.
	 *
	 * @param args      the arguments of the operation
	 * @param type      the type of the operation
	 * @param groupName
	 */
	public Operation(OpType type, String groupName, T payload) {
		this.type = type;
		this.groupName = groupName;
		this.payload = payload;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Operation<?> operation = (Operation<?>) o;
		return type == operation.type && Objects.equals(groupName, operation.groupName) && Objects.equals(payload, operation.payload);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, groupName, payload);
	}

	/**
	 * The type of the operation.
	 */
	public enum OpType {
		JOIN_GROUP, SEND_MSG, SEND_FILE, LOG_OFF
	}
}
