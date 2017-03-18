/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dummytaskclient;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Chris
 */
public class DummyTaskClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try {
            Socket socket = new Socket("localhost", 44553);
            
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            
            out.writeBytes("registerTask\nMy Test Task\ncomputation\n");
               
            String response = in.readLine();
            
            long id;
            
            if(response.startsWith("taskCreationSuccess")) {
                
                String [] parts = response.split("\\|");
                id = Long.parseLong(parts[1]);
                
                out.writeBytes("setCurrentTask\n" + id + "\n");
                
                response = in.readLine();
                
                if(response.equals("changeSuccessful")) {
                    String inputFileName = "numbers.txt";
                    
                    File inputFile = new File("testfile/" + inputFileName);
                    
                    out.writeBytes("addInputFileToTask\n" + inputFileName + "\n" + inputFile.length() + "\n");

                    FileInputStream fis = new FileInputStream(inputFile);

                    if(inputFile.length() > Integer.MAX_VALUE) {
                        System.err.println("File too large!");
                        
                        return;
                        // TODO: Split file.
                    }
                    
                    byte[] buffer = new byte[(int)inputFile.length()];
                    int bytesRead = fis.read(buffer);
                    
                    fis.close();
                    
                    out.write(buffer);
                    out.flush();

                    response = in.readLine();

                    System.out.println("Add File Status: " + response);
                    
                    String scriptFileName = "script.txt";
                    
                    File scriptFile = new File("testfile/" + scriptFileName);
                    
                    out.writeBytes("addScriptToTask\n" + scriptFileName + "\n" + scriptFile.length() + "\n");
                    
                    buffer = new byte[(int)scriptFile.length()];
                    fis = new FileInputStream(scriptFile);
                    fis.read(buffer);
                    
                    fis.close();
                    
                    out.write(buffer);
                    out.flush();
                   
                    response = in.readLine();
                    
                    System.out.println("Add Script Status: " + response);
                    
                    out.writeBytes("executeTask\n");
                    
                    response = in.readLine();
                    
                    System.out.println("Task Execute Status: " + response);
                }

            } else {
                
            }
            
            out.writeBytes("finished\n");
            
            in.close();
            out.close();
            socket.close();
        } catch(UnknownHostException uhe) {
            
        } catch(IOException ioe) {
            System.err.println(ioe);
        }
    }
}
