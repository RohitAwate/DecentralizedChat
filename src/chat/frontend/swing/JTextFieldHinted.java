package chat.frontend.swing;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * A JTextField with a default hint shown when no text is entered.
 */
public class JTextFieldHinted extends JTextField implements FocusListener {

	/**
	 * The hint text to show when no text is entered.
	 */
	private final String hint;

	/**
	 * Indicates whether the hint is currently being shown.
	 */
	private boolean showingHint;

	/**
	 * Constructs a new JTextFieldHinted with the specified hint.
	 *
	 * @param hint the hint text to show when no text is entered
	 */
	public JTextFieldHinted(final String hint) {
		super(hint);
		this.hint = hint;
		this.showingHint = true;
		super.addFocusListener(this);
	}

	/**
	 * Invoked when the component gains focus.
	 *
	 * @param e the focus event
	 */
	@Override
	public void focusGained(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText("");
			showingHint = false;
		}
	}

	/**
	 * Invoked when the component loses focus.
	 *
	 * @param e the focus event
	 */
	@Override
	public void focusLost(FocusEvent e) {
		if (this.getText().isEmpty()) {
			super.setText(hint);
			showingHint = true;
		}
	}

	/**
	 * Returns the text entered in the text field, or an empty string if the hint is being shown.
	 *
	 * @return the text entered in the text field, or an empty string if the hint is being shown
	 */
	@Override
	public String getText() {
		return showingHint ? "" : super.getText();
	}

	/**
	 * Resets the text field to its default hint.
	 */
	public void reset() {
		setText(hint);
		this.showingHint = true;
	}
}

