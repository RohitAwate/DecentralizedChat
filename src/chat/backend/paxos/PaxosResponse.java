package chat.backend.paxos;

import chat.backend.Result;

import java.io.Serializable;

/**
 * Represents a response from a PaxosParticipant. Contains a status indicating the result of the response,
 * as well as optional data depending on the stage of the response.
 */
public class PaxosResponse implements Serializable {
	/**
	 * The status of the response, indicating the result of the Paxos round.
	 */
	public final Status status;

	// Only available if the response is from the PREPARE or ACCEPT stage
	public PaxosProposal paxosProposal;

	// Only available if the response is from the LEARN stage
	public Result<?> learnResult;

	/**
	 * Constructs a new PaxosResponse with the given status and data.
	 *
	 * @param status        The status of the response.
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @param learnResult   The result of the LEARN stage.
	 */
	private PaxosResponse(Status status, PaxosProposal paxosProposal, Result learnResult) {
		this.status = status;
		this.paxosProposal = paxosProposal;
		this.learnResult = learnResult;
	}

	/**
	 * Constructs a new PaxosResponse with a PROMISED status and the given proposal.
	 *
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @return A new PaxosResponse with PROMISED status.
	 */
	public static PaxosResponse PROMISED(PaxosProposal paxosProposal) {
		return new PaxosResponse(Status.PROMISED, paxosProposal, null);
	}

	/**
	 * Constructs a new PaxosResponse with an ACCEPTED status and the given proposal.
	 *
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @return A new PaxosResponse with ACCEPTED status.
	 */
	public static PaxosResponse ACCEPTED(PaxosProposal paxosProposal) {
		return new PaxosResponse(Status.ACCEPTED, paxosProposal, null);
	}

	/**
	 * Constructs a new PaxosResponse with a REJECTED status and the given proposal.
	 *
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @return A new PaxosResponse with REJECTED status.
	 */
	public static PaxosResponse REJECTED(PaxosProposal paxosProposal) {
		return new PaxosResponse(Status.REJECTED, paxosProposal, null);
	}

	/**
	 * Constructs a new PaxosResponse with an OK status and the given proposal and learn result.
	 *
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @param learnResult   The result of the LEARN stage.
	 * @return A new PaxosResponse with OK status.
	 */
	public static PaxosResponse OK(PaxosProposal paxosProposal, Result learnResult) {
		return new PaxosResponse(Status.OK, paxosProposal, learnResult);
	}

	/**
	 * Constructs a new PaxosResponse with a FAILED status and the given proposal and learn result.
	 *
	 * @param paxosProposal The proposal accepted by the Paxos round.
	 * @param learnResult   The result of the LEARN stage.
	 * @return A new PaxosResponse with FAILED status.
	 */
	public static PaxosResponse FAILED(PaxosProposal paxosProposal, Result learnResult) {
		return new PaxosResponse(Status.FAILED, paxosProposal, learnResult);
	}

	/**
	 * The status of a PaxosResponse, indicating the result of a Paxos round.
	 */
	public enum Status {
		// For PREPARE and ACCEPT stages
		PROMISED, ACCEPTED, REJECTED,

		// For LEARN stage
		OK, FAILED;
	}
}
