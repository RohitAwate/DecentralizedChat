package bsds.server.paxos;

import bsds.logging.Logger;
import bsds.server.Result;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static bsds.server.paxos.PaxosResponse.Status.ACCEPTED;

/**
 * PaxosEngine runs the Paxos protocol on behalf of a replica.
 * It executes all stages of the protocol.
 */
public class PaxosEngine {
    private final ExecutorService executorService;
    private final List<Integer> participantPorts;
    private final List<PaxosParticipant> participants = new ArrayList<>();

    private static final double CONSENSUS_THRESHOLD = 0.5;

    /**
     * Create a PaxosEngine instance that utilizes a thread pool
     * of the given size to dispatch messages to other PaxosParticipants.
     */
    public PaxosEngine(int threads, List<Integer> replicaPorts) {
        this.executorService = Executors.newFixedThreadPool(threads);
        this.participantPorts = replicaPorts;
    }

    /**
     * Represents the various stages in the Paxos protocol.
     */
    private enum PaxosStage {
        PREPARE, ACCEPT, LEARN;
    }

    /**
     * Connects to all the participants in the protocol and runs all the stages of the
     * Paxos protocol for the given proposal. Returns the result which is dependent on whether
     * the proposal received a consensual acceptance or some other situation.
     *
     * @param paxosProposal - proposal to run Paxos for
     * @return - Result of the process
     */
    public Result run(PaxosProposal paxosProposal) throws NotBoundException, RemoteException {
        connectToParticipants();

        PaxosStage[] stages = new PaxosStage[]{PaxosStage.PREPARE, PaxosStage.ACCEPT, PaxosStage.LEARN};

        List<PaxosResponse> responses = null;
        for (PaxosStage stage : stages) {
            responses = this.dispatch(paxosProposal, stage);
            ConsensusResponse response = isConsensus(responses, stage);

            if (response.equals(ConsensusResponse.CONSENSUS_NOT_REACHED)) {
                Logger.logError("Paxos: Consensus not reached during " + stage);
                String msg = "Could not reach Paxos consensus while running your operation.";
                return Result.failure(msg);
            } else if (response.equals(ConsensusResponse.CONSENSUS_PREEMPTED)) {
                Logger.logMessage("Paxos: Consensus Preempted i.e. received ACCEPTED proposal during PREPARE phase");
                paxosProposal.operation = response.acceptedPaxosProposal.operation;
            }
        }

        // Grab a result from a consensus agreeing response to return to client
        return responses.stream()
                .filter(r -> r.status == PaxosResponse.Status.OK)
                .findFirst().get().learnResult;
    }

    /**
     * Represents the possible situations that may be observed
     * while calculating if consensus was reached.
     */
    private enum ConsensusResponse {
        CONSENSUS_REACHED, CONSENSUS_NOT_REACHED,

        /**
         * Consensus was preempted by a proposal with a higher ID.
         * This happens when we send a new proposal to an acceptor that
         * has already accepted another proposal.
         */
        CONSENSUS_PREEMPTED;

        // Only available for CONSENSUS_PREEMPTED
        private PaxosProposal acceptedPaxosProposal;
    }

    /**
     * Determines if a consensus is reached from the collected responses.
     *
     * @param responses - responses from participants
     * @param stage     - stage of the protocol
     * @return ConsensusResponse enum
     */
    private ConsensusResponse isConsensus(List<PaxosResponse> responses, PaxosStage stage) {
        // If stage is PREPARE, we must check for any ACCEPTED responses
        // which indicate that the acceptors already accepted a proposal
        // with a higher ID. In that situation, we pick the proposal
        // with the highest ID out of all the ACCEPTED responses we receive.
        // We then use that proposal for the rest of the proposal.
        // Source: https://people.cs.rutgers.edu/~pxk/417/notes/paxos.html
        if (stage.equals(PaxosStage.PREPARE)) {
            Optional<PaxosResponse> max = responses.stream()
                    .filter(r -> r != null && r.status.equals(ACCEPTED))
                    .max(Comparator.comparing(r -> r.paxosProposal.id));

            if (max.isPresent()) {
                ConsensusResponse response = ConsensusResponse.CONSENSUS_PREEMPTED;
                response.acceptedPaxosProposal = max.get().paxosProposal;
                return response;
            }
        }

        int participantCount = participants.size();

        // The expected status depends on the current stage
        PaxosResponse.Status expectedStatus;
        switch (stage) {
            case PREPARE:
                expectedStatus = PaxosResponse.Status.PROMISED;
                break;
            case ACCEPT:
                expectedStatus = ACCEPTED;
                break;
            case LEARN:
                expectedStatus = PaxosResponse.Status.OK;
                break;
            default:
                throw new IllegalArgumentException("Unknown stage of Paxos: " + stage);
        }

        // Count how many expected statuses were observed
        int positiveCounts = 0;
        for (PaxosResponse response : responses) {
            if (response != null && response.status.equals(expectedStatus)) {
                positiveCounts++;
            }
        }

        double consensusRatio = (double) positiveCounts / (double) participantCount;
        if (consensusRatio >= CONSENSUS_THRESHOLD) {
            Logger.logError(String.format("Paxos %s: Consensus reached (consensus ratio=%.2f)", stage, consensusRatio));
            return ConsensusResponse.CONSENSUS_REACHED;
        }

        Logger.logError(String.format("Paxos %s: Consensus not reached (consensus ratio=%.2f)", stage, consensusRatio));
        return ConsensusResponse.CONSENSUS_NOT_REACHED;
    }

    /**
     * Dispatch the proposal to all participants by making concurrent
     * RMI calls.
     *
     * @param paxosProposal - proposal used in the protocol
     * @param stage         - stage currently being executed
     * @return responses from the participants
     */
    private List<PaxosResponse> dispatch(PaxosProposal paxosProposal, PaxosStage stage) {
        CompletionService<PaxosResponse> service = new ExecutorCompletionService<>(executorService);

        for (PaxosParticipant participant : participants) {
            switch (stage) {
                case PREPARE:
                    service.submit(() -> participant.prepare(paxosProposal));
                    break;
                case ACCEPT:
                    service.submit(() -> participant.accept(paxosProposal));
                    break;
                case LEARN:
                    service.submit(() -> participant.learn(paxosProposal));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown stage of Paxos: " + stage);
            }
        }

        List<PaxosResponse> responses = new ArrayList<>();
        for (PaxosParticipant participant : participants) {
            try {
                Future<PaxosResponse> future = service.take();
                responses.add(future.get(1, TimeUnit.SECONDS));
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Logger.logError(String.format("Paxos %s: Participant timed out", stage));
            }
        }

        return responses;
    }

    /**
     * Connects to the {@code PaxosParticipant}s and returns the list of connected participants.
     *
     * @throws NotBoundException if a remote object could not be found with the specified name
     * @throws RemoteException   if a communication-related exception occurs while communicating with a remote object
     */
    private void connectToParticipants() throws NotBoundException, RemoteException {
        // Check if already connected previously
        if (participantPorts.size() == this.participants.size()) {
            return;
        }

        for (int port : participantPorts) {
            try {
                String url = String.format("rmi://localhost:%d/Replica", port);
                PaxosParticipant participant = (PaxosParticipant) Naming.lookup(url);
                this.participants.add(participant);
            } catch (MalformedURLException e) {
                Logger.logError(e.getMessage());
            }
        }

    }
}
