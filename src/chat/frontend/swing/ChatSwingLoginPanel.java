package chat.frontend.swing;

import chat.backend.ChatEngine;
import chat.logging.Logger;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import static javax.swing.JOptionPane.showMessageDialog;


/**
 * Class represents a login panel providing interface for providing the host name, port number and
 * group name (optional). Also provides functionality to connect and disconnect as a client peer.
 */
public class ChatSwingLoginPanel extends JPanel {

    private final ChatSwingMain parent;
    private final ChatSwingSession session;
    private final JTextFieldHinted nameTextField;
    private final JTextFieldHinted portTextField;
    private final JButton sessionButton;

    ChatSwingLoginPanel(ChatSwingMain parent, ChatSwingSession session)
            throws MalformedURLException, IllegalArgumentException, RemoteException {
        this.parent = parent;
        this.session = session;
        setMaximumSize(new Dimension(300, 50));
        setLayout(new FlowLayout(FlowLayout.LEADING));

        nameTextField = new JTextFieldHinted("Enter name");
        nameTextField.setPreferredSize(new Dimension(100, 25));
        add(nameTextField);

        portTextField = new JTextFieldHinted("Enter port");
        portTextField.setPreferredSize(new Dimension(75, 25));
        add(portTextField);

        sessionButton = new JButton("Login");
        sessionButton.setPreferredSize(new Dimension(75, 25));
        sessionButton.addActionListener(actionEvent -> {
            try {
                sessionEnabler();
            } catch (Exception e) {
                showMessageDialog(null, e.getMessage());
            }
        });
        add(sessionButton);
    }

    private void sessionEnabler()
            throws MalformedURLException, NotBoundException, IllegalArgumentException, RemoteException {
        if (!session.isLoggedIn()) {
            String displayName = nameTextField.getText();
            if (displayName == null || displayName.isEmpty()) {
                throw new IllegalArgumentException("Empty name!");
            }
            int selfPort;
            try {
                selfPort = Integer.parseInt(portTextField.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid port number!");
            }
            Logger.setOwner(displayName, selfPort);
            session.setBackend(new ChatEngine(displayName, selfPort));
        } else {
            session.purge();
            nameTextField.reset();
            portTextField.reset();
            System.gc();
            System.exit(0);
        }
        parent.refreshUI();
    }

    /**
     * Refresh all the panels in the UI to reflect the latest state.
     */
    protected void refreshUI() {
        nameTextField.setEnabled(!session.isLoggedIn());
        portTextField.setEnabled(!session.isLoggedIn());
        if (session.isLoggedIn()) {
            sessionButton.setText("Logout");
        } else {
            sessionButton.setText("Login");
        }
    }
}
