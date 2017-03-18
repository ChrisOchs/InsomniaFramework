package centraltaskmanager;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Chris
 */
public class FileHelper {

    public static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }
    
    public static void copyFromBufferedReaderToFile(BufferedReader in, int fileLength, File file) throws IOException {
        BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(file));

        char[] buffer = new char[fileLength];
        
        int totalCharsRead = 0;
        
        while(totalCharsRead < fileLength) {
            totalCharsRead += in.read(buffer, totalCharsRead, buffer.length - totalCharsRead);
        }
        
        fos.write(new String(buffer).getBytes());
        
        fos.close();
    }
}
