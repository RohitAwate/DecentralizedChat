package chat.server;

import java.net.InetSocketAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ChatPeer extends Remote {
    List<ChatPeer> joinGroup(String name, ChatPeer peer) throws RemoteException;

    /**
     * Returns the address of this participant.
     *
     * @return the address of this participant
     * @throws RemoteException if there is a remote error
     */
    InetSocketAddress getAddress() throws RemoteException;

    String getDisplayName();
}
