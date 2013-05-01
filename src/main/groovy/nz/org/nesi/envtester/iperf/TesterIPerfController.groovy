package nz.org.nesi.envtester.iperf

import net.nlanr.jperf.core.Measurement
import net.nlanr.jperf.ui.IPerfController
import nz.org.nesi.envtester.EnvTestResult

class TesterIPerfController implements IPerfController {
	
	private final EnvTestResult result;
	

	public TesterIPerfController(EnvTestResult result) {
		this.result = result
	}

	@Override
	public void setStartedStatus() {
		
		result.addLog('Iperf started')
		
	}

	@Override
	public void logMessage(String input_line) {

		result.addDetail(input_line)
				
	}

	@Override
	public void setEnabled(boolean b) {
	
		//
		
	}

	@Override
	public void setStoppedStatus() {
	
		result.addLog('Iperf finished')
		
	}

	@Override
	public void addNewStreamBandwidthMeasurement(int id, Measurement m) {

//		result.addDetail 'measurement: '+id+' : '+m.toString()
		
		
	}

	@Override
	public void addNewStreamBandwidthAndJitterMeasurement(int id,
			Measurement b, Measurement j) {

//			result.addDetail 'measurement (jitter): '+id+' : '+b.toString() + ' : ' + j.toString()
		
	}



}
