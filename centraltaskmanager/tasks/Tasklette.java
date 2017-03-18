/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package centraltaskmanager.tasks;

import centraltaskmanager.Logger;
import centraltaskmanager.devices.Device;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Chris
 */
public class Tasklette {
    
    private Task task;
    private File inputFile;
    private File outputFile;
    
    public static enum TaskletteStatus {Pending, InExecution, Complete, Failed};
    
    private TaskletteStatus status;
    
    private Device deviceExecutedOn;
    
    private long executionStartTime;
    private long executionEndTime;
    private long executionOnDeviceTime;
    
    public Tasklette(Task task, File inputFile, File outputFile) {
        this.task = task;
        this.inputFile = inputFile;
        this.outputFile = outputFile;
        
        this.status = TaskletteStatus.Pending;
    }
    
    public void setExecuting() {
        this.status = TaskletteStatus.InExecution;
        this.task.taskletteExecuting(this);
        this.executionStartTime = System.currentTimeMillis();
    }
    
    public void saveOutput(ArrayList<String> lines) {
        
        try {
            PrintWriter out = new PrintWriter(outputFile);
            
            for(String line : lines) {
                out.println(line);
            }
            
            out.close();
        } catch(FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }
    
    public void setComplete(long executionOnDeviceTime) {
        this.status = TaskletteStatus.Complete;
        this.task.taskletteComplete(this);
        this.executionEndTime = System.currentTimeMillis();
        
        this.executionOnDeviceTime = executionOnDeviceTime;
        
        Logger.doLog(this, "STATUS", "Tasklette complete...");
    }
    
    /*
     * Returns the total time the tasklette has been set as "executing" on the server.
     */
    public long serverExecutionTime() {
        
        if(isPending() || failed()) {
            return 0;
        }
        
        if(isComplete()) {
            return executionEndTime - executionStartTime;
        }
        
        return System.currentTimeMillis() - this.executionStartTime;
    }
    
    /*
     * Returns the total time the tasklette took to execute on the device. Will differ from server time.
     */
    public long deviceExecutionTime() {
        return this.executionOnDeviceTime;
    }
    
    public boolean isPending() {
        return this.status.equals(TaskletteStatus.Pending);
    }
    
    public boolean isExecuting() {
        return this.status.equals(TaskletteStatus.InExecution);
    }
    
    public boolean isComplete() {
        return this.status.equals(TaskletteStatus.Complete);
    }
    
    public boolean failed() {
        return this.status.equals(TaskletteStatus.Failed);
    }
    
    public Task getTask() {
        return task;
    }
    
    public File getInputFile() {
        return inputFile;
    }
    
    public void setDeviceExecutedOn(Device device) {
        this.deviceExecutedOn = device;
    }
    
    public boolean assignedToDevice() {
        return this.deviceExecutedOn != null;
    }
    
    public Device getDeviceExecutedOn() {
        return deviceExecutedOn;
    }
}
