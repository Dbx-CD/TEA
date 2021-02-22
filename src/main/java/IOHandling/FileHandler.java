package IOHandling;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * This class handles reading the files for the server.
 */
public class FileHandler {

    /**
     * Will Read a file contained in a specified path and return the contents as a byte Array.
     * @param path The path to the file's position.
     * @return The byte array with the file's contents.
     * @throws IOException readAllBytes may throw an exception.
     */
    public  static byte[] readFileBytes(String path) throws IOException {
        return Files.readAllBytes( Paths.get( path ) );
    }

}
