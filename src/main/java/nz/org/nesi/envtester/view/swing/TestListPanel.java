package nz.org.nesi.envtester.view.swing;

import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import nz.org.nesi.envtester.EnvTest;

import com.beust.jcommander.internal.Lists;
import java.awt.GridBagLayout;

public class TestListPanel extends JPanel {

	private List<TestSelectPanel> panels = Lists.newArrayList();
	
	/**
	 * Create the panel.
	 */
	public TestListPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void addTest(EnvTest test) {

		TestSelectPanel p = new TestSelectPanel(test);
		panels.add(p);
		add(p);
	}
	
	public List<EnvTest> getTestsToRun() {
		
		List<EnvTest> tests = Lists.newArrayList();
		
		for ( TestSelectPanel p : panels ) {
			if ( p.isRunTest() ) {
				tests.add(p.getTest());
			}
		}
		
		return tests;
	}

}
