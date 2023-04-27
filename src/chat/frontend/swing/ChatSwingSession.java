package chat.frontend.swing;

import chat.backend.ChatEngine;
import chat.backend.Group;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class ChatSwingSession {

	private final String sessionId = UUID.randomUUID().toString();
	@Nullable
	private ChatEngine chatEngine;
	@Nullable
	private Group currentlyActiveGroup;

	public boolean isLoggedIn() {
		return chatEngine != null;
	}

	@Nullable
	public List<Group> getGroups() {
		return chatEngine.getGroups();
	}

	public String getSessionId() {
		return sessionId;
	}

	public boolean ifAnyGroupActive() {
		return currentlyActiveGroup != null;
	}

	@Nullable
	public ChatEngine getChatEngine() {
		return chatEngine;
	}

	public void setChatEngine(@Nullable ChatEngine chatEngine) {
		this.chatEngine = chatEngine;
	}

	@Nullable
	public Group getCurrentlyActiveGroup() {
		return currentlyActiveGroup;
	}

	public void setCurrentlyActiveGroup(@Nullable Group currentlyActiveGroup) {
		this.currentlyActiveGroup = currentlyActiveGroup;
	}

	public void purge() {
		currentlyActiveGroup = null;
		chatEngine = null;
	}
}
