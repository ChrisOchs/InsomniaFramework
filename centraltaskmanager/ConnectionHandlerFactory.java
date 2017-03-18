package centraltaskmanager;

import centraltaskmanager.devices.DeviceManager;
import centraltaskmanager.tasks.TaskManager;
import java.net.Socket;

/**
 *
 * @author Chris
 */
public class ConnectionHandlerFactory {

    private DeviceManager deviceManager;
    
    private TaskManager taskManager;
    
    public ConnectionHandlerFactory(DeviceManager deviceManager, TaskManager taskManager) {
        this.deviceManager = deviceManager;
        this.taskManager = taskManager;
    }
    
    public TaskConnectionHandler createTaskConnectionHandler(Socket socket, long connectionId) {
        return new TaskConnectionHandler(taskManager, deviceManager, socket, connectionId);
    }
    
    public DeviceConnectionHandler createDeviceConnectionHandler(Socket socket, long connectionId) {
        return new DeviceConnectionHandler(deviceManager, taskManager, socket, connectionId);
    }
}
