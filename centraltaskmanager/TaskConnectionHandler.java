package centraltaskmanager;

import centraltaskmanager.devices.DeviceManager;
import centraltaskmanager.tasks.Task;
import centraltaskmanager.tasks.TaskManager;
import centraltaskmanager.tasks.InvalidTaskException;
import centraltaskmanager.tasks.UninitializedTaskException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 *
 * @author Chris
 */
public class TaskConnectionHandler implements Runnable {
    
    private TaskManager taskManager;
    
    private DeviceManager deviceManager;
    
    private Socket socket;
    
    private long connectionId;
    
    private boolean connected;
    
    public TaskConnectionHandler(TaskManager taskManager, DeviceManager deviceManager,
            Socket socket, long connectionId) {
        
        this.taskManager = taskManager;
        this.deviceManager = deviceManager;
        
        this.socket = socket;
        this.connectionId = connectionId;
        this.connected = true;
        
        Logger.doLog(this, "STATUS",
                String.format("Task Connection Handler created for %s with ConnectionID: %d",
                    socket.getInetAddress().toString(), connectionId));
    }
    
    private Task currentTask;
    
    public void run() {

        BufferedReader in = null;
        DataOutputStream out = null;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            Logger.doLog(this, "STATUS",
                    String.format("Task Connection Handler %d is awaiting commands from client.",
                    connectionId));

            String request;

            while ((request = in.readLine()) != null && !request.equals("finished")) {
                
                Logger.doLog(this, "STATUS",
                    String.format("Task Connection Handler %d received command (%s) from client.",
                    connectionId, request));

                if (request.equals("registerTask")) {
                    doRegisterTask(in, out);
                } else if (request.equals("setCurrentTask")) {
                    doSetCurrentTask(in, out);
                } else {
                    if (currentTask == null) {
                        out.writeBytes("noTaskSetError\n");
                    } else {
                        if (request.equals("addInputFileToTask")) {
                            doAddInputFile(currentTask, in, out);
                        } else if (request.equals("addScriptToTask")) {
                            doAddScriptFile(currentTask, in, out);
                        } else if (request.equals("executeTask")) {
                            doExecuteTask(currentTask, in, out);
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            System.err.println(ioe);
        } finally {
            try {

                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }

                if (socket != null) {
                    socket.close();
                }

            } catch (IOException ioe) {
            }
        }
        
        connected = false;
    }

    private void doRegisterTask(BufferedReader in, DataOutputStream out) throws IOException {
        String taskName = in.readLine();
        String taskType = in.readLine();
        
        Task task = null;
        
        if(taskType.equalsIgnoreCase("sensing")) {
            String sensorTypesStr = in.readLine();
            String readIntervalStr = in.readLine();
            
            String [] sensorTypes = sensorTypesStr.split("\\|");
            
            task = taskManager.registerSensingTask(taskName, sensorTypes, Integer.parseInt(readIntervalStr));
        } else if(taskType.equals("photo")) {
            task = taskManager.registerPhotoTask(taskName);
        } else if(taskType.equals("computation")) {
            task = taskManager.registerComputationTask(taskName);
        } else {
            // What is it?
        }
        
        if (task != null && task.isInitialized()) {
            out.writeBytes("taskCreationSuccess|" + task.getId() + "\n");
        } else {
            out.writeBytes("taskCreationFailure\n");
        }
    }
    
    private void doSetCurrentTask(BufferedReader in, DataOutputStream out) throws IOException {
        long taskId = Long.parseLong(in.readLine());

        try {
            currentTask = taskManager.getTask(taskId);
            out.writeBytes("changeSuccessful\n");
        } catch (InvalidTaskException e) {
            System.err.println(e);

            currentTask = null;
            out.writeBytes("invalidTaskSpecified\n");
        }
    }
    
    private void doAddInputFile(Task task, BufferedReader in, DataOutputStream out) throws IOException {

        String fileName = in.readLine();
        int fileLength = Integer.parseInt(in.readLine());

        try {
            task.addInputFile(fileName, fileLength, in);
            out.writeBytes("fileTransferComplete\n");
        } catch(IOException ioe) {
            out.writeBytes("fileTransferError\n");
        }
    }
    
    private void doAddScriptFile(Task task, BufferedReader in, DataOutputStream out) throws IOException {
        
        String fileName = in.readLine();
        int fileLength = Integer.parseInt(in.readLine());

        try {
            task.addScriptFile(fileName, fileLength, in);
            out.writeBytes("fileTransferComplete\n");
        } catch (IOException ioe) {
            out.writeBytes("fileTransferError\n");
        }
    }
    
    private void doExecuteTask(Task task, BufferedReader in, DataOutputStream out) throws IOException {
        try {
            task.executeTask(deviceManager);
            out.writeBytes("taskExecuting\n");
        } catch (UninitializedTaskException ute) {
            out.writeBytes("uninitializedTaskError\n");
        }
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public long getConnectionId() {
        return connectionId;
    }
}
