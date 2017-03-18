package dummytaskclient;

import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author Chris
 */
public class TaskHelper {
    
    public static String copyFromStreamToString(BufferedReader in, int fileLength) throws IOException {

        char[] buffer = new char[fileLength];
        
        int totalCharsRead = 0;
        
        while(totalCharsRead < fileLength) {
            totalCharsRead += in.read(buffer, totalCharsRead, buffer.length - totalCharsRead);
        }
        
        return new String(buffer);
    }
}
