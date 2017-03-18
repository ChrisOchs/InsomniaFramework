package centraltaskmanager.tasks;

import java.util.ArrayList;

/**
 *
 * @author Chris O
 */
public class SensingTask extends Task {
    
    public static enum SensorType {
        Accelerometer,
        Gyroscope,
        GPS,
        Wifi,
        Bluetooth
    }
    
    private ArrayList<SensorType> sensorTypes;
    
    private int sensorReadDuration = -1;
    
    public SensingTask(TaskManager taskManager, long taskId, String taskName, int senseDuration, ArrayList<SensorType> sensorTypes) {
        super(taskManager, taskId, taskName);
        
        this.sensorReadDuration = senseDuration;
        
        this.sensorTypes = sensorTypes;
    }
    
    public ArrayList<SensorType> getSensorTypes() {
        return sensorTypes;
    }
}
