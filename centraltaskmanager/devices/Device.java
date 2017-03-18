package centraltaskmanager.devices;

import centraltaskmanager.tasks.SensingTask.SensorType;
import centraltaskmanager.tasks.Tasklette;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 *
 * @author Chris
 */
public class Device {
    
    // Active: Processing task
    // Standby: awaiting task assignment
    // Inactive: Not accepting tasks
    public static enum DeviceStatus {Executing, Standby, Inactive};
    
    private String uniqueId;
    
    private DeviceStatus status;
    
    private long lastUpdateTime;
    
    private Queue<Tasklette> assignedTasks = new ArrayDeque<Tasklette>();
    
    private Tasklette executingTask;
    
    private String phoneMake;
    private String phoneModel;
    private String androidVersion;
    private String deviceId;
    
    private ArrayList<SensorType> availableSensors = new ArrayList<SensorType>();
    
    public Device(String uniqueId, String phoneMake, 
            String phoneModel, String androidVersion, String deviceId, String [] sensors) {
        // TODO: load device from DB
        
        this.uniqueId = uniqueId;
        this.status = DeviceStatus.Standby;
        
        this.phoneMake = phoneMake;
        this.phoneModel = phoneModel;
        this.androidVersion = androidVersion;
        this.deviceId = deviceId;
        
        this.lastUpdateTime = System.currentTimeMillis();
        
        for(String type : sensors) {
            availableSensors.add(SensorType.valueOf(type));
        }
    }
    
    public String getUniqueID() {
        return uniqueId;
    }
    
    public String getPhoneMfg() {
        return phoneMake;
    }
    
    public String getPhoneModel() {
        return phoneModel;
    }
    
    public String getAndroidVersion() {
        return androidVersion;
    }
    
    public String getDeviceId() {
        return deviceId;
    }

    public void setLastUpdateTime(long time) {
        this.lastUpdateTime = time;
    }
    
    public void assignTask(Tasklette task) {
        assignedTasks.add(task);
    }
    
    public Tasklette getNextAssignedTask() {
        return assignedTasks.poll();
    }
    
    public void executingTask(Tasklette task) {
        this.executingTask = task;
        
        if(task == null) {
            this.status = DeviceStatus.Standby;
        } else {
            task.setExecuting();
            this.status = DeviceStatus.Executing;
        }
    }
    
    public boolean hasSensors(ArrayList<SensorType> sensors) {
        return availableSensors.containsAll(sensors);
    }
    
    public ArrayList<SensorType> getAvailableSensors() {
        return availableSensors;
    }
    
    public Tasklette getExecutingTask() {
        return executingTask;
    }
    
    public boolean isExecuting() {
        return this.status.equals(DeviceStatus.Executing);
    }
    
    public boolean onStandby() {
        return this.status.equals(DeviceStatus.Standby);
    }
}
