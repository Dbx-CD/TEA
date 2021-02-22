package Server;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Is Main class, does main things.
 */
public class Main {

    private static final int PORT = 443;

    private static final String WWWfolder = "www/";
    private static final char[] password = "teapassword".toCharArray();

    public static void main(String[] args) throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {

        // before starting the server we check if a www folder exists in the same directory as the java server.
        // It should contain at least an index.html file, for the server to default to.
        // Otherwise the server will return 404 error.
        if (Files.exists(Path.of(WWWfolder))) {
            // Keystore is opened and read to get the certificate :
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream("TEAKeyStoreServer.jks"), password);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManager[] trustManagers = tmf.getTrustManagers();
            sslContext.init(kmf.getKeyManagers(), trustManagers, null);

            // Then the SSL server socket is generated :
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();

            SSLServerSocket server = (SSLServerSocket) ssf.createServerSocket(PORT);

            System.out.println("Java server is listening on port : " + "https://localhost:"+PORT);

            while (true) {
                new ClientHandler( (SSLSocket)server.accept(),  WWWfolder ).start();
            }
        } else {
            System.err.println("WWW folder not found, please create a WWW folder and insert the HTML contents in it, then retry.");
            System.exit(-1);
        }
    }
}
