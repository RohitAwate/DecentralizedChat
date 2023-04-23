package chat.frontend.swing;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JTextFieldHinted extends JTextField implements FocusListener {

	private final String hint;
	private boolean showingHint;

	public JTextFieldHinted(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	@Override
	public void focusGained(FocusEvent e) {
		if(this.getText().isEmpty()) {
			super.setText("");
			showingHint = false;
		}
	}
	@Override
	public void focusLost(FocusEvent e) {
		if(this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}

	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}

	public void reset() {
		setText(hint);
		this.showingHint = true;
	}
}
