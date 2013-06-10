package nz.org.nesi;

import com.beust.jcommander.Parameter;
import grith.jgrith.cred.GridCliParameters;

public class ExampleCliParameters extends GridCliParameters {

	@Parameter(names = { "-f", "--file" }, description = "the path to a file")
	private String file;

	public String getFile() {
		return file;
	}

}
