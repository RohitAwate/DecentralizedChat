package chat.frontend.swing;

<<<<<<< Updated upstream
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
=======
import chat.backend.Message;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static javax.swing.JOptionPane.showMessageDialog;

public class ChatSwingSendingPanel extends JPanel {

	private final ChatSwingMain parent;
	private final ChatSwingSession session;
	private final JTextFieldHinted messageTextField;
	private final JButton sendMessageButton;

	ChatSwingSendingPanel(ChatSwingMain parent, ChatSwingSession session)
			throws MalformedURLException, IllegalArgumentException, RemoteException {
		this.parent = parent;
		this.session = session;
		setMaximumSize(new Dimension(600, 50));
		setLayout(new FlowLayout(FlowLayout.CENTER));

		messageTextField = new JTextFieldHinted("Enter message");
		messageTextField.setPreferredSize(new Dimension(450, 25));
		messageTextField.setEnabled(false);
		add(messageTextField);

		sendMessageButton = new JButton("Send");
		sendMessageButton.setPreferredSize(new Dimension(100, 25));
		sendMessageButton.setEnabled(false);
		sendMessageButton.addActionListener(actionEvent -> {
			try {
				String message = messageTextField.getText();
				if (message == null || message.isEmpty()) {
					throw new IllegalArgumentException("Empty message!");
				}
				if (session.isLoggedIn()) {
					if (!session.ifAnyGroupActive()) {
						throw new IllegalArgumentException("Please select a group first!");
					}
					session.getCurrentlyActiveGroup()
							.addMessageToGroupHistory(new Message(session.getChatEngine(), message, System.currentTimeMillis()));
					session.getChatEngine().sendMessage(message, session.getCurrentlyActiveGroup().getName());
				}
				parent.refreshUI();
			} catch (Exception e) {
				showMessageDialog(null, e.getMessage());
			}
		});
		add(sendMessageButton);
	}

	protected void refreshUI() {
		messageTextField.reset();
		messageTextField.setEnabled(session.isLoggedIn());
		sendMessageButton.setEnabled(session.isLoggedIn());
>>>>>>> Stashed changes
	}
}
