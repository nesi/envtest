package nz.org.nesi.envtester

import java.awt.image.ImagingOpException;

abstract class ExternalCommandEnvTest extends EnvTest {

	public ExternalCommandEnvTest(Map<String, String> config) {
		super(config)
	}

	abstract protected List<String> getCommand()

	@Override
	protected void runTest() {

		def proc = null

		proc = getCommand().execute()


		InputStream is = proc.getInputStream()
		InputStreamReader isr = new InputStreamReader(is)
		final BufferedReader br = new BufferedReader(isr)
		new Thread() {
					public void run() {
						String line
						try {
							while ((line = br.readLine()) != null) {
								addResultDetail(line)
							}
						} catch (IOException e) {
							myLogger.debug(e.getLocalizedMessage())
						}
					}
				}.start()
		InputStream es = proc.getErrorStream()
		InputStreamReader esr = new InputStreamReader(es)
		final BufferedReader ebr = new BufferedReader(esr)
		new Thread() {
					public void run() {
						String line;
						try {
							while ((line = ebr.readLine()) != null) {
								addErrorMessage(line)
							}
						} catch (IOException e) {
							myLogger.debug(e.getLocalizedMessage())
						}
					}
				}.start()


			proc.waitFor()
			
			// wait in case stdout/err buffers are not empty yet
			Thread.sleep(500);
			
			if ( proc.exitValue() == 0 ) {
				setResultMessage("Command "+getCommand()[0]+" successful")
			} else {
				setResultMessage("Command "+getCommand()[0]+" failed")
				setFailed()
			}
		}

	@Override
	public String getTestDescription() {
		return "Executing command: '"+getCommand().join(' ')+"'";
	}
	}
