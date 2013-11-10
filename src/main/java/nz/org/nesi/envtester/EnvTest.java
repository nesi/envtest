package nz.org.nesi.envtester;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import grith.jgrith.cred.Cred;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class EnvTest {

	public static final Logger myLogger = LoggerFactory
			.getLogger(EnvTest.class);

	final public static String NAME_KEY = "name";
	final public static String TEST_ID_KEY = "testId";
	final public static String DESC_KEY = "desc";
	final public static String EXE_KEY = "execute";
	final public static String HOST_KEY = "host";
	final public static String PORT_KEY = "port";
	final public static String SOURCE_KEY = "source";
	final public static String SOURCE_GROUP_KEY = "source-group";
    final public static String TARGET_KEY = "target";
    final public static String TARGET_DIR_KEY = "target-dir";
	final public static String TARGET_GROUP_KEY = "target-group";
    final public static String COMMAND_OPTIONS_KEY = "command-options";
    final public static String PATH_KEY = "path";
    final public static String TRANSFER_MODE_KEY = "transfer-mode";
    final public static String BYTES_KEY = "bytes";

    final public static String WORKING_DIRECTORY = "working-directory";

	final public static String DURATION_KEY = "duration";
	final public static String STARTED_KEY = "started";
	final public static String FINISHED_KEY = "finished";
	final public static String RESULT_KEY = "result";
	final public static String TESTRUN_ID_KEY = "testrun";


	final public ImmutableMap<String, String> config;

	protected EnvTestResult currentResult;

	protected boolean isEnabled = true;

	private Cred credential = null;

	private EnvDetails envDetails = null;

	public final String testId = UUID.randomUUID().toString();

	final PropertyChangeSupport pcs = new PropertyChangeSupport(this);


	public EnvTest(Map<String, String> config) {

        if ( config == null ) {
            throw new RuntimeException("No config for test: "+getClass().getSimpleName());
        }

		this.config = ImmutableMap.copyOf(config);
		if (requiredConfigKeys() != null) {
			for (String key : requiredConfigKeys()) {
				if (!config.keySet().contains(key)) {
					throw new RuntimeException("Config for "
							+ getClass().getSimpleName()
							+ " does not contain key: " + key);
				}
				if (StringUtils.isBlank(config.get(key))) {
					throw new RuntimeException("Config for "
							+ getClass().getSimpleName()
							+ " has empty value for: " + key);
				}
			}
		}

		isEnabled = executeTestByDefault();
	}

	public void addErrorMessage(String e) {
		getResult().addError(e);
		pcs.firePropertyChange("Error", null, e);
	}

	public synchronized void addLogMessage(String msg) {
		getResult().addLog(msg);
		pcs.firePropertyChange("Log", null, msg);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public void setEnvDetails(EnvDetails d) {
		this.envDetails = d;
	}

	public EnvDetails getEnvDetails() {
		return this.envDetails;
	}

	public void addResultDetail(String d) {
		getResult().addDetail(d);
		pcs.firePropertyChange("Detail", null, d);
	}

	public boolean executeTestByDefault() {
		if (Boolean.parseBoolean(getConfig(EXE_KEY))) {
			return true;
		} else {
			return false;
		}
	}

	public Map<String, String> getConfig() {
		return config;
	}

	public String getConfig(String key) {
		return config.get(key);
	}

	public Cred getCredential() {
		return this.credential;
	}

	public EnvTestResult getResult() {
		return currentResult;
	}

	/**
	 * Override this if necessary.
	 *
	 * @return the description
	 */
	public String getTestDescription() {
		return getConfig(DESC_KEY);
	}

	public String getTestName() {
		if (StringUtils.isBlank(getConfig(NAME_KEY))) {
			return getClass().getSimpleName();
		} else {
			return getConfig(NAME_KEY);
		}

	}

	public boolean isEnabled() {
		return isEnabled;
	}

	abstract protected String[] requiredConfigKeys();


	abstract public boolean requiresAuthentication();

	abstract protected void prepareTest();
	abstract protected void runTest();

	abstract protected void postTestDetails();


	abstract protected void postTestErrors(Exception e);

	@Subscribe
	public void setCredential(Cred cred) {
		this.credential = cred;
	}

	public void setEnabled(boolean enabled) {
		this.isEnabled = enabled;
	}

	public void setFailed() {
		getResult().setFailed();
		pcs.firePropertyChange("Failed", null, true);
	}

	public void setResultMessage(String msg) {
		getResult().setResult(msg);
		pcs.firePropertyChange("Result", null, msg);
	}

	public List<File> getAdditionalFiles() {
		return null;
	}

	public EnvTestResult start(String testrun_id) {

		currentResult = new EnvTestResult(this, testrun_id);

		addLogMessage("Preparing test: "+getTestName());
        addLogMessage("\t"+getTestDescription());
		try {
			prepareTest();
			addLogMessage("Starting test: " + getTestName());
			getResult().setStarted();
			runTest();
			getResult().setFinished();
			addLogMessage("Test duration: "+getResult().getDuration() + " ms");
			postTestDetails();
		} catch (Exception e) {
			getResult().setFinished();
			getResult().setException(e);
			addLogMessage("Test duration: "+getResult().getDuration() + " ms");
			postTestErrors(e);
			getResult().setFailed();
			addLogMessage("Test failed: "+e.getLocalizedMessage());
		}

		addLogMessage("Test finished: " + getTestName());

		return currentResult;
	}

	public String toString() {
		return getTestName();
	}

}
