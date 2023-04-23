package chat.frontend.swing;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static javax.swing.JOptionPane.showMessageDialog;

public class ChatSwingMain extends JFrame {

	private boolean isLoggedIn = false;
	private final ChatSwingLoginPanel chatSwingLoginPanel;
	private final ChatSwingReceivingPanel chatSwingReceivingPanel;
	private final ChatSwingSendingPanel chatSwingSendingPanel;

	ChatSwingMain() throws MalformedURLException, RemoteException, RuntimeException {
		JFrame frame = new JFrame("Decentralized Chat");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(600, 500));

		BorderLayout borderLayout = new BorderLayout();
		JPanel panel = new JPanel(borderLayout);

		chatSwingLoginPanel = new ChatSwingLoginPanel(this);
		chatSwingReceivingPanel = new ChatSwingReceivingPanel();
		chatSwingSendingPanel = new ChatSwingSendingPanel();

		panel.add(chatSwingLoginPanel, BorderLayout.PAGE_START);
		panel.add(chatSwingReceivingPanel, BorderLayout.CENTER);
		panel.add(chatSwingSendingPanel, BorderLayout.PAGE_END);

		frame.setContentPane(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	protected boolean isLoggedIn() {
		return isLoggedIn;
	}

	protected void switchUIEnabledStatus() throws MalformedURLException, RemoteException {
		isLoggedIn = !isLoggedIn;
		chatSwingLoginPanel.switchUIEnabledStatus(isLoggedIn);
		chatSwingReceivingPanel.switchUIEnabledStatus(isLoggedIn);
		chatSwingSendingPanel.switchUIEnabledStatus(isLoggedIn);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new ChatSwingMain();
			} catch (Exception e) {
				showMessageDialog(null, e.getMessage());
			}
		});
	}
}
