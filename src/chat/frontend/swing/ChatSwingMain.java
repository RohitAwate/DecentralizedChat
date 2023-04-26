package chat.frontend.swing;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
<<<<<<< Updated upstream
=======
import java.util.concurrent.ExecutionException;
>>>>>>> Stashed changes

import static javax.swing.JOptionPane.showMessageDialog;

public class ChatSwingMain extends JFrame {

<<<<<<< Updated upstream
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
=======
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
>>>>>>> Stashed changes
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				new ChatSwingMain();
			} catch (Exception e) {
<<<<<<< Updated upstream
=======
				e.printStackTrace();
>>>>>>> Stashed changes
				showMessageDialog(null, e.getMessage());
			}
		});
	}
<<<<<<< Updated upstream
=======

	protected void refreshUI()
			throws MalformedURLException, IllegalArgumentException, RemoteException {
		chatSwingLoginPanel.refreshUI();
		chatSwingJoiningPanel.refreshUI();
		chatSwingReceivingPanel.refreshUI();
		chatSwingSendingPanel.refreshUI();
	}
>>>>>>> Stashed changes
}
