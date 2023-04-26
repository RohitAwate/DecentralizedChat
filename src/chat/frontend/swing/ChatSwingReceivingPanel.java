package chat.frontend.swing;

import chat.backend.Group;
import chat.backend.Message;
<<<<<<< Updated upstream
import chat.backend.MockChatEngine;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
=======

import javax.swing.*;
>>>>>>> Stashed changes
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
<<<<<<< Updated upstream
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
=======
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
>>>>>>> Stashed changes

import static javax.swing.JOptionPane.showMessageDialog;

public class ChatSwingReceivingPanel extends JPanel {

<<<<<<< Updated upstream
	private final List<Group> groups;
	private final JList<Group> groupJList;
	private final JScrollPane groupListJScrollPane;
	private final JTextArea groupMessagesJTextArea;

	ChatSwingReceivingPanel() throws MalformedURLException, IllegalArgumentException, RemoteException {
		setPreferredSize(new Dimension(600, 400));
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

		groups = new ArrayList<>();
=======
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

>>>>>>> Stashed changes
		groupJList = new JList<>();
		groupJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		groupJList.setLayoutOrientation(JList.VERTICAL);
		groupJList.setVisibleRowCount(-1);
<<<<<<< Updated upstream
		groupJList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent listSelectionEvent) {
				try {
					groupMessagesJTextArea.setText(null);
					if (!groups.isEmpty()) {
						List<Message> groupHistory = groupJList.getSelectedValue().getMessageHistory();
						for (Message m : groupHistory) {
							Date date = new Date(m.getTimestamp());
							Format format = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aaa");
							groupMessagesJTextArea.append(String.format("[%s] %s: %s\n",
									format.format(date), m.getFrom().getDisplayName(), m.getContents()));
						}
					}
				} catch (RemoteException e) {
					showMessageDialog(null, e.getMessage());
				}
=======
		groupJList.addListSelectionListener(listSelectionEvent -> {
			if (!listSelectionEvent.getValueIsAdjusting()) {
				session.setCurrentlyActiveGroup(groupJList.getSelectedValue());
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
	}

	private void addAllGroups() throws MalformedURLException, RemoteException {
		Group group1 = new Group("TestGroup1");
		Group group2 = new Group("TestGroup2");
		Group group3 = new Group("TestGroup3");

		MockChatEngine mockUser1 = new MockChatEngine("user 1", 1);
		MockChatEngine mockUser2 = new MockChatEngine("user 2", 2);
		MockChatEngine mockUser3 = new MockChatEngine("user 3", 3);

		Message message1 = new Message(mockUser1, "test message 1", System.currentTimeMillis());
		Message message2 = new Message(mockUser1, "test message 2", System.currentTimeMillis());
		Message message3 = new Message(mockUser1, "test message 3", System.currentTimeMillis());
		Message message4 = new Message(mockUser2, "test message 4", System.currentTimeMillis());
		Message message5 = new Message(mockUser2, "test message 5", System.currentTimeMillis());
		Message message6 = new Message(mockUser2, "test message 6", System.currentTimeMillis());
		Message message7 = new Message(mockUser3, "test message 7", System.currentTimeMillis());
		Message message8 = new Message(mockUser3, "test message 8", System.currentTimeMillis());
		Message message9 = new Message(mockUser3, "test message 9", System.currentTimeMillis());

		group1.addMessageToGroupHistory(message1);
		group1.addMessageToGroupHistory(message2);
		group1.addMessageToGroupHistory(message3);
		group1.addMessageToGroupHistory(message4);
		group1.addMessageToGroupHistory(message5);
		group1.addMessageToGroupHistory(message6);

		group2.addMessageToGroupHistory(message4);
		group2.addMessageToGroupHistory(message5);
		group2.addMessageToGroupHistory(message6);
		group2.addMessageToGroupHistory(message7);
		group2.addMessageToGroupHistory(message8);
		group2.addMessageToGroupHistory(message9);

		group3.addMessageToGroupHistory(message1);
		group3.addMessageToGroupHistory(message2);
		group3.addMessageToGroupHistory(message3);
		group3.addMessageToGroupHistory(message7);
		group3.addMessageToGroupHistory(message8);
		group3.addMessageToGroupHistory(message9);

		groups.add(group1);
		groups.add(group2);
		groups.add(group3);
	}

	protected void switchUIEnabledStatus(boolean isLoggedIn) throws MalformedURLException, RemoteException {
		if (!isLoggedIn) {
			groups.clear();
			groupJList.clearSelection();
			groupMessagesJTextArea.setText(null);
		} else {
			addAllGroups();
		}
		groupJList.setEnabled(isLoggedIn);
		groupListJScrollPane.setEnabled(isLoggedIn);
		groupMessagesJTextArea.setEnabled(isLoggedIn);
		groupJList.setListData(groups.toArray(new Group[0]));
=======

		new ChatSwingWorkerViewUpdate(Executors.newScheduledThreadPool(1)).schedule();
	}

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
				groupMessagesJTextArea.setText(null);
				for (Message m : get()) {
					Date date = new Date(m.getTimestamp());
					Format format = new SimpleDateFormat("MM/dd/yyyy, hh:mm:ss aaa");
					groupMessagesJTextArea.append(String.format("[%s] %s: %s\n",
							format.format(date), m.getFrom().getDisplayName(), m.getContents()));
				}
			} catch (RemoteException e) {
				showMessageDialog(null, e.getMessage());
			} catch (ExecutionException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
>>>>>>> Stashed changes
	}
}
