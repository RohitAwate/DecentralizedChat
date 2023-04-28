package chat.frontend.swing;

import chat.backend.Message;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * This panel allows the user to send a message or upload a file.
 */
public class ChatSwingSendingPanel extends JPanel {

    private final ChatSwingMain parent;
    private final ChatSwingSession session;
    private final JButton fileChooseButton;
    private final JFileChooser fileChooser;
    private final JTextFieldHinted messageTextField;
    private final JButton sendMessageButton;

    ChatSwingSendingPanel(ChatSwingMain parent, ChatSwingSession session)
            throws MalformedURLException, IllegalArgumentException, RemoteException {
        this.parent = parent;
        this.session = session;
        setMaximumSize(new Dimension(600, 50));
        setLayout(new FlowLayout(FlowLayout.CENTER));

        fileChooseButton = new JButton("Upload");
        fileChooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        fileChooseButton.setPreferredSize(new Dimension(100, 25));
        fileChooseButton.setEnabled(false);
        fileChooseButton.addActionListener(e -> {
            int chosenOption = fileChooser.showOpenDialog(null);
            if (chosenOption == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                if (session.isLoggedIn() && selectedFile.exists()) {
                    // Call file upload from here
                    String path = selectedFile.getAbsolutePath();
                    try {
                        boolean success = session.getBackend().sendFile(selectedFile, session.getCurrentlyActiveGroup());
                        if (success) {
                            session.getCurrentlyActiveGroup().addMessageToGroupHistory(
                                    new Message(session.getBackend().getDisplayName(),
                                            "Sent file: " + selectedFile.getName(),
                                            System.currentTimeMillis())
                            );
                        }
                    } catch (IOException ex) {
                        showMessageDialog(null, ex.getMessage());
                    }
                }
            }
        });
        add(fileChooseButton);

        messageTextField = new JTextFieldHinted("Enter message");
        messageTextField.setPreferredSize(new Dimension(375, 25));
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
                            .addMessageToGroupHistory(new Message(session.getBackend().getDisplayName(), message, System.currentTimeMillis()));
                    session.getBackend().sendMessage(message, session.getCurrentlyActiveGroup());
                }
                parent.refreshUI();
            } catch (Exception e) {
                showMessageDialog(null, e.getMessage());
            }
        });
        add(sendMessageButton);
    }

    /**
     * Refresh all the panels in the UI to reflect the latest state.
     */
    protected void refreshUI() {
        messageTextField.reset();
        fileChooseButton.setEnabled(session.isLoggedIn());
        messageTextField.setEnabled(session.isLoggedIn());
        sendMessageButton.setEnabled(session.isLoggedIn());
    }
}
