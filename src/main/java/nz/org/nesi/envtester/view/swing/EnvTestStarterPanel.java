package nz.org.nesi.envtester.view.swing;

import com.google.common.io.Files;
import grisu.jcommons.utils.swing.LogPanel;
import grith.gridsession.GridClient;
import grith.gridsession.view.GridLoginDialog;
import grith.jgrith.cred.AbstractCred;
import grith.jgrith.cred.Cred;
import net.miginfocom.swing.MigLayout;
import nz.org.nesi.envtester.EnvTest;
import nz.org.nesi.envtester.TestController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

public class EnvTestStarterPanel extends JPanel implements
        PropertyChangeListener {

    public static final String LIST_PANEL = "list";
    public static final String LOG_PANEL = "log";

    private static final String EMAIL = "m.binsteiner@auckland.ac.nz";

    private JPanel panel;
    private JButton btnNewButton;
    private TestListPanel testListPanel;
    private LogPanel logPanel;

    private final TestController testController;
    private final GridClient client;
    private JSpinner spinner;
    private JLabel lblRepeat;
    private JScrollPane scrollPane;

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
            //panel.add(getTestListPanel(), LIST_PANEL);
            panel.add(getScrollPane(), LIST_PANEL);
            panel.add(getLogPanel(), LOG_PANEL);
        }
        return panel;
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane();
            scrollPane.setViewportView(getTestListPanel());
        }
        return scrollPane;
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

                                if (testController.requiresAuthentication()) {

                                    Cred c = AbstractCred.getExistingCredential();
                                    if (c == null) {
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

                                    testController.startTests((Integer) getSpinner().getValue());

                                    File zip = testController.archiveResults();

                                    askUserWhatToDoWithResults(zip);


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

    private void askUserWhatToDoWithResults(File zip) {
        try {

            //String[] options = {"Save results", "Send results via email", "Cancel"};
            String[] options = {"Save results", "Cancel"};
            JPanel panel = new JPanel();
            panel.add(new JLabel("Tests finished, please save results and send the file to the appropriate support person."), BorderLayout.CENTER);
            int selected = JOptionPane.showOptionDialog(
                    SwingUtilities.getRootPane(EnvTestStarterPanel.this), panel, "Results", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

            switch (selected) {
                case 1:
                case JOptionPane.CLOSED_OPTION:
                    break;
                case 0:
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setSelectedFile(new File("results.zip"));
                    if (fileChooser.showSaveDialog(EnvTestStarterPanel.this) == JFileChooser.APPROVE_OPTION) {
                        File file = fileChooser.getSelectedFile();
                        Files.copy(zip, file);
                    } else {
                        askUserWhatToDoWithResults(zip);
                    }
                    break;
//                case 1:
//                    File tmp = new File(Files.createTempDir(), zip.getName());
//
//                    Files.copy(zip, tmp);
//                    Desktop desktop = Desktop.getDesktop();
//                    EmailUtils.mailto(EMAIL, "Environment-test result",
//                            "<please fill in your name and details about where you are located>", tmp.getAbsolutePath());
//
//                    break;
                default:
                    throw new RuntimeException("No valid option: "+selected);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
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

        if (oldValue != null && newValue instanceof EnvTest) {
            getLogPanel().addMessage("\n======================================================\n");
        } else if (evt.getNewValue() instanceof String) {
            getLogPanel().addMessage((String) evt.getNewValue());
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
