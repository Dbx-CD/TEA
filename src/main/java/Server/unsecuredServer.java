package Server;

import ClientHanding.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;

public class unsecuredServer extends BaseServer {
    private final String WWWfolder;
    private final int PORT;

    /**
     * Instanciating the unsecuredServer to be able to run it alongside the SSLServer.
     * @param PORT the port on which the server will listen
     * @param WWWFolder the folder where the files to show are located
     */
    public unsecuredServer(int PORT, String WWWFolder) {
        this.WWWfolder = WWWFolder;
        this.PORT = PORT;
    }

    /**
     * Starts the server thread.
     */
    public void start() {
        System.out.println("New Unsecure Server thread...");
        new Thread(this, "Unsecure Server").start();
    }

    /**
     * Loads the keystore to get the certificate and prepares to handle clients.
     */
    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("Base server is running on : http://localhost:"+PORT);
            while (true) new ClientHandler(server.accept(), WWWfolder).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
