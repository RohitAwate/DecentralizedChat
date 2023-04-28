package chat.frontend.swing;

import chat.backend.Group;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Optional;

import static javax.swing.JOptionPane.showMessageDialog;

/**
 * Panel used for joining a group.
 * The panel contains three UI components: a text field for the group's port, a text field for the group's name,
 * and a button for joining the group.
 */
public class ChatSwingJoiningPanel extends JPanel {

    private final ChatSwingMain parent;
    private final ChatSwingSession session;
    private final JTextFieldHinted groupPortTextField;
    private final JTextFieldHinted groupNameTextField;
    private final JButton joinGroupButton;

    ChatSwingJoiningPanel(ChatSwingMain parent, ChatSwingSession session)
            throws MalformedURLException, IllegalArgumentException, RemoteException {
        this.parent = parent;
        this.session = session;
        setMaximumSize(new Dimension(300, 50));
        setLayout(new FlowLayout(FlowLayout.TRAILING));

        groupPortTextField = new JTextFieldHinted("Group port");
        groupPortTextField.setPreferredSize(new Dimension(75, 25));
        groupPortTextField.setEnabled(false);
        add(groupPortTextField);

        groupNameTextField = new JTextFieldHinted("Group name");
        groupNameTextField.setPreferredSize(new Dimension(100, 25));
        groupNameTextField.setEnabled(false);
        add(groupNameTextField);

        joinGroupButton = new JButton("Join");
        joinGroupButton.setPreferredSize(new Dimension(75, 25));
        joinGroupButton.setEnabled(false);
        joinGroupButton.addActionListener(actionEvent -> {
            try {
                String groupName = groupNameTextField.getText();
                if (groupName == null || groupName.isEmpty()) {
                    throw new IllegalArgumentException("Empty name!");
                }
                int groupPort;
                try {
                    groupPort = Integer.parseInt(groupPortTextField.getText());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid port number!");
                }
                joinGroup(groupPort, groupName);
            } catch (Exception e) {
                showMessageDialog(null, e.getMessage());
            }
        });
        add(joinGroupButton);
    }

    private void joinGroup(int port, String groupName)
            throws MalformedURLException, IllegalArgumentException, RemoteException {
        if (groupName != null && !groupName.isEmpty() && session.isLoggedIn()) {
            Optional<Group> group = session.getBackend().joinGroup("localhost", port, groupName);
            if (group.isPresent()) {
                session.setCurrentlyActiveGroup(group.get());
                showMessageDialog(null, String.format("Joined group %s at port %d!", groupName, port));
            } else {
                session.getBackend().createGroup(groupName);
                showMessageDialog(null,
                        String.format("No groups found with name %s at port %d! New group created!", groupName, port));

            }
        }
        parent.refreshUI();
    }
    /**
     * Refresh all the panels in the UI to reflect the latest state.
     */
    protected void refreshUI() {
        groupPortTextField.reset();
        groupNameTextField.reset();
        groupPortTextField.setEnabled(session.isLoggedIn());
        groupNameTextField.setEnabled(session.isLoggedIn());
        joinGroupButton.setEnabled(session.isLoggedIn());
    }
}
