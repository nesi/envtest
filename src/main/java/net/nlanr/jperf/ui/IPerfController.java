package net.nlanr.jperf.ui;

import net.nlanr.jperf.core.Measurement;

public interface IPerfController {

	void setStartedStatus();

	void logMessage(String input_line);

	void setEnabled(boolean b);

	void setStoppedStatus();

	void addNewStreamBandwidthMeasurement(int id, Measurement m);

	void addNewStreamBandwidthAndJitterMeasurement(int id, Measurement b,
			Measurement j);

}
