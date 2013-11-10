package nz.org.nesi.envtester
import com.github.sardine.Sardine
import com.github.sardine.SardineFactory
import org.apache.commons.io.FilenameUtils
/**
 * Project: grisu
 *
 * Written by: Markus Binsteiner
 * Date: 24/06/13
 * Time: 10:52 AM
 */
class WebDavTransferTest extends EnvTest {

    static void main(String[] args) {

        def config = [:]

        config.put(SOURCE_KEY, "https://df.bestgrid.org/BeSTGRID/home/public/transferTests/46mbInput0.bin")
        config.put(TARGET_DIR_KEY, "/home/markus/temp")
        config.put(TRANSFER_MODE_KEY, "download")

        WebDavTransferTest test = new WebDavTransferTest(config)

        EnvTestResult result = test.start()

        println result.toString()
        println result.getPropertiesString()
        println result.getErrorsString()

    }

    def source
    def target
    def targetFile
    def mode
    def bytes = 1024

    WebDavTransferTest(Map<String, String> config) {
        super(config)

        source = config.get(SOURCE_KEY)
        target = config.get(TARGET_DIR_KEY)
        targetFile = new File(target, FilenameUtils.getName(source))

        mode = config.get(TRANSFER_MODE_KEY)
        if ( config.get(BYTES_KEY) ) {
            bytes = config.get(BYTES_KEY)
        }
    }

    @Override
    protected String[] requiredConfigKeys() {
        return [SOURCE_KEY, TARGET_DIR_KEY, TRANSFER_MODE_KEY] as String[]
    }

    @Override
    boolean requiresAuthentication() {
        return false  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void prepareTest() {

        boolean del = targetFile.delete()
        if ( del ) {
            addResultDetail("Deleted existing target: "+targetFile.getAbsolutePath())
        } else {
            addResultDetail("Target file does not exist: "+targetFile.getAbsolutePath())
        }
        targetFile.delete()
    }

    @Override
    protected void runTest() {

        addResultDetail("Connecting to webdav server")
        Sardine sardine = SardineFactory.begin("markus.binsteiner2", "nixenixe25")

        addResultDetail("Starting transfer: "+source+" => "+targetFile.getAbsolutePath())
        InputStream is = sardine.get(source)

        FileOutputStream out = new FileOutputStream(targetFile)

        int read = 0
        byte[] bytes = new byte[1024]

        while ((read = is.read(bytes)) != -1) {
            out.write(bytes, 0, read)
        }

        is.close()
        out.close()

        //new FileOutputStream(targetFile).write(IOUtils.read.readFully(is, -1, false));
        addResultDetail("Transfer finished")

    }

    @Override
    protected void postTestDetails() {

    }

    @Override
    protected void postTestErrors(Exception e) {

    }
}
