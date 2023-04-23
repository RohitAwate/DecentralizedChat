package chat.backend;

import chat.backend.paxos.PaxosParticipant;
import chat.backend.paxos.PaxosProposal;
import chat.backend.paxos.PaxosResponse;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class MockChatEngine extends UnicastRemoteObject implements ChatPeer, ChatBackend, PaxosParticipant {

	private final String displayName;

	public MockChatEngine(String displayName, int port) throws MalformedURLException, IllegalArgumentException, RemoteException {
		super();
		this.displayName = displayName;
	}

	@Override
	public boolean joinGroup(String ip, int port, String groupName) {
		return false;
	}

	@Override
	public boolean sendMessage(String message, String groupName) {
		return false;
	}

	@Override
	public List<Group> getGroups() {
		return null;
	}

	@Override
	public boolean createGroup(String name) {
		return false;
	}

	@Override
	public Group acceptJoin(String name, ChatPeer peer) throws RemoteException {
		return null;
	}

	@Override
	public InetSocketAddress getAddress() throws RemoteException {
		return null;
	}

	@Override
	public String getDisplayName() throws RemoteException {
		return displayName;
	}

	@Override
	public PaxosResponse prepare(PaxosProposal paxosProposal) throws RemoteException {
		return null;
	}

	@Override
	public PaxosResponse accept(PaxosProposal paxosProposal) throws RemoteException {
		return null;
	}

	@Override
	public PaxosResponse learn(PaxosProposal paxosProposal) throws RemoteException {
		return null;
	}
}
