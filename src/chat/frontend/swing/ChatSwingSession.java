package chat.frontend.swing;

import chat.backend.ChatBackend;
import chat.backend.Group;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Represents a user session for the P2P chat application.
 */
public class ChatSwingSession {

    @Nullable
    private ChatBackend backend;
    @Nullable
    private Group currentlyActiveGroup;

    /**
     * Checks if the user is logged in to the chat engine.
     *
     * @return true if the user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return backend != null;
    }

    /**
     * Returns a list of all the groups available in the chat engine.
     *
     * @return a list of groups, or null if the user is not logged in
     */
    @Nullable
    public List<Group> getGroups() {
        return backend.getGroups();
    }

    /**
     * Checks if the user is currently in an active group.
     *
     * @return true if the user is in an active group, false otherwise
     */
    public boolean ifAnyGroupActive() {
        return currentlyActiveGroup != null;
    }

    /**
     * Returns the chat engine instance associated with this user session.
     *
     * @return the chat engine, or null if the user is not logged in
     */
    @Nullable
    public ChatBackend getBackend() {
        return backend;
    }

    /**
     * Sets the chat engine instance associated with this user session.
     *
     * @param backend the chat engine instance to set
     */
    public void setBackend(@Nullable ChatBackend backend) {
        this.backend = backend;
    }

    /**
     * Returns the currently active group for this user session.
     *
     * @return the active group, or null if no group is active
     */
    @Nullable
    public Group getCurrentlyActiveGroup() {
        return currentlyActiveGroup;
    }

    /**
     * Sets the currently active group for this user session.
     *
     * @param currentlyActiveGroup the group to set as active
     */
    public void setCurrentlyActiveGroup(@Nullable Group currentlyActiveGroup) {
        this.currentlyActiveGroup = currentlyActiveGroup;
    }

    /**
     * Purges this user session by resetting the currently active group and chat engine instances to null.
     */
    public void purge() {
        currentlyActiveGroup = null;
        backend = null;
    }
}
