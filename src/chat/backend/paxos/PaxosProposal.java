package chat.backend.paxos;

import chat.backend.Operation;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a proposal in the Paxos algorithm.
 */
public class PaxosProposal implements Serializable {
	/**
	 * Unique identifier for the proposal.
	 */
	public final long id;

	/**
	 * The operation to be proposed.
	 */
	public Operation<?> operation;

	/**
	 * Creates a new PaxosProposal object with a unique identifier and the given operation.
	 *
	 * @param operation the operation to be proposed
	 */
	public PaxosProposal(Operation<?> operation) {
		this.id = System.currentTimeMillis();
		this.operation = operation;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PaxosProposal paxosProposal = (PaxosProposal) o;
		return id == paxosProposal.id && operation.equals(paxosProposal.operation);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, operation);
	}
}
