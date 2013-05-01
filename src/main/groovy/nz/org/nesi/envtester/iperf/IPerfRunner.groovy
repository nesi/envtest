/**
 * - 02/2008: Class created by Nicolas Richasse
 *
 * Changelog:
 * 	- class created
 *
 * To do:
 * 	- ...
 */

package nz.org.nesi.envtester.iperf

import java.util.regex.Pattern

import net.nlanr.jperf.core.JperfStreamResult
import net.nlanr.jperf.core.Measurement
import nz.org.nesi.envtester.EnvTestResult

class IPerfRunner {

	private TesterIPerfController controller;
	private String command;
	private Process process;

	private BufferedReader input;
	private BufferedReader errors;

	private Vector<JperfStreamResult>	finalResults;

	public IPerfRunner(String command, EnvTestResult result) {

		controller = new TesterIPerfController(result)

		this.command = command;
		this.controller = controller;
		this.finalResults = new Vector<JperfStreamResult>();
		this.controller.logMessage(command);
	}



	public int run() {
		try {
			controller.setStartedStatus();

			process = command.execute()

			process.consumeProcessOutputStream(System.out)
			process.consumeProcessErrorStream(System.err)


			process.waitFor()

			int exitcode = process.exitValue()

			controller.logMessage("Done. ( Exit code: "+exitcode+" )")

			return exitcode

		}
		catch (Exception e)
		{
			controller.logMessage("\nIperf thread stopped [CAUSE=" + e.getMessage() + "]");
		}

	}
	public void parseLine(String line)
	{
		// only want the actual output lines
		if (line.matches("\\[[ \\d]+\\]\\s*[\\d]+.*"))
		{
			Pattern p = Pattern.compile("[-\\[\\]\\s]+");
			// ok now break up the line into id#, interval, amount transfered, format
			// transferred, bandwidth, and format of bandwidth
			String[] results = p.split(line);

			// get the ID # for the stream
			Integer temp = new Integer(results[1].trim());
			int id = temp.intValue();

			boolean found = false;
			JperfStreamResult streamResult = new JperfStreamResult(id);
			for (int i = 0; i < finalResults.size(); ++i)
			{
				if ((finalResults.elementAt(i)).getID() == id)
				{
					streamResult = finalResults.elementAt(i);
					found = true;
					break;
				}
			}

			if (!found)
			{
				finalResults.add(streamResult);
			}
			// this is TCP or Client UDP
			if (results.length == 9)
			{
				Double start = new Double(results[2].trim());
				Double end = new Double(results[3].trim());
				Double bw = new Double(results[7].trim());

				Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[8]);
				streamResult.addBW(M);
				controller.addNewStreamBandwidthMeasurement(id, M);
			}
			else if (results.length == 14)
			{
				Double start = new Double(results[2].trim());
				Double end = new Double(results[3].trim());
				Double bw = new Double(results[7].trim());

				Measurement B = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
				streamResult.addBW(B);

				Double jitter = new Double(results[9].trim());
				Measurement J = new Measurement(start.doubleValue(), end.doubleValue(), jitter.doubleValue(), results[10]);
				streamResult.addJitter(J);
				controller.addNewStreamBandwidthAndJitterMeasurement(id, B, J);
			}
		}
	}


}
