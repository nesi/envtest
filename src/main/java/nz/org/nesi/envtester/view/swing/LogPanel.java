package nz.org.nesi.envtester.view.swing;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class LogPanel extends JPanel {
	private JScrollPane scrollPane;
	private JTextPane textPane;

	private final StyledDocument doc;
	private final Style normalStyle;
	private final Style warningStyle;

	/**
	 * Create the panel.
	 */
	public LogPanel() {
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane(), BorderLayout.CENTER);
		doc = getTextPane().getStyledDocument();
		normalStyle = textPane.addStyle("NormalStyle", null);
		StyleConstants.setForeground(normalStyle, Color.black);

		warningStyle = textPane.addStyle("WarningStyle", normalStyle);
		StyleConstants.setForeground(warningStyle, Color.red);
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTextPane());
		}
		return scrollPane;
	}

	private JTextPane getTextPane() {
		if (textPane == null) {
			textPane = new JTextPane();
			textPane.setEditable(false);

		}
		return textPane;
	}

	public void addMessage(String message) {

		addMessage(message, normalStyle);
	}

	public void addWarningMessage(String message) {
		addMessage(message, warningStyle);
	}

	public void addMessage(final String message, final Style style) {
		SwingUtilities.invokeLater(new Thread() {
			public void run() {

				try {
					doc.insertString(doc.getLength(), message + "\n", style);
				} catch (BadLocationException e) {
				}
			}
		});
	}

	public void clear() {
		SwingUtilities.invokeLater(new Thread() {
			public void run() {

				try {
					doc.remove(0, doc.getLength());
				} catch (BadLocationException e) {
					e.printStackTrace();
				}

			}
		});
	}
}
