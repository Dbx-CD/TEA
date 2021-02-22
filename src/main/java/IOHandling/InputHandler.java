package IOHandling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * This class will generate a {@link BufferedReader} to read the request sent by the client.
 * It also contains the methods to read said {@link BufferedReader} and return its contents.
 */
public class InputHandler {

    private final BufferedReader reader;

    static final int BUFFER_LENGTH = 1000;

    /**
     * Constructor that generates the {@link BufferedReader} based on the {@link InputStream} specified in the parameter.
     * @param in The {@link InputStream} (In this case the {@link javax.net.ssl.SSLSocket}'s {@link InputStream}).
     */
    public InputHandler(InputStream in) {
        reader = new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Reads One line of the BufferedReader and returns it as a String.
     * @return The line of the BufferedReader as a String.
     * @throws IOException Can throw an IOException because of the readLine() method.
     */
    public String readNextLine() throws IOException {
        return reader.readLine();
    }

    /**
     * Reads the contents of the BufferedReader for a set length, specified in 'BUFFER_LENGTH'
     * @return The char Array with the contents of the BufferedReader.
     * @throws IOException Can throw an IOException because of .read()
     */
    public char[] read() throws IOException {
        char[] chain = new char[BUFFER_LENGTH];
        reader.read(chain);
        return chain;
    }

    /**
     * Closes the BufferedReader.
     * @throws IOException Can throw an IOException.
     */
    public void close() throws IOException {
        reader.close();
    }
}
