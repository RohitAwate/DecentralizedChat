package chat.frontend.swing;

import chat.backend.Group;
import chat.backend.Message;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This panel shows the messages in the app.
 */
public class ChatSwingReceivingPanel extends JPanel {

    private final ChatSwingMain parent;
    private final JList<Group> groupJList;
    private final ChatSwingSession session;
    private final JScrollPane groupListJScrollPane;
    private final JTextArea groupMessagesJTextArea;

    ChatSwingReceivingPanel(ChatSwingMain parent, ChatSwingSession session)
            throws MalformedURLException, IllegalArgumentException, RemoteException, ExecutionException, InterruptedException {
        this.parent = parent;
        this.session = session;
        setMaximumSize(new Dimension(600, 400));
        setLayout(new FlowLayout(FlowLayout.CENTER));

        groupJList = new JList<>();
        groupJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupJList.setLayoutOrientation(JList.VERTICAL);
        groupJList.setVisibleRowCount(-1);
        groupJList.addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                session.setCurrentlyActiveGroup(groupJList.getSelectedValue());
            }
        });

        groupListJScrollPane = new JScrollPane(groupJList);
        groupListJScrollPane.setPreferredSize(new Dimension(175, 400));
        groupListJScrollPane.setEnabled(false);
        add(groupListJScrollPane);

        groupMessagesJTextArea = new JTextArea();
        groupMessagesJTextArea.setPreferredSize(new Dimension(375, 400));
        groupMessagesJTextArea.setEditable(false);
        groupMessagesJTextArea.setEnabled(false);
        add(groupMessagesJTextArea);

        new ChatSwingWorkerViewUpdate(Executors.newScheduledThreadPool(1)).schedule();
    }

    /**
     * Refresh all the panels in the UI to reflect the latest state.
     */
    protected void refreshUI()
            throws MalformedURLException, IllegalArgumentException, RemoteException {
        if (!session.isLoggedIn()) {
            groupJList.clearSelection();
            groupMessagesJTextArea.setText(null);
        } else {
            if (groupJList.getModel().getSize() != session.getGroups().size()) {
                groupJList.setListData(session.getGroups().toArray(new Group[0]));
            }
        }
        groupJList.setEnabled(session.isLoggedIn());
        groupListJScrollPane.setEnabled(session.isLoggedIn());
        groupMessagesJTextArea.setEnabled(session.isLoggedIn());
    }

    /**
     * Worker thread that is used to periodically grab the latest changes and update the state.
     */
    private class ChatSwingWorkerViewUpdate extends SwingWorker<List<Message>, Message> {

        private final ScheduledExecutorService service;

        private ChatSwingWorkerViewUpdate(ScheduledExecutorService service) {
            this.service = service;
        }

        public void schedule() {
            service.schedule(new ChatSwingWorkerViewUpdate(service), 100, TimeUnit.MILLISECONDS);
        }

        @Override
        protected List<Message> doInBackground() {
            schedule();
            if (session.isLoggedIn() && session.ifAnyGroupActive()) {
                return session.getCurrentlyActiveGroup().getHistory();
            }
            return new ArrayList<>();
        }

        @Override
        protected void done() {
            try {
                if (groupMessagesJTextArea != null && session.isLoggedIn() && session.ifAnyGroupActive()) {
                    groupMessagesJTextArea.setText(null);
                    for (Message m : get()) {
                        Date date = new Date(m.getTimestamp());
                        Format format = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aaa");
                        groupMessagesJTextArea.append(String.format("[%s] %s: %s\n",
                                format.format(date), m.getFrom(), m.getContents()));
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
