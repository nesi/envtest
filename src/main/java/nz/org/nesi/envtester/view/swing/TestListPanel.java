package nz.org.nesi.envtester.view.swing;

import com.beust.jcommander.internal.Lists;
import nz.org.nesi.envtester.EnvTest;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TestListPanel extends JPanel {

	private List<TestSelectPanel> panels = Lists.newArrayList();

	/**
	 * Create the panel.
	 */
	public TestListPanel() {
        BoxLayout bl = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(bl);
	}

	public void addTest(EnvTest test) {

		TestSelectPanel p = new TestSelectPanel(test);
		panels.add(p);
		add(p);
        add(Box.createRigidArea(new Dimension(0,5)));
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
