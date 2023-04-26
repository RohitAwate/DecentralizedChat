package chat.frontend.swing;

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
	}
}
