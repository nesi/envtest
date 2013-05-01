package nz.org.nesi.envtester

import grith.jgrith.cred.Cred

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import nz.org.nesi.envtester.view.cli.CliTestListener

import org.apache.commons.lang3.exception.ExceptionUtils

import com.google.common.base.Charsets
import com.google.common.collect.Lists
import com.google.common.eventbus.EventBus
import com.google.common.io.Files


class TestController {

	static main(def args) {

		def config = new ConfigSlurper().parse(new File('/data/src/grisu/envtest/test.config').toURL())

		List<EnvTest> tests = Lists.newArrayList()

		CliTestListener l = new CliTestListener()

		for (def e in config) {

			def name = e.key
			def object = e.value

			if ( ! (object instanceof EnvTest) ) {
				println 'Not test: '+object.toString()
				continue
			}
			object.addPropertyChangeListener(l)
			tests.add(object)
		}


		println 'tests: '+tests

		TestController tc = new TestController(tests)
		tc.addPropertyChangeListener(new CliTestListener())
		tc.startTests()

		File file = tc.createZipFile()

		println file.getAbsolutePath()
	}

	public static EventBus eventBus = new EventBus("envTest")

	private final List<EnvTest> tests = Lists.newArrayList()

	private final List<EnvTestResult> results = Lists.newArrayList()

	private Cred cred = null

	private EnvTest currentTest = null

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this)

	private final EnvDetails envdetails = new EnvDetails();

	public TestController(String pathToConfig) {

		if ( ! pathToConfig ) {
			pathToConfig = ''
		}

		def config = new ConfigSlurper().parse(pathToConfig).toURL()

		List<EnvTest> tests = Lists.newArrayList()

		CliTestListener l = new CliTestListener()

		for (def e in config) {

			def name = e.key
			def object = e.value

			if ( ! (object instanceof EnvTest) ) {
				println 'Not test: '+object.toString()
				continue
			}
			object.addPropertyChangeListener(l)
			tests.add(object)
		}


		println 'tests: '+tests
	}

	public TestController() {
		this(null)
	}

	public TestController(List<EnvTest> tests) {
		addTests(tests)
	}

	public void addTests(Collection<EnvTest> tests) {
		tests.each { addTest(it) }
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l)
	}

	public List<EnvTest> getTests() {
		return tests
	}

	public void addTest(EnvTest t) {
		tests.add(t)
		t.setEnvDetails(envdetails)
		eventBus.register(t)
		if ( cred ) {
			t.setCredential(cred)
		}
	}

	public EnvTest getCurrentTest() {
		return currentTest
	}

	public void setAuthentication(Cred cred) {
		this.cred = cred
		eventBus.post(cred)
	}

	public void startTests(int repeat) {

		Long testrun_id = new Date().getTime()

		for ( i in 1..repeat ) {
			for ( EnvTest test : getEnabledTests() ) {
				EnvTest old = currentTest
				currentTest = test
				pcs.firePropertyChange("currentTest", old, currentTest)
				EnvTestResult result = test.start(testrun_id.toString())
				results.add(result)
			}
		}
	}

	public void startTests() {

		startTests(1)
	}

	public List<EnvTest> getEnabledTests() {

		return getTests().findAll {
			it.isEnabled()
			//			println it.getTestName()+": "+it.isEnabled()
		}
	}

	public File createZipFile() {

		File dir = Files.createTempDir()
		String username = System.getProperty("user.name")
		File zip = new File(dir, username+"_results.zip")
		ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zip))

		def buffer = new byte[1024]
		for ( EnvTestResult r : results ) {
			List<File> files = r.getTestFiles()
			files.each  { file ->
				String started = new Date().getTime() + "_not_started"
				if ( r.getStarted() != null ) {
					started = r.getStarted().getTime()
				}
				zipFile.putNextEntry(new ZipEntry(r.getTest().getClass().getSimpleName()+"/"+r.getTest().getTestName()+"/"+started+"/"+file.getName()))
				file.withInputStream { i ->
					def l = i.read(buffer)
					// check wether the file is empty
					if (l > 0) {
						zipFile.write(buffer, 0, l)
					}
				}
				zipFile.closeEntry()
			}
		}

		zipFile.close()

		return zip
	}

	public boolean requiresAuthentication() {

		return getEnabledTests().any { it ->
			it.requiresAuthentication()
		}
	}

}
