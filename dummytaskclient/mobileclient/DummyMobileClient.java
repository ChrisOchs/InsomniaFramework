/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dummytaskclient.mobileclient;

import dummytaskclient.TaskHelper;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 *
 * @author Chris
 */
public class DummyMobileClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        String[] uniqueIds = new String[]{"abcd", "1234", "ab12"};

        for (String uniqueId : uniqueIds) {

            try {
                Socket socket = new Socket("localhost", 44663);

                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                /*
                 out.writeBytes("registerClientDevice\n"
                 + "Samsung|GSIII|4.3|0xdeadbeef\n");
            
                 String uniqueId = in.readLine();
            
                 System.out.println("Assigned Unqiue ID: " + uniqueId);
                 */

                out.writeBytes("retrieveNextTask\n" + uniqueId + "\n");

                String response = in.readLine();

                System.out.println("Response: " + response);

                if (response.equals("taskPending")) {

                    String receiving = in.readLine();

                    if (receiving.equals("scriptFile")) {
                        String fileSizeStr = in.readLine();
                        int fileSize = Integer.parseInt(fileSizeStr);

                        String taskScript = TaskHelper.copyFromStreamToString(in, fileSize);

                        System.out.println("----- Received script: \n" + taskScript);
                    }

                    receiving = in.readLine();

                    if (receiving.equals("inputFile")) {
                        String fileSizeStr = in.readLine();
                        int fileSize = Integer.parseInt(fileSizeStr);

                        String input = TaskHelper.copyFromStreamToString(in, fileSize);

                        System.out.println("------ Received input: \n" + input);
                    }


                    System.out.println("SIMULATING OUTPUT...");

                    out.writeBytes("taskComplete\n" + uniqueId + "\n");

                    receiving = in.readLine();

                    System.out.println("Task status: " + receiving);

                    if (receiving.equals("clearToSendResults")) {
                        out.writeBytes("3\n12345\n54321\n13579\n");

                        long executionTime = 10000 + (long) (Math.random() * 5000);

                        out.writeBytes(executionTime + "\n");
                    }
                }

                out.writeBytes("finished\n");

                in.close();
                out.close();
                socket.close();
            } catch (UnknownHostException uhe) {
                System.err.println(uhe);
            } catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }
}
