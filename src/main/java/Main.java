import Server.SSLServer;
import Server.unsecuredServer;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Is Main class, does main things.
 */
public class Main {

    private static final int HTTPS_PORT = 443;
    private static final int HTTP_PORT = 80;
    private static final String WWWfolder = "www/";

    public static void main(String[] args) {
        // before starting the server we check if a www folder exists in the same directory as the java server.
        // It should contain at least an index.html file, for the server to default to.
        // Otherwise the server will return 404 error.
        if (Files.exists(Path.of(WWWfolder))) {
            SSLServer securedServer = new SSLServer(HTTPS_PORT, WWWfolder);
            unsecuredServer unsecuredServer = new unsecuredServer(HTTP_PORT, WWWfolder);

            securedServer.start();
            unsecuredServer.start();
        } else {
            System.err.println("WWW folder not found, please create a WWW folder and insert the HTML contents in it, then retry.");
            System.exit(-1);
        }
    }
}
