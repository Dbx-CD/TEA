package ClientHanding;

import IOHandling.FileHandler;
import IOHandling.InputHandler;
import IOHandling.OutputHandler;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ClientHandler implements Runnable {

    private final String ThreadName;
    private final SSLSocket client;
    private final String wwwPath;

    private final Socket unsecuredClient;

    private String clientQuery;

    private InputHandler in;
    private OutputHandler out;

    /**
     * Constructor for the client
     * @param client The socket generated at each connection of a client.
     * @param wwwPath the path to the 'www' folder, used to open files that are into that folder.
     */
    public ClientHandler(SSLSocket client, String wwwPath) {
        this.client = client;
        this.unsecuredClient = null;
        ThreadName = "Generic_Client";
        this.wwwPath = wwwPath;
    }

    public ClientHandler(Socket client, String wwwPath) {
        this.unsecuredClient = client;
        this.client = null;
        this.wwwPath = wwwPath;
        ThreadName = "Unsecured_Client";
    }

    /**
     * This method creates a thread for each client, this allows multiple clients to connect and use their
     * own run() method.
     */
    public void start() {
        System.out.println("Accepting client...");
        System.out.println("Creating Thread...");
        new Thread(this, ThreadName).start();
    }

    /**
     * Run method that contains most of the server's logic to determine if the client tries to send a GET or a POST request.
     */
    @Override
    public void run() {
        try {
            // Once the thread is created, we start the handshake.
            // If the server is not a secured server we redirect the client to the secured url.
            if (this.client != null) {
                // Create the Input and Output.
                this.client.startHandshake();
                in = new InputHandler(this.client.getInputStream());
                out = new OutputHandler(this.client.getOutputStream());
            } else {
                assert this.unsecuredClient != null;
                in = new InputHandler(this.unsecuredClient.getInputStream());
                out = new OutputHandler(this.unsecuredClient.getOutputStream());
                RedirectToHTTPS();
            }

            // And read the first line of the request.
            String startLine = in.readNextLine();

            // If the first line contains information, we extract it.
            if (startLine != null) {
                clientQuery = startLine + "\r\n" + new String(in.read()).trim();
                System.out.println("New request : " + clientQuery);
            } else {
                // Otherwise, if the request is skewed we throw a 400 Error.
                INVALIDRequest();
                closeLink();
            }
            // StringTokenizer is used to divide the header string into substrings delimited by a space.
            assert startLine != null;
            StringTokenizer tokenizer = new StringTokenizer(startLine, " ");
            // We can then extract the protocol, target path, and type of the request.
            String request = tokenizer.nextToken(), path = "", type = "";
            if (tokenizer.hasMoreTokens()) {path = tokenizer.nextToken();}
            if (tokenizer.hasMoreTokens()) {type = tokenizer.nextToken();}

            // If the request's type or protocol isn't equal to the permitted options, we throw a 400 error.
            if (!type.equalsIgnoreCase("HTTP/1.1")) {
                INVALIDRequest();
            } else if (request.equalsIgnoreCase("GET")) {
                GETRequest(path);
            } else if (request.equalsIgnoreCase("POST")) {
                POSTRequest(path);
            } else {
                INVALIDRequest();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When the run() method has determined that the client is trying to GET a page or file, this method is used
     * It receives a 'path' which contains the name of the page that the client is trying to access.
     * Depending on the route, the method will either run a 'CGI' program or try to open the file specified in the route.
     * If the file is found, the method calls WriteToClient to send the contents of the file back to the client.
     * If it is not found, it will call WriteToClient to sent a 404 error back to the client.
     * @param path the route of the file the client is trying to access.
     * @throws IOException This method potentially throws an IO Exception if the WriteToClient method encounters an IOException itself.
     */
    private void GETRequest (String path) throws IOException {
        if (path.equalsIgnoreCase("/")) path = "/index.html";
        if (path.equalsIgnoreCase("/executeping")) {
            // If the queried route is equal to "/executeping" then the server executes the command and returns the result
            // in HTML form.
            System.out.println("CGI Ping detected.");
            String ping = doSomePinging();
            WriteToClient("200 OK", "text/html", ping.getBytes(), null);

        } else {
            System.out.println("Searching for file : " + wwwPath+path.substring(1));

            File file = new File(wwwPath+path.substring(1));

            if (!file.canRead()) {
                // If the file can't be read, then we throw a 404 error.
                System.out.println("Ran into a not found.");
                String NOT_FOUND = "<!DOCTYPE html>\n" +
                        "<html lang=\"en\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <title>404 : Oops</title>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "<h1 style=\"text-align: center\">404 Not Found</h1>\n" +
                        "<nav style=\"text-align: center\"><a href=\"/\">Mama take me home</a></nav>\n" +
                        "<p style=\"text-align: center\">It appears that you may be lost</p>\n" +
                        "</body>\n" +
                        "</html>";
                WriteToClient("404 NOT FOUND", "text/html", NOT_FOUND.getBytes(), null);
            } else {
                System.out.println("File found, reading file.");
                WriteToClient("200 OK", Files.probeContentType(file.toPath()), FileHandler.readFileBytes(file.getPath()), null);
            }
        }
    }

    /**
     * Redirects a standard client to the https server.
     * @throws IOException Can throw an exception
     */
    private void RedirectToHTTPS () throws IOException {
        ArrayList<String> params = new ArrayList<>();
        params.add("Location: https://localhost");
        WriteToClient("301 Moved Permanently", null, null, params);
    }

    /**
     * If the run() method has determined that the client has sent a POST request, this method will be called.
     * This method will extract the contents of the POST request and write them back into an HTML skeleton, to be sent
     * to the client.
     * @param path the route the client is trying to send the POST request to.
     * @throws IOException This method potentially throws an IO Exception if the WriteToClient method encounters an IOException itself.
     */
    private void POSTRequest (String path) throws IOException {
        System.out.println("POST Request detected.");
        System.out.println("To path : " + path);
        String data = null;
        StringBuilder formContents = new StringBuilder();

        // If somehow an empty POST request is sent then the response will be empty.
        if (clientQuery.contains("Content-Length: 0")) {
            formContents = new StringBuilder("No data");
        } else {
            data = clientQuery.substring(clientQuery.lastIndexOf("\n\r")).trim();
            formContents.append("<ul>");
            for(String POSTElement : data.split("&")) {
                String key = POSTElement.split("=")[0];
                String value = URLDecoder.decode(POSTElement.split("=")[1], StandardCharsets.UTF_8);
                formContents.append("<li>");
                formContents.append(key);
                formContents.append(" : ");
                formContents.append(value);
                formContents.append("</li>");
            }
            formContents.append("</ul>");
        }
        System.out.println("Data is : " + data);
        String postResponse = "<a href=\"/\">Bring me home</a>\n" +
                "<p>POST data is : " + formContents + "</p>";
        WriteToClient( "200 OK" , "text/html" , postResponse.getBytes(), null);
    }

    /**
     * If the request is neither a GET request nor a POST, the run() method will use this method.
     * This method will simply send a 400 BAD REQUEST error back to the client.
     * @throws IOException This method potentially throws an IO Exception if the WriteToClient method encounters an IOException itself.
     */
    private void INVALIDRequest() throws IOException {
        System.out.println("Invalid request received, sending 400");
        WriteToClient("400 BAD REQUEST", null, null, null);
    }

    /**
     * This method is used to write the HTTP response contents back to the client.
     * It first writes the headers of the response, with the status, MIME type and content length specified in the parameters,
     * then writes the contents of the file that was previously read via the other methods.
     * Lastly it calls closeLink().
     * @param status The status of the response (ex: 200 OK, 404 NOT FOUND, 400 BAD REQUEST, 420 BLAZE IT, etc...)
     * @param MIME The MIME type of the file. (ex: text/html, text/css, image/png, etc...)
     * @param contents The contents of the file as a byte Array.
     * @param additionnalHeaders can be used to add other headers to the response.
     * @throws IOException Throws an IOException if the OutputWriter encounters an error.
     */
    private void WriteToClient(String status, String MIME, byte[] contents, ArrayList<String> additionnalHeaders) throws IOException {
        if (MIME != null) System.out.println("Sending contents of MIME type : " + MIME);

        out.writeLine("HTTP/1.1 " + status);

        if (MIME != null) {
            out.writeLine("ContentType: " + MIME);
            out.writeLine("ContentLength: " + contents.length);
            out.writeLine("Connection: close");
        }

        if (additionnalHeaders != null) {
            for(String header : additionnalHeaders) {
                out.write(header.getBytes(StandardCharsets.UTF_8));
            }
        }

        out.writeLine();
        if (contents != null) {
            out.write(contents);
        }
        closeLink();
    }

    /**
     * Closes the Output/InputWriter as well as the Socket that were used to respond to the client.
     * @throws IOException Can throw an IOException because of the Output/InputWriter classes.
     */
    void closeLink() throws IOException {
        System.out.println("Closing current connection");
        out.close();
        in.close();
        if (client != null) {
            client.close();
        } else {
            assert unsecuredClient != null;
            unsecuredClient.close();
        }
        System.out.println("Connection closed");
    }

    /**
     * This method is used to run a ping command line on the Server's side.
     * It will redirect the response to a StringBuilder to build a sort of HTML skeleton to be written as a reponse.
     * @return The String that was created containing the contents of the command line.
     * @throws IOException Can throw an IOException because of the exec() and readLine() methods used.
     */
    private String doSomePinging() throws IOException {
        Runtime rt = Runtime.getRuntime();

        String[] command = {"ping", "1.1.1.1"};

        System.out.println("Running Ping.");
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

        StringBuilder pingResponse = new StringBuilder("<a href=\"/\">Bring me home</a>\n" + "<p>Ping results are :</p><pre>");

        String s;
        while ((s=stdInput.readLine()) != null) {
            pingResponse.append(s).append("\n");
        }
        System.out.println("Here is the standard error of the command (if any):\n");
        while ((s = stdError.readLine()) != null) {
            pingResponse.append(s).append("\n");
            System.out.println(s);
        }
        pingResponse.append("</pre>");

        return pingResponse.toString();
    }

}
