package nz.org.nesi.envtester.view.cli;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import nz.org.nesi.envtester.EnvTest;

public class CliTestListener implements PropertyChangeListener {

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		Object oldValue = evt.getOldValue();
		Object newValue = evt.getNewValue();
		
		if ( oldValue != null && newValue instanceof EnvTest ) {
			System.out.print("\n======================================================\n");
		} else if ( evt.getNewValue() instanceof String ){
			System.out.println((String)evt.getNewValue());
		}

	}

}
