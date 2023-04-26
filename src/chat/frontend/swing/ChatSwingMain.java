package chat.frontend.swing;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;

import static javax.swing.JOptionPane.showMessageDialog;

public class ChatSwingMain extends JFrame {

	private final ChatSwingLoginPanel chatSwingLoginPanel;
	private final ChatSwingJoiningPanel chatSwingJoiningPanel;
	private final ChatSwingReceivingPanel chatSwingReceivingPanel;
	private final ChatSwingSendingPanel chatSwingSendingPanel;

	ChatSwingMain() throws MalformedURLException, RemoteException, RuntimeException, ExecutionException, InterruptedException {
		super("Decentralized Chat");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setPreferredSize(new Dimension(600, 500));
		setResizable(false);

		JPanel panel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());

		ChatSwingSession session = new ChatSwingSession();
		chatSwingLoginPanel = new ChatSwingLoginPanel(this, session);
		chatSwingJoiningPanel = new ChatSwingJoiningPanel(this, session);
		chatSwingReceivingPanel = new ChatSwingReceivingPanel(this, session);
		chatSwingSendingPanel = new ChatSwingSendingPanel(this, session);

		topPanel.add(chatSwingLoginPanel, BorderLayout.LINE_START);
		topPanel.add(chatSwingJoiningPanel, BorderLayout.LINE_END);
		panel.add(topPanel, BorderLayout.PAGE_START);
		panel.add(chatSwingReceivingPanel, BorderLayout.CENTER);
		panel.add(chatSwingSendingPanel, BorderLayout.PAGE_END);

		setContentPane(panel);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		setFocusable(true);
		requestFocus();
		requestFocusInWindow();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new ChatSwingMain();
			} catch (Exception e) {
				e.printStackTrace();
				showMessageDialog(null, e.getMessage());
			}
		});
	}

	protected void refreshUI()
			throws MalformedURLException, IllegalArgumentException, RemoteException {
		chatSwingLoginPanel.refreshUI();
		chatSwingJoiningPanel.refreshUI();
		chatSwingReceivingPanel.refreshUI();
		chatSwingSendingPanel.refreshUI();
	}
}
