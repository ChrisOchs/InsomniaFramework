package cs.njit.edu.cs756.insomniaclient;

import java.util.ArrayList;

/**
 *
 * @author Chris O
 */
public class InsomniaTask {
    public static enum TaskType {Computation, Photo, Sensing};
    
    public static enum SensorReadType {
        Accelerometer,
        Gyroscope,
        GPS,
        Wifi,
        Bluetooth
    }
    
    private TaskType type;
    
    private ArrayList<String> scripts = new ArrayList<String>();
    
    private ArrayList<String> inputs = new ArrayList<String>();
    
    private ArrayList<String> output;
    
    private ArrayList<SensorReadType> sensors;
    
    private int sensorReadDuration;
    
    private long executionTime;
    
    public InsomniaTask() {
        this(TaskType.Computation);
    }
    
    public InsomniaTask(TaskType type) {
        this.type = type;
    }
    
    public InsomniaTask(ArrayList<SensorReadType> sensorTypes, int sensorReadDuration) {
        this(TaskType.Sensing);
        
        this.sensors = sensorTypes;
        this.sensorReadDuration = sensorReadDuration;
    }
    
    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public void addScript(String script) {
        this.scripts.add(script);
    }
    
    public String getScript() {
        return scripts.get(0);
    }
    
    public void addInput(String input) {
        inputs.add(input);
    }
    
    public String getInput() {
        return inputs.get(0);
    }
    
    public void setOutput(ArrayList<String> output) {
        this.output = output;
    }
    
    public ArrayList<String> getOutput() {
        return output;
    }
    
    public ArrayList<SensorReadType> getSensorsUsed() {
        return sensors;
    }
    
    public int getSensorReadTime() {
        return sensorReadDuration;
    }
}
