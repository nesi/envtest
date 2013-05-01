package nz.org.nesi.envtester.view.swing;

import grisu.jcommons.interfaces.GrinformationManagerDozer;
import grisu.jcommons.interfaces.InformationManager;
import grith.jgrith.cred.AbstractCred;
import grith.jgrith.voms.VOManagement.VOManager;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import nz.org.nesi.envtester.DirectGridFtpTest;
import nz.org.nesi.envtester.EnvTest;
import nz.org.nesi.envtester.IPerfTest;
import nz.org.nesi.envtester.TestController;
import nz.org.nesi.envtester.TraceRouteTest;

import com.google.common.collect.Maps;

public class TestStarterFrame extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		final TestController tc = new TestController();

		Map<String, String> config = Maps.newHashMap();
				
		config.put(EnvTest.EXE_KEY, "true");
		
		config.put(EnvTest.HOST_KEY, "pan.nesi.org.nz");
		config.put(EnvTest.PORT_KEY, "5001");
		config.put(EnvTest.SOURCE_KEY, "gsiftp://pan.nesi.org.nz/home/mbin029/2timesamples.log");
		config.put(EnvTest.TARGET_KEY, "gsiftp://df.auckland.ac.nz/BeSTGRID/home/markus.binsteiner2/testfolder");
		config.put(EnvTest.SOURCE_GROUP_KEY, "/nz/nesi");
		config.put(EnvTest.TARGET_GROUP_KEY, "/none");

		config.put(EnvTest.NAME_KEY, "Traceroute test");
		tc.addTest(new TraceRouteTest(config));
		
		config.put(EnvTest.NAME_KEY, "IPerf test");
		tc.addTest(new IPerfTest(config));

		config.put(EnvTest.HOST_KEY, "ucgridserver.canterbury.ac.nz");
		config.put(EnvTest.PORT_KEY, "40000");
		config.put(EnvTest.NAME_KEY, "IPerf test");
		tc.addTest(new IPerfTest(config));
		
		config.put(EnvTest.NAME_KEY, "Gridftp transfer (3rd party - small file)");
		tc.addTest(new DirectGridFtpTest(config));
		
		config.put(EnvTest.NAME_KEY, "Gridftp transfer (3rd party - 1gb file)");
		config.put(EnvTest.SOURCE_KEY, "gsiftp://pan.nesi.org.nz/home/mbin029/bigfiles/10000000_lines.txt");
		config.put(EnvTest.EXE_KEY, "false");
		tc.addTest(new DirectGridFtpTest(config));
		
		config.put(EnvTest.TARGET_KEY, "gsiftp://df.bestgrid.org/BeSTGRID/home/markus.binsteiner2/testfolder");
		tc.addTest(new DirectGridFtpTest(config));
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					InformationManager im = new GrinformationManagerDozer(
							"/data/src/config/nesi-grid-info/nesi_info.groovy");
					AbstractCred.DEFAULT_VO_MANAGER = new VOManager(im);
					
					
					TestStarterFrame frame = new TestStarterFrame(tc);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private final TestController testController;
	private TestStarterFrame testStarterFrame;
	private EnvTestStarterPanel envTestStarterPanel;
	
	/**
	 * Create the frame.
	 */
	public TestStarterFrame(TestController tc) {
		this.testController = tc;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 727, 655);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		contentPane.add(getEnvTestStarterPanel(), BorderLayout.CENTER);
		
	}

	private EnvTestStarterPanel getEnvTestStarterPanel() {
		if (envTestStarterPanel == null) {
			envTestStarterPanel = new EnvTestStarterPanel(testController);
		}
		return envTestStarterPanel;
	}
}
