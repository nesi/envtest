package nz.org.nesi.envtester.view.swing;

import grith.gridsession.GridClient;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import nz.org.nesi.envtester.DummyTest;
import nz.org.nesi.envtester.EnvTest;

public class TestSelectPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;

	private JLabel lblNewLabel;
	
	private final EnvTest test;
	private JTextField textField;
	private JCheckBox chckbxNewCheckBox;

	private boolean enabled = true;
	

	public TestSelectPanel(EnvTest test) {
		this.test = test;
		setLayout(new MigLayout("", "[][grow][grow]", "[][]"));
		add(getChckbxNewCheckBox(), "cell 0 0");
		add(getLblNewLabel(), "cell 1 0");
		add(getTextField(), "cell 1 1 2 1,growx");
		
		getChckbxNewCheckBox().setSelected(test.executeTestByDefault());

//		setEnabled(!test.requiresAuthentication());
		
	}
	
	@Override
	public Dimension getMaximumSize() {
	    Dimension size = getPreferredSize();
	    size.width = Short.MAX_VALUE;
	    return size;
	}
	
	/**
	 * Create the panel.
	 */
	public TestSelectPanel() {
		this(new DummyTest());
	}
	
	public EnvTest getTest() {
		return this.test;
	}


	private JLabel getLblNewLabel() {
		if (lblNewLabel == null) {
			lblNewLabel = new JLabel(test.getTestName());
		}
		return lblNewLabel;
	}
	
	
	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.setEditable(false);
			textField.setColumns(10);
			textField.setText(test.getTestDescription());
		}
		return textField;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		
		this.enabled = enabled;
		getTextField().setEnabled(enabled);
		getLblNewLabel().setEnabled(enabled);
		getChckbxNewCheckBox().setEnabled(enabled);
		
	}


	private JCheckBox getChckbxNewCheckBox() {
		if (chckbxNewCheckBox == null) {
			chckbxNewCheckBox = new JCheckBox("");
			chckbxNewCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
				    if (e.getStateChange() == ItemEvent.SELECTED) {
				    	test.setEnabled(true);
				    } else {
				        test.setEnabled(false);
				    }
				}
			});
		}
		return chckbxNewCheckBox;
	}

	public boolean isRunTest() {

		return enabled && getChckbxNewCheckBox().isSelected();
	}
}
