package nz.org.nesi.envtester

import grisu.jcommons.utils.MemoryUtils
import grith.jgrith.cred.Cred
import nz.org.nesi.envtester.gsiftp.Transfer

import org.apache.commons.io.FileUtils
import org.apache.commons.vfs2.FileObject

class DirectGridFtpTest extends EnvTest {
	
	private long filesize;
	private FileObject source;
	private FileObject target;

	public DirectGridFtpTest(Map<String, String> config) {
		super(config)
	}
	
	@Override
	protected void runTest() {

		addLogMessage("Starting transfer...")
		Transfer.copy(source, target)
		addLogMessage("Transfer finished.")


	}
	
	protected void postTestDetails() {
		
		long time = getResult().getDuration()
		addResultDetail("Duration: "+time+" ms")
		long speed = filesize/(time/1000)
		addResultDetail("Speed: "+FileUtils.byteCountToDisplaySize(speed)+"/s")
		
		setResultMessage("Transfer successful")

	}
	
	protected void postTestErrors(Exception e) {
		setResultMessage("Transfer failed: "+e.getLocalizedMessage())
	}

	@Override
	protected String[] requiredConfigKeys() {

		return [SOURCE_KEY, SOURCE_GROUP_KEY, TARGET_KEY, TARGET_GROUP_KEY] as String[]
	}

	@Override
	public boolean requiresAuthentication() {
		return true;
	}


	@Override
	public String getTestDescription() {
		return "Transfer: "+getConfig(SOURCE_KEY)+" -> "+getConfig(TARGET_KEY)
	}

	@Override
	protected void prepareTest() {
		// TODO Auto-generated method stub
		Cred source_cred = getCredential().getGroupCredential(getConfig(SOURCE_GROUP_KEY))
		Cred target_cred = getCredential().getGroupCredential(getConfig(TARGET_GROUP_KEY))
		
		source = Transfer.resolveFile(source_cred, getConfig(SOURCE_KEY))
		target = Transfer.resolveFile(target_cred, getConfig(TARGET_KEY))
		
		filesize = source.getContent().getSize()
		addResultDetail("Source: "+source.getURL().toString())
		addResultDetail("Target: "+target.getURL().toString())
		addResultDetail("Filesize: "+FileUtils.byteCountToDisplaySize(filesize) + " ( "+filesize+" bytes)")
	}

}
