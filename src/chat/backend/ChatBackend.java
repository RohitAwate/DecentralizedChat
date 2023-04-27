package chat.backend;

import java.util.List;
import java.util.Optional;

public interface ChatBackend {
    Optional<Group> joinGroup(String ip, int port, String groupName);

    boolean sendMessage(String message, Group group);

    List<Group> getGroups();

    boolean createGroup(String name);

    boolean syncUp(Group group);

    void shutdown();
}
