package chat.frontend.swing;

import chat.backend.ChatBackend;
import chat.backend.MockChatEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static javax.swing.JOptionPane.showMessageDialog;


/**
 * Class represents a login panel providing interface for providing the host name, port number and
 * group name (optional). Also provides functionality to connect and disconnect as a client peer.
 */
public class ChatSwingLoginPanel extends JPanel {

	private MockChatEngine mockChatEngine;
	private final ChatSwingMain parent;

	private final JTextFieldHinted host;
	private final JTextFieldHinted port;
	private final JTextFieldHinted group_name;

	private final JButton connect;
	private final JButton disconnect;


	ChatSwingLoginPanel(ChatSwingMain parent) throws RuntimeException {
		this.parent = parent;
		setPreferredSize(new Dimension(600, 50));
		setLayout(new FlowLayout(FlowLayout.CENTER, 2, 12));

		host = new JTextFieldHinted("Enter hostname");
		host.setPreferredSize(new Dimension(100, 25));
		add(host);

		port = new JTextFieldHinted("Enter port");
		port.setPreferredSize(new Dimension(75, 25));
		add(port);

		group_name = new JTextFieldHinted("Enter group name (optional)");
		group_name.setPreferredSize(new Dimension(175, 25));
		add(group_name);

		connect = new JButton("Connect");
		connect.setPreferredSize(new Dimension(100, 25));
		connect.addActionListener(actionEvent -> {
			try {
				login();
			} catch (MalformedURLException | IllegalArgumentException | RemoteException e) {
				showMessageDialog(null, e.getMessage());
			}
		});
		add(connect);

		disconnect = new JButton("Disconnect");
		disconnect.setPreferredSize(new Dimension(100, 25));
		disconnect.setEnabled(false);
		disconnect.addActionListener(actionEvent -> {
			try {
				logout();
			} catch (MalformedURLException | IllegalArgumentException | RemoteException e) {
				showMessageDialog(null, e.getMessage());
			}
		});
		add(disconnect);
	}

	private void login() throws MalformedURLException, IllegalArgumentException, RemoteException {
		String displayName = host.getText();
		if (displayName == null || displayName.isEmpty()) {
			throw new IllegalArgumentException("Invalid host name!");
		}
		int selfPort;
		try {
			selfPort = Integer.parseInt(port.getText());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid port number!");
		}
		if (!parent.isLoggedIn()) {
			parent.switchUIEnabledStatus();
		}
		mockChatEngine = new MockChatEngine(displayName, selfPort);
	}

	private void logout() throws MalformedURLException, RemoteException {
		mockChatEngine = null;
		host.reset();
		port.reset();
		group_name.reset();
		if (parent.isLoggedIn()) {
			parent.switchUIEnabledStatus();
		}
	}

	protected void switchUIEnabledStatus(boolean isLoggedIn) {
		host.setEnabled(!isLoggedIn);
		port.setEnabled(!isLoggedIn);
		group_name.setEnabled(!isLoggedIn);
		connect.setEnabled(!isLoggedIn);
		disconnect.setEnabled(isLoggedIn);
	}
}
