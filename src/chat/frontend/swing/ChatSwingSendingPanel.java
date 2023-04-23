package chat.frontend.swing;

import javax.swing.*;
import java.awt.*;

public class ChatSwingSendingPanel extends JPanel {

	private final JTextFieldHinted message;
	private final JButton sendMessage;

	ChatSwingSendingPanel() {
		setPreferredSize(new Dimension(600, 50));
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 12));

		message = new JTextFieldHinted("Enter message");
		message.setPreferredSize(new Dimension(450, 25));
		message.setEnabled(false);
		add(message);

		sendMessage = new JButton("Send");
		sendMessage.setPreferredSize(new Dimension(100, 25));
		sendMessage.setEnabled(false);
		add(sendMessage);
	}

	protected void switchUIEnabledStatus(boolean isLoggedIn) {
		if (!isLoggedIn) {
			message.reset();
		}
		message.setEnabled(isLoggedIn);
		sendMessage.setEnabled(isLoggedIn);
	}
}
