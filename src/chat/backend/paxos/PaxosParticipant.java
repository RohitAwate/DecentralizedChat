package chat.backend.paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface for a participant of the Paxos protocol.
 * These methods may be called by other participants during
 * a run of the protocol.
 */
public interface PaxosParticipant extends Remote {
	/**
	 * Run the "prepare" stage of the protocol.
	 *
	 * @param paxosProposal - proposal that is sent for preparation
	 * @return PROMISED, ACCEPTED or REJECTED
	 */
	PaxosResponse prepare(PaxosProposal paxosProposal) throws RemoteException;

	/**
	 * Run the "accept" stage of the protocol.
	 *
	 * @param paxosProposal - proposal that is sent for acceptance
	 * @return ACCEPTED or REJECTED response
	 */
	PaxosResponse accept(PaxosProposal paxosProposal) throws RemoteException;

	/**
	 * Run the "learn" stage of the protocol.
	 *
	 * @param paxosProposal - proposal that is sent for learning
	 * @return OK or FAILED response
	 */
	PaxosResponse learn(PaxosProposal paxosProposal) throws RemoteException;
}
