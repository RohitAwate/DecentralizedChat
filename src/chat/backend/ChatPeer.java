package chat.backend;

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ChatPeer extends Remote {
	/**
	 * Accept a new peer into a group.
	 */
	Group acceptJoin(String groupName, ChatPeer peer) throws RemoteException;

	/**
	 * Returns the address of this participant.
	 */
	InetSocketAddress getAddress() throws RemoteException;

	/**
	 * Get the display name of the peer.
	 */
	String getDisplayName() throws RemoteException;
}
