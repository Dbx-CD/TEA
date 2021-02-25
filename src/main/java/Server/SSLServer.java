package Server;


import ClientHanding.ClientHandler;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SSLServer extends BaseServer {
    private final String WWWfolder;
    private final int PORT;
    private static final char[] password = "teapassword".toCharArray();

    /**
     * Instanciating the SSLServer to be able to run it alongside the non SSL server.
     * @param PORT the port on which the server will listen
     * @param WWWFolder the folder where the files to show are located
     */
    public SSLServer(int PORT, String WWWFolder) {
        this.WWWfolder = WWWFolder;
        this.PORT = PORT;
    }

    /**
     * Starts the server thread.
     */
    public void start() {
        System.out.println("New Secure Server thread...");
        new Thread(this, "Secure Server").start();
    }

    /**
     * Loads the keystore to get the certificate and prepares to handle clients.
     */
    @Override
    public void run() {
        try {
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

            System.out.println("SSL server is listening on port : " + "https://localhost:"+PORT);

            while (true) new ClientHandler((SSLSocket)server.accept(),  WWWfolder).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
