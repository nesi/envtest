package nz.org.nesi.envtester.view.swing;

import grith.gridsession.GridClient;
import grith.gridsession.view.GridLoginDialog;
import grith.jgrith.cred.AbstractCred;
import grith.jgrith.cred.Cred;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import net.miginfocom.swing.MigLayout;
import nz.org.nesi.envtester.EnvTest;
import nz.org.nesi.envtester.TestController;

import com.google.common.io.Files;

public class EnvTestStarterPanel extends JPanel implements
		PropertyChangeListener {

	public static final String LIST_PANEL = "list";
	public static final String LOG_PANEL = "log";

	private JPanel panel;
	private JButton btnNewButton;
	private TestListPanel testListPanel;
	private LogPanel logPanel;

	private final TestController testController;
	private final GridClient client;
	private JSpinner spinner;
	private JLabel lblRepeat;

	/**
	 * Create the panel.
	 */
	public EnvTestStarterPanel(TestController tc) {
		this.testController = tc;
		this.testController.addPropertyChangeListener(this);
		try {
			client = new GridClient();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		setLayout(new MigLayout("", "[][grow][grow]", "[grow][]"));
		add(getPanel(), "cell 0 0 3 1,grow");
		add(getLblRepeat(), "cell 0 1");
		add(getSpinner(), "cell 1 1");
		add(getBtnNewButton(), "cell 2 1,alignx right");

		for (EnvTest t : tc.getTests()) {
			t.addPropertyChangeListener(this);
		}

	}

	private JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new CardLayout(0, 0));
			panel.add(getTestListPanel(), LIST_PANEL);
			panel.add(getLogPanel(), LOG_PANEL);
		}
		return panel;
	}

	private JButton getBtnNewButton() {
		if (btnNewButton == null) {
			btnNewButton = new JButton("Start");
			btnNewButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {

					if ("Start".equals(btnNewButton.getText())) {

						Thread t = new Thread() {
							public void run() {
								
								if ( testController.requiresAuthentication() ) {
									
									Cred c = AbstractCred.getExistingCredential();
									if ( c == null ) { 
										GridLoginDialog d = new GridLoginDialog(client);
										d.setVisible(true);
										c = client.getCredential();
									}
									
									testController.setAuthentication(c);
									
								}

								try {
									switchToPanel(LOG_PANEL);
									getBtnNewButton().setEnabled(false);
									getBtnNewButton().setText("Running...");

									testController.startTests((Integer)getSpinner().getValue());
									
									File zip = testController.createZipFile();
									
									try {
										Files.move(zip, new File("/home/markus/Desktop/zipfile.zip"));
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}

								} finally {
									getBtnNewButton().setText("OK");
									getBtnNewButton().setEnabled(true);
								}

							}
						};

						t.start();

					} else if ("OK".equals(btnNewButton.getText())) {
						clearLogPanel();
						switchToPanel(LIST_PANEL);
						btnNewButton.setText("Start");
					}

				}
			});

		}
		return btnNewButton;
	}

	private TestListPanel getTestListPanel() {
		if (testListPanel == null) {
			testListPanel = new TestListPanel();
			for (EnvTest t : testController.getTests()) {
				testListPanel.addTest(t);
			}
		}
		return testListPanel;
	}

	private LogPanel getLogPanel() {
		if (logPanel == null) {
			logPanel = new LogPanel();
		}
		return logPanel;
	}

	private void clearLogPanel() {
		getLogPanel().clear();
	}

	private void switchToPanel(String panelName) {
		CardLayout cl = (CardLayout) (getPanel().getLayout());
		cl.show(getPanel(), panelName);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		
		if ( oldValue != null && newValue instanceof EnvTest ) {
			getLogPanel().addMessage("\n======================================================\n");
		} else if ( evt.getNewValue() instanceof String ){
			getLogPanel().addMessage((String)evt.getNewValue());
		}

	}
	private JSpinner getSpinner() {
		if (spinner == null) {
			spinner = new JSpinner();
			spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		}
		return spinner;
	}
	private JLabel getLblRepeat() {
		if (lblRepeat == null) {
			lblRepeat = new JLabel("Repeat:");
		}
		return lblRepeat;
	}
}
