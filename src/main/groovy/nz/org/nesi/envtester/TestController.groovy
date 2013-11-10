package nz.org.nesi.envtester

import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.google.common.eventbus.EventBus
import grisu.jcommons.configuration.CommonGridProperties
import grisu.jcommons.constants.GridEnvironment
import grisu.jcommons.git.GitRepoUpdater
import grith.jgrith.cred.Cred
import nz.org.nesi.envtester.view.cli.CliTestListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class TestController {

	static final Logger log = LoggerFactory.getLogger(TestController.class);

    static main(def args) {


		CliTestListener l = new CliTestListener()

		TestController tc = new TestController()
		tc.addTestPropertyChangeListener(l)
		tc.addPropertyChangeListener(l)
		tc.startTests()

		File file = tc.archiveResults()

		println file.getAbsolutePath()
	}

	public static EventBus eventBus = new EventBus("envTest")

    public static final String TEST_CONFIG_KEY = "ENVTEST_CONFIG"

	private final List<EnvTest> tests = Lists.newArrayList()

	private final List<EnvTestResult> results = Lists.newArrayList()

	private Cred cred = null

	private EnvTest currentTest = null

	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this)

	private Set<PropertyChangeListener> testListeners = Sets.newLinkedHashSet()

	private final EnvDetails envdetails = new EnvDetails();

	public TestController(String pathToConfig) {

		def config = null

		try {
			if ( ! pathToConfig ) {
                pathToConfig = CommonGridProperties.getDefault().getOtherGridProperty(TEST_CONFIG_KEY)
                if ( ! pathToConfig ) {
                    pathToConfig = 'https://raw.github.com/nesi/nesi-grid-info/master/env_test_config.groovy'
                }
			}

			if ( pathToConfig.startsWith('http') ) {
				log.debug 'Retrieving remote config from "'+pathToConfig+'"...'
				config = new ConfigSlurper().parse(new URL(pathToConfig))
			} else if ( pathToConfig.startsWith("git:")) {
                File file = GitRepoUpdater.ensureUpdated(pathToConfig)
                config = new ConfigSlurper().parse(file.toURL())
            } else {
				log.debug 'Using local config from "'+pathToConfig+'"...'
				config = new ConfigSlurper().parse(new File(pathToConfig).toURL())
			}

			List<EnvTest> testsTemp = Lists.newArrayList()

			for (def e in config) {

				def name = e.key
				def object = e.value

                if ( isCollectionOrArray(object) ) {
                    object.each {
                        if ( it instanceof EnvTest ) {
                            testsTemp.add(it)
                        }
                    }
                } else if (object instanceof EnvTest ) {
                    testsTemp.add(object)
				} else {
                    println 'Not test: '+object.toString()
                    continue
                }
			}

			addTests(testsTemp)

		} catch (all) {
			log.error("Can't build tests, probably test config broken: "+all.getLocalizedMessage(), all)
			throw new RuntimeException("Can't create tests: "+all.getLocalizedMessage())
		}


	}

    public static boolean isCollectionOrArray(object) {
    [Collection, Object[]].any { it.isAssignableFrom(object.getClass()) }
}

	public TestController() {
		this((String)null)
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

	public void addTestPropertyChangeListener(PropertyChangeListener l) {
		testListeners.add(l)
		tests.each {
			it.addPropertyChangeListener(l)
		}
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
		for ( PropertyChangeListener l : testListeners ) {
			t.addPropertyChangeListener(l)
		}
	}

	public EnvTest getCurrentTest() {
		return currentTest
	}

	public void setAuthentication(Cred cred) {
		this.cred = cred
		eventBus.post(cred)
	}

	public File startTests(int repeat) {

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

	public File startTests() {

		return startTests(1)
	}

	public List<EnvTest> getEnabledTests() {

		return getTests().findAll {
			it.isEnabled()
			//			println it.getTestName()+": "+it.isEnabled()
		}
	}

	public File archiveResults() {

		if ( ! tests ) {
			return null
		}

		File dir = GridEnvironment.getGridConfigDirectory()
		String username = System.getProperty("user.name")
		File zip = new File(dir, username+"_env_tests.zip")
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

//		File temp = new File(dir, "testrun_"+)

		zipFile.close()

		return zip
	}

	public boolean requiresAuthentication() {

		return getEnabledTests().any { it ->
			it.requiresAuthentication()
		}
	}

}
