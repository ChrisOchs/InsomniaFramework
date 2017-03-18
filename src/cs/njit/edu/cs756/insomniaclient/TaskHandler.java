package cs.njit.edu.cs756.insomniaclient;

import android.provider.Settings.Secure;
import android.util.Log;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask.SensorReadType;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.NoTaskAvailableException;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.UnknownTaskTypeException;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Chris O
 */
public class TaskHandler {

    public TaskHandler() {
        
    }
    
    public String registerDevice(BufferedReader in, DataOutputStream out) throws IOException {
        out.writeBytes("registerClientDevice\n");
        
        String details = 
                android.os.Build.MANUFACTURER + "|" + 
                android.os.Build.MODEL + "|" +
                android.os.Build.VERSION.RELEASE + "|" +
                Secure.ANDROID_ID;
        
        out.writeBytes(details + "\n");
        
        ArrayList<SensorReadType> readTypes = DeviceInformation.availableSensors;
        
        if(readTypes.isEmpty()) {
            out.writeBytes("noSensorsAvailable\n");
        } else {
            
            out.writeBytes("sensorsAvailable\n");
            
            String sensors = readTypes.get(0).name();
            
            for(int c = 1; c < readTypes.size(); c++) {
                sensors += ("|" + readTypes.get(c));
            }
            
            out.writeBytes(sensors + "\n");
        }
        
        String insomniaId = in.readLine();
        
        return insomniaId;
    }
    

    public InsomniaTask getTask(BufferedReader in, DataOutputStream out) throws IOException,
            NoTaskAvailableException, UnknownTaskTypeException {

        out.writeBytes("retrieveNextTask\n" + DeviceInformation.insomniaId + "\n");
        
        String response = in.readLine();

        if (response.equals("taskPending")) {

            String taskType = in.readLine();

            InsomniaTask.TaskType type = null;

            if (taskType.equalsIgnoreCase("computation")) {
                type = InsomniaTask.TaskType.Computation;
            } else if (taskType.equalsIgnoreCase("photo")) {
                type = InsomniaTask.TaskType.Photo;
            } else if (taskType.equalsIgnoreCase("sensing")) {
                type = InsomniaTask.TaskType.Sensing;
            } else {
                Log.e("Task Type Error", "Unknown task type encountered!");

                throw new UnknownTaskTypeException();
            }
            
            InsomniaTask task;

            if (type == InsomniaTask.TaskType.Sensing) {
                String sensorTypesStr = in.readLine();
                String readTimeStr = in.readLine();

                int readTime = Integer.parseInt(readTimeStr);
                
                ArrayList<InsomniaTask.SensorReadType> sensors = new ArrayList<InsomniaTask.SensorReadType>();
                
                String [] sensorTypeStrs = sensorTypesStr.split("\\|");
                
                for(String s : sensorTypeStrs) {
                    sensors.add(InsomniaTask.SensorReadType.valueOf(s));
                }
                
                task = new InsomniaTask(sensors, readTime);
            } else {
                task = new InsomniaTask(type);
            }

            String receiving = in.readLine();

            String taskScript = "";

            String input = "";

            if (receiving.equals("scriptFile")) {
                String fileSizeStr = in.readLine();
                int fileSize = Integer.parseInt(fileSizeStr);

                taskScript = TaskHelper.copyFromStreamToString(in, fileSize);
            }

            task.addScript(taskScript);

            receiving = in.readLine();

            if (receiving.equals("inputFile")) {
                String fileSizeStr = in.readLine();
                int fileSize = Integer.parseInt(fileSizeStr);

                input = TaskHelper.copyFromStreamToString(in, fileSize);
            }

            task.addInput(input);

            return task;
        }

        throw new NoTaskAvailableException();
    }

    public void sendTaskResults(BufferedReader in, DataOutputStream out, InsomniaTask task) throws IOException {
        out.writeBytes("taskComplete\n" + DeviceInformation.insomniaId + "\n");

        String receiving = in.readLine();

        Log.d("Task Status", "Received task status: " + receiving);
        
        ArrayList<String> output = task.getOutput();
        long executionTime = task.getExecutionTime();

        if (receiving.equals("clearToSendResults")) {
            out.writeBytes(output.size() + "\n");

            for (String result : output) {
                out.writeBytes(result + "\n");
            }

            out.writeBytes(executionTime + "\n");
        }
    }
}
