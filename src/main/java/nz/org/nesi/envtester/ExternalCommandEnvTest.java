package nz.org.nesi.envtester;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * Project: grisu
 * <p/>
 * Written by: Markus Binsteiner
 * Date: 12/06/13
 * Time: 4:53 PM
 */
abstract class ExternalCommandEnvTest extends EnvTest {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0 );

    }

    public static boolean isSolaris() {

        return (OS.indexOf("sunos") >= 0);

    }

    public ExternalCommandEnvTest(Map<String, String> config) {
        super(config);
    }

    abstract protected List<String> getCommand();

    @Override
    protected void runTest() {


        Process proc = null;
        ProcessBuilder pb = new ProcessBuilder(getCommand());

        //proc = getCommand().execute();

        InputStream is = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        final BufferedReader br = new BufferedReader(isr);
        new Thread() {
            public void run() {
                String line;
                try {
                    while ((line = br.readLine()) != null) {
                        addResultDetail(line);
                    }
                } catch (IOException e) {
                    myLogger.debug(e.getLocalizedMessage());
                }
            }
        }.start();
        InputStream es = proc.getErrorStream();
        InputStreamReader esr = new InputStreamReader(es);
        final BufferedReader ebr = new BufferedReader(esr);
        new Thread() {
            public void run() {
                String line;
                try {
                    while ((line = ebr.readLine()) != null) {
                        addErrorMessage(line);
                    }
                } catch (IOException e) {
                    myLogger.debug(e.getLocalizedMessage());
                }
            }
        }.start();

        try {
            proc.waitFor();

            // wait in case stdout/err buffers are not empty yet
            Thread.sleep(500);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String cmd = StringUtils.join(getCommand(), " ");

        if ( proc.exitValue() == 0 ) {
            setResultMessage("Command "+cmd+" finished");
        } else {
            setResultMessage("Command "+cmd+" failed");
            setFailed();
        }
    }

    @Override
    public String getTestDescription() {
        String cmd = StringUtils.join(getCommand(), " ");
        return "Executing command: '"+cmd+"'";
    }

}
