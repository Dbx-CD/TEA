package IOHandling;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * This class will write to the specified outputStream.
 */
public class OutputHandler {

    OutputStream outputStream;

    /**
     * On creation the constructor receives the {@link OutputStream} and saves it.
     * Here the {@link OutputStream} is the {@link javax.net.ssl.SSLSocket}'s OutputStream.
     * @param out The {@link OutputStream}
     */
    public OutputHandler(OutputStream out) {
        outputStream = out;
    }

    /**
     * Will write the contents of the String in parameter as a single line in the outputStream.
     * @param contents The String to write.
     * @throws IOException Can throw an IOException.
     */
    public void writeLine(String contents) throws IOException {
        write((contents + "\r\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Will write one empty line in the Stream, used to build the HTTP responses.
     * @throws IOException Can throw an IOException.
     */
    public void writeLine() throws IOException {
        write(("\r\n").getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Writes to the outputStream the byte array specified in the parameter.
     * @param data The data to write.
     * @throws IOException Can throw an exception.
     */
    public void write(byte[] data) throws IOException {
        outputStream.write(data);
        outputStream.flush();
    }

    /**
     * Do I really need to comment this ?
     * I do close thingy mmmh yeesss.
     * @throws IOException Can throw an exception.
     */
    public void close() throws IOException {
        outputStream.close();
    }
}
