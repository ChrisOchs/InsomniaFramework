package centraltaskmanager;

import centraltaskmanager.devices.Device;
import centraltaskmanager.devices.DeviceManager;
import centraltaskmanager.tasks.ComputationTask;
import centraltaskmanager.tasks.PhotoTask;
import centraltaskmanager.tasks.SensingTask;
import centraltaskmanager.tasks.Task;
import centraltaskmanager.tasks.TaskManager;
import centraltaskmanager.tasks.Tasklette;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Chris
 */
public class DeviceConnectionHandler implements Runnable {
    
    private DeviceManager deviceManager;
    private TaskManager taskManager;
    
    private Socket socket;
    
    private long connectionId;
    
    private boolean connected;
    
    public DeviceConnectionHandler(DeviceManager deviceManager, TaskManager taskManager, 
            Socket socket, long connectionId) {
        
        this.deviceManager = deviceManager;
        this.taskManager = taskManager;
        
        this.socket = socket;
        this.connectionId = connectionId;
        this.connected = true;
        
        Logger.doLog(this, "STATUS",
                String.format("Connection Handler created for %s with ConnectionID: %d",
                    socket.getInetAddress().toString(), connectionId));
    }
    
    public void run() {

        BufferedReader in = null;
        DataOutputStream out = null;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());

            Logger.doLog(this, "STATUS",
                    String.format("Device Connection Handler %d is awaiting commands from client.",
                    connectionId));

            String request;

            while ((request = in.readLine()) != null && !request.equals("finished")) {
                
                Logger.doLog(this, "STATUS",
                    String.format("Device Connection Handler %d received command (%s) from client.",
                    connectionId, request));

                if (request.equals("registerClientDevice")) {
                    doRegisterDevice(in, out);
                } else if (request.equals("retrieveNextTask")) {
                    doGetNextDeviceTask(in, out);
                } else if (request.equals("taskComplete")) {
                    doTaskExecutionComplete(in, out);
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
    
    private void doRegisterDevice(BufferedReader in, DataOutputStream out) throws IOException {
        String phoneInfo = in.readLine();

        String[] clientDetails = phoneInfo.split("\\|");

        String phoneMfg = clientDetails[0];
        String phoneModel = clientDetails[1];
        String androidVersion = clientDetails[2];
        String phoneId = clientDetails[3];
        
        String sensorDetails = in.readLine();
        
        String [] sensorTypes = null;
        
        if(sensorDetails.equals("sensorsAvailable")) {
            String sensors = in.readLine();
            
             sensorTypes = sensors.split("\\|");
        }

        Device device = deviceManager.registerDevice(phoneMfg, phoneModel, androidVersion, phoneId, sensorTypes);

        Logger.doLog(this, "STATUS",
                String.format("Connection Handler %d registering device (UUID: %s).",
                connectionId, device.getUniqueID()));

        out.writeBytes(device.getUniqueID() + "\n");
    }
    
    private void doGetNextDeviceTask(BufferedReader in, DataOutputStream out) throws IOException {
        String deviceId = in.readLine();
        
        if(!deviceManager.deviceExists(deviceId)) {
            out.writeBytes("noSuchDeviceException\n");
        }
        
        Device device = deviceManager.getDeviceFromUniqueId(deviceId);
        
        device.setLastUpdateTime(System.currentTimeMillis());
        
        Tasklette tasklette = device.getNextAssignedTask();
        
        if(tasklette != null) {
            
            Task task = tasklette.getTask();
            
            String taskType = "";
            
            if(task instanceof SensingTask) {
                taskType = "sensing";
            } else if(task instanceof PhotoTask) {
                taskType = "photo";
            } else if(task instanceof ComputationTask) {
                taskType = "computation";
            } else {
                // What is it?
            }
            
            out.writeBytes("taskPending\n" + taskType + "\n");
            
            ArrayList<File> scriptFiles = tasklette.getTask().getScriptFiles();
            File scriptFile = scriptFiles.get(0); // TODO: Multiple scripts
            
            out.writeBytes("scriptFile\n" + scriptFile.length() + "\n");
            
            FileInputStream fis = new FileInputStream(scriptFile);
            
            byte [] buffer = new byte[(int)scriptFile.length()];
            fis.read(buffer);
            fis.close();
            
            out.write(buffer);
            out.flush();
            
            File inputFile = tasklette.getInputFile();
            out.writeBytes("inputFile\n" + inputFile.length() + "\n");
            
            fis = new FileInputStream(inputFile);
            buffer = new byte[(int)inputFile.length()];
            fis.read(buffer);
            
            out.write(buffer);
            out.flush();
            
            device.executingTask(tasklette);
        } else {
            out.writeBytes("noTasksPending\n");
        }
    }
    
    private void doTaskExecutionComplete(BufferedReader in, DataOutputStream out) throws IOException {
        String deviceId = in.readLine();
        
        if(!deviceManager.deviceExists(deviceId)) {
            out.writeBytes("noSuchDeviceException\n");
        }
        
        Device device = deviceManager.getDeviceFromUniqueId(deviceId);
        device.setLastUpdateTime(System.currentTimeMillis());
        
        Tasklette executedTask = device.getExecutingTask();
        
        // Tasklette reassigned
        if(!executedTask.getDeviceExecutedOn().getUniqueID().equals(device.getUniqueID())) {
            out.writeBytes("taskletteReassignedException\n");
            
            return;
        }
        
        out.writeBytes("clearToSendResults\n");
        
        int resultCount = Integer.parseInt(in.readLine());
        
        ArrayList<String> results = new ArrayList<String>();
        
        for(int c = 0; c < resultCount; c++) {
            String line = in.readLine();            
            results.add(line);
        }
        
        long executionTime = Long.parseLong(in.readLine());
        
        executedTask.saveOutput(results);
        executedTask.setComplete(executionTime);
        device.executingTask(null);
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public long getConnectionId() {
        return connectionId;
    }
}
