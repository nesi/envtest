package nz.org.nesi.envtester

class DummyTest extends EnvTest {
	
	public DummyTest() {
		super([:]);
	}

	@Override
	protected void runTest() {
		 
		getResult().addLog("Test started");
		getResult().addLog("Test finished");

	}

	@Override
	protected String[] requiredConfigKeys() {
		return [] as String[];
	}


	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	@Override
	public boolean executeTestByDefault() {
		return false;
	}

	@Override
	public String getTestDescription() {
		return "n/a";
	}

	@Override
	protected void prepareTest() {
		getResult().addLog("Test prepared")
	}

	@Override
	protected void postTestDetails() {
		getResult.addDetail("Post test detail");
	}

	@Override
	protected void postTestErrors(Exception e) {
		getResult.addError("Post test error");
	}

}
