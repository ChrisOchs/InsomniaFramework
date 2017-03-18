/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package centraltaskmanager.tasks;

import centraltaskmanager.DatabaseManager;
import centraltaskmanager.Logger;
import centraltaskmanager.Options;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Chris
 */
public class TaskManager {
    
    private long nextTaskId = 0;
    
    private HashMap<Long, Task> unfinishedTasks = new HashMap<Long, Task>();
    
    private HashMap<Long, Task> finishedTasks = new HashMap<Long, Task>();
    
    private DatabaseManager dbManager;
    
    public TaskManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public void initializeDebug() {
        if (Options.DEBUG_MODE) {
            unfinishedTasks.put(0l, new ComputationTask(this, 0, "TEST_COMPUTATION_TASK"));
        }
    }
    
    public ComputationTask registerComputationTask(String taskName) {
        ComputationTask task = new ComputationTask(this, nextTaskId++, taskName);

        unfinishedTasks.put(task.getId(), task);

        return task;
    }
    
    public SensingTask registerSensingTask(String taskName, String [] sensorTypeStrs, int senseDuration) {
        ArrayList<SensingTask.SensorType> sensorTypes = new ArrayList<SensingTask.SensorType>();
        
        for(String s : sensorTypeStrs) {
            sensorTypes.add(SensingTask.SensorType.valueOf(s));
        }
        
        SensingTask task = new SensingTask(this, nextTaskId++, taskName, senseDuration, sensorTypes);
        
        unfinishedTasks.put(task.getId(), task);
        
        return task;
    }

    public PhotoTask registerPhotoTask(String taskName) {
        
        PhotoTask task = new PhotoTask(this, nextTaskId++, taskName);

        unfinishedTasks.put(task.getId(), task);

        return task;
    }
    
    public Task getTask(long id) throws InvalidTaskException {
        if(!unfinishedTasks.containsKey(id) && !finishedTasks.containsKey(id)) {
            throw new InvalidTaskException("No task with given identification number.");
        }
        
        if(finishedTasks.containsKey(id)) {
            return finishedTasks.get(id);
        }
        
        // TODO: Security check
        
        return unfinishedTasks.get(id);
    }
    
    public void update() {
        ArrayList<Task> completedTasks = new ArrayList<Task>();
        
        for (Task task : unfinishedTasks.values()) {
            if (!task.isComplete()) {
                task.update();
            } else {
                completedTasks.add(task);
            }
        }
        
        for(Task task : completedTasks) {
            unfinishedTasks.remove(task.getId());
            
            finishedTasks.put(task.getId(), task);
            
            Logger.doLog(this, "STATUS", "Task " + task.getId() + " complete...");
        }
    }
}
