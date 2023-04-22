package chat.backend;

import java.util.List;

public interface ChatBackend {
    boolean joinGroup(String ip, int port, String groupName);

    boolean sendMessage(String message, String groupName);

    List<Group> getGroups();

    boolean createGroup(String name);
}
