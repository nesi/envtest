package nz.org.nesi.envtester

import org.apache.commons.lang.SystemUtils

class TraceRouteTest extends ExternalCommandEnvTest {


	public TraceRouteTest(Map<String, String> config) {
		super(config)
	}

	@Override
	protected List<String> getCommand() {
		def command = []

		if ( SystemUtils.IS_OS_WINDOWS ) {
			command.add('tracert')
		} else if ( SystemUtils.IS_OS_MAC_OSX ) {
            command.add('traceroute')
        } else {
			try {
				def proc = 'traceroute --help'.execute()
				command.add('tracepath')
			} catch (IOException e) {
				command.add('traceroute')
			}
		}

		command.add(getConfig(HOST_KEY))

		return command

	}

	@Override
	protected String[] requiredConfigKeys() {
		return [ HOST_KEY ] as String[]
	}

	@Override
	public boolean requiresAuthentication() {
		return false;
	}

	@Override
	protected void prepareTest() {

	}

	@Override
	protected void postTestDetails() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void postTestErrors(Exception e) {
		// TODO Auto-generated method stub

	}





}
