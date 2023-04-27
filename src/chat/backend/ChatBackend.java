package chat.backend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ChatBackend {
    Optional<Group> joinGroup(String ip, int port, String groupName);

    boolean sendMessage(String message, Group group);

    boolean sendFile(File file, Group group) throws IOException;

    List<Group> getGroups();

    Optional<Group> syncUp(String groupName);

    boolean createGroup(String name);

    void shutdown();
}
