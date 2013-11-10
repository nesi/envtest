package nz.org.nesi.envtester;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class EnvTestResult {

	private final Map<Date, String> log = Collections.synchronizedMap(new TreeMap<Date, String>());
	private String result = "n/a";
	private final List<String> details = Lists.newArrayList();
	private final List<String> errors = Lists.newArrayList();
	private boolean failed = false;
	private final List<File> files = Collections.synchronizedList(new ArrayList<File>());
	private Exception exception;
	private Date started;
	private Date finished;

	private final String testrunId;

	private final EnvTest test;

	public EnvTestResult(EnvTest test, String testrun_id) {
		this.test = test;
		this.testrunId = testrun_id;
	}

	public String getTestRunId() {
		return testrunId;
	}

	public void addFile(File file) {
		files.add(file);
	}

	public String getTestName() {
		return test.getTestName();
	}

	public String getTestDescription() {
		return test.getTestDescription();
	}

	public void addLog(String msg) {
		log.put(new Date(), msg);
	}

	public synchronized void addDetail(String detail) {
		details.add(detail);
	}

	public synchronized void addError(String err) {
		errors.add(err);
	}

	public void setException(Exception e) {
		this.exception = e;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public List<String> getErrors() {
		return this.errors;
	}

	public String getResult() {
		return result;
	}

	public List<String> getDetails() {
		return details;
	}

	public Map<Date, String> getLog() {
		return log;
	}

	public void setFailed() {
		this.failed = true;
	}

	public boolean isFailed() {
		return this.failed;
	}

	private Map<String, String> createPropertiesMap() {
		Map<String, String> map = Maps.newLinkedHashMap();

		map.put(EnvTest.NAME_KEY, test.getTestName());
		map.put(EnvTest.DESC_KEY, test.getTestDescription());
		map.put(EnvTest.TEST_ID_KEY, test.testId);
		map.put(EnvTest.DURATION_KEY, getDuration().toString());
		map.put(EnvTest.TESTRUN_ID_KEY, testrunId);
		if ( started != null ) {
			map.put(EnvTest.STARTED_KEY, Long.toString(started.getTime()));
		}
		if ( finished != null ) {
			map.put(EnvTest.FINISHED_KEY, Long.toString(finished.getTime()));
		}

		map.put(EnvTest.RESULT_KEY, getResult());

		for ( String key : test.getConfig().keySet() ) {
			if ( EnvTest.NAME_KEY.equals(key)
					|| EnvTest.DESC_KEY.equals(key) ) {
				continue;
			}

			map.put(key, test.getConfig(key));
		}


		return map;
	}


    public String getResultsString() {
        // results file
        StringBuffer temp = new StringBuffer("Test: "+getTestName()+"\n\n");
        temp.append("Description: "+getTestDescription()+"\n\n");
        temp.append("Result: "+getResult()+"\n\n");
        temp.append("Log:\n");

        for ( Date d : log.keySet() ) {
            temp.append(d.toString()+":\t"+ log.get(d)+"\n");
        }
        temp.append("\nDetails:\n");
        for ( String l : details ) {
            temp.append(l+"\n");
        }

        if ( getErrors().size() > 0 ) {
            temp.append("\n\nErrors:\n\n");
            for ( String e : errors ) {
                temp.append(e+"\n");
            }
        }

        temp.append("\n");
        return temp.toString();
    }

    public String getPropertiesString() {
        StringBuffer temp = new StringBuffer();
        Map<String, String> map = createPropertiesMap();
        for ( String key : map.keySet() ) {
            temp.append(key+" = "+map.get(key)+"\n");
        }
        return temp.toString();
    }

    public String getDetailsString() {
        StringBuffer temp = new StringBuffer();
        for ( String detail : getDetails() ) {
            temp.append(detail+"\n");
        }
        return temp.toString();
    }

    public String getErrorsString() {
        StringBuffer temp = new StringBuffer();
        for ( String error : getErrors() ) {
            temp.append(error+"\n");
        }
        return temp.toString();
    }

	public List<File> getTestFiles() throws IOException {

		File dir = Files.createTempDir();

		List<File> files = Lists.newArrayList();

        String temp = getResultsString();
		File resultsFile = new File(dir, "results.test");
		Files.write(temp, resultsFile, Charsets.UTF_8);
		files.add(resultsFile);

		temp = getPropertiesString();

		File propFile = new File(dir, "properties.test");
		Files.write(temp, propFile, Charsets.UTF_8);
		files.add(propFile);

		temp = getDetailsString();

		File detailsFile = new File(dir, "details.test");
		Files.write(temp, detailsFile, Charsets.UTF_8);
		files.add(detailsFile);

		if ( errors.size() > 0 ) {
			temp = getErrorsString();
			File errorFile = new File(dir, "errors.test");
			Files.write(temp, errorFile, Charsets.UTF_8);
			files.add(errorFile);
		}
		if ( exception != null ) {
			String exc = ExceptionUtils.getStackTrace(exception);
			File exceptionFile = new File(dir, "exception.test");
			Files.write(exc, exceptionFile, Charsets.UTF_8);
			files.add(exceptionFile);
		}

		EnvDetails d = test.getEnvDetails();
		if ( d != null ) {
			String envdetails = d.createReport();
			File envDetailsFile = new File(dir, "environment.test");
			Files.write(envdetails, envDetailsFile, Charsets.UTF_8);
			files.add(envDetailsFile);
		}

		List<File> additionalFiles = test.getAdditionalFiles();
		if ( additionalFiles != null ) {
			files.addAll(additionalFiles);
		}




		return files;
		// creating the zip file


	}

    public String toString() {
        return getResultsString();
    }

	public EnvTest getTest() {
		return test;
	}

	public Date getStarted() {
		return started;
	}

	public Long getDuration() {
		if ( finished == null || started == null ) {
			return -1L;
		} else {
			return finished.getTime() - started.getTime();
		}
	}

	public void setStarted() {

		started = new Date();

	}

	public void setFinished() {
		finished = new Date();
	}
}
