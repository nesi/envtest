package nz.org.nesi.envtester

import java.util.List;

import grisu.jcommons.interfaces.GrinformationManagerDozer
import grisu.jcommons.interfaces.InformationManager
import grisu.model.info.dto.FileSystem
import nz.org.nesi.envtester.iperf.IPerfRunner
import nz.org.nesi.envtester.iperf.TesterIPerfController

class IPerfTest extends ExternalCommandEnvTest {

	static main(args) {
		
		InformationManager grin = new GrinformationManagerDozer('/data/src/config/nesi-grid-info/nesi_info.groovy')
		

		for ( FileSystem fs : grin.getFileSystems() ) {
			if ( fs.getOptions().get('iperf') != null ) {
				println 'Testing fs: '+fs
				IPerfRunner iperf = new IPerfRunner("iperf -c "+fs.getHost()+" -P 1 -i 1 -p 5001 -f M -t 10", new TesterIPerfController())
				iperf.run()
				
				println 'started'
			}
		}
		
	}

	private List<String> command = []

	public IPerfTest(Map<String, String> config) {
		super(config)
		command.add('iperf')
		command.add('-c')
		command.add(getConfig(HOST_KEY))
		command.add('-P')
		command.add('1')
		command.add('-i')
		command.add('1')
		command.add('-p')
		command.add(getConfig(PORT_KEY))
		command.add('-f')
		command.add('M')
		command.add('-t')
		command.add('10')
	}

	@Override
	protected String[] requiredConfigKeys() {
		return [HOST_KEY, PORT_KEY] as String[]
	}

	@Override
	protected List<String> getCommand() {
		return command;
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
