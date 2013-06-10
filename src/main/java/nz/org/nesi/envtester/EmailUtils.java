package nz.org.nesi.envtester;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

/**
 * Project: grisu
 * <p/>
 * Written by: Markus Binsteiner
 * Date: 7/06/13
 * Time: 5:09 PM
 */
public class EmailUtils {

    public static void mailto(String recipient, String subject,
                              String body, String attachment) throws Exception {
        String uriStr = String.format("mailto:%s?subject=%s&body=%s&attachment=%s",
                recipient, // use semicolon ";" for Outlook!
                urlEncode(subject),
                urlEncode(body),urlEncode(attachment));
        Desktop.getDesktop().browse(new URI(uriStr));
    }

    private static final String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String join(String sep, Iterable<?> objs) {
        StringBuilder sb = new StringBuilder();
        for (Object obj : objs) {
            if (sb.length() > 0) sb.append(sep);
            sb.append(obj);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {
        mailto("jane@example.com", "Hello!",
                "This is an automatically sent email!\n", "/home/markus/output.pdf");
    }

}
