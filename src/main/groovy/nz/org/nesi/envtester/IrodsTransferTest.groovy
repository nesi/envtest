package nz.org.nesi.envtester

import org.apache.commons.io.FilenameUtils

/**
 * Project: grisu
 *
 * Written by: Markus Binsteiner
 * Date: 24/06/13
 * Time: 10:52 AM
 */
class IrodsTransferTest extends ExternalCommandEnvTest {

    static void main(def args) {

        def config = [:]
        config.put(TARGET_DIR_KEY, "/home/markus/temp")
        config.put(SOURCE_KEY, "/BeSTGRID/home/markus.binsteiner2/46mbInput0.bin")
        config.put(COMMAND_OPTIONS_KEY, "-f")
        config.put(PATH_KEY, "/home/markus/Downloads/iRODS/clients/icommands/bin/")
        config.put(TRANSFER_MODE_KEY, "download")
        IrodsTransferTest test = new IrodsTransferTest(config)

        EnvTestResult result = test.start()
        println result.getResultsString()
        println result.getPropertiesString()

    }

    private List<String> command = []
    private String workingDirectory
    private String sourceFile

    IrodsTransferTest(Map<String, String> config) {
        super(config)

        def options = config.get(COMMAND_OPTIONS_KEY).split()
        sourceFile = config.get(SOURCE_KEY)
        workingDirectory = config.get(TARGET_DIR_KEY)
        def path = config.get(PATH_KEY)

        def transferMode = config.get(TRANSFER_MODE_KEY)
        if ( transferMode == "download") {
            command.add(path+File.separator+'iget')
        } else if ( transferMode == "uplaod" ) {
            command.add(path+File.separator+'iput')
        }
        command.add("-v")
        if ( options ) {
            command.addAll(options)
        }
        command.add(sourceFile)
        //command.add(targetFile)

    }

    @Override
    protected List<String> getCommand() {
        return command
    }

    @Override
    protected String getWorkingDirectory() {
        return workingDirectory
    }

    @Override
    protected String[] requiredConfigKeys() {
        return [TARGET_DIR_KEY, SOURCE_KEY, TRANSFER_MODE_KEY] as String[]
    }

    @Override
    boolean requiresAuthentication() {
        return false
    }

    @Override
    protected void prepareTest() {
        File targetFile = new File(workingDirectory, FilenameUtils.getName(sourceFile))
        boolean del = targetFile.delete()
        if ( del ) {
            addResultDetail("Deleted existing target: "+targetFile.getAbsolutePath())
        } else {
            addResultDetail("Target file does not exist: "+targetFile.getAbsolutePath())
        }
    }

    @Override
    protected void postTestDetails() {
    }

    @Override
    protected void postTestErrors(Exception e) {
    }
}
