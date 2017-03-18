package centraltaskmanager;

import centraltaskmanager.devices.DeviceManager;
import centraltaskmanager.tasks.TaskManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Chris
 */
public class ConnectionManager {

    private int taskPort;
    private int devicePort;
    
    private int nextConnectionId = 0;
    
    private List<TaskConnectionHandler> openTaskConnections =
            Collections.synchronizedList(new ArrayList<TaskConnectionHandler>());
    
    private List<DeviceConnectionHandler> openDeviceConnections =
            Collections.synchronizedList(new ArrayList<DeviceConnectionHandler>());
    
    private DatabaseManager dbManager = new DatabaseManager();
    
    private TaskManager taskManager = new TaskManager(dbManager);
    private DeviceManager deviceManager = new DeviceManager(dbManager);
    
    private ConnectionHandlerFactory connectionHandlerFactory = 
            new ConnectionHandlerFactory(deviceManager, taskManager);
    
    public ConnectionManager(int taskPort, int devicePort) {
        this.taskPort = taskPort;
        this.devicePort = devicePort;
        
        Thread connectionCleanupThread = new Thread(
                new Runnable() {
                    public void run() {
                        while(true) {
                            removeDeadConnections();
                        }
                    }
                });

        connectionCleanupThread.start();
        
        Thread taskServerThread = new Thread(
               new Runnable() {
                   public void run() {
                       runTaskServer();
                   }
               });
        
        Thread deviceServerThread = new Thread(
                new Runnable() {
                    public void run() {
                        runDeviceServer();
                    }
                });
        
        Thread updateThread = new Thread(
                new Runnable() {
                    public void run() {
                        while(true) {
                            taskManager.update();
                        }
                    }
                });
        
        if(Options.DEBUG_MODE) {
            deviceManager.initializeDebug();
            taskManager.initializeDebug();
            
            try {
                taskManager.getTask(0).executeTask(deviceManager);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        taskServerThread.start();
        deviceServerThread.start();
        updateThread.start();
    }

    private void removeDeadConnections() {
        for (int c = openTaskConnections.size() - 1; c >= 0; c--) {
            if (!openTaskConnections.get(c).isConnected()) {

                Logger.doLog(this, "STATUS", "Connection manager removed dead task connection "
                        + openTaskConnections.get(c).getConnectionId());

                openTaskConnections.remove(c);
            }
        }

        for (int c = openDeviceConnections.size() - 1; c >= 0; c--) {
            if (!openDeviceConnections.get(c).isConnected()) {

                Logger.doLog(this, "STATUS", "Connection manager removed dead connection "
                        + openDeviceConnections.get(c).getConnectionId());

                openDeviceConnections.remove(c);
            }
        }
    }
    
    private void runTaskServer() {
        try {            
            ServerSocket serverSocket = new ServerSocket(taskPort);
            
            while(true) {
                Logger.doLog(this, "STATUS", "Task Connection manager waiting for connections...");
                
                Socket socket = serverSocket.accept();
                
                Logger.doLog(this, "STATUS", "Task Connection manager accepted new connection from " +
                        socket.getInetAddress());
                
                TaskConnectionHandler handler = connectionHandlerFactory.createTaskConnectionHandler(socket, nextConnectionId++);
                
                Thread handlerThread = new Thread(handler);
                handlerThread.start();
                
                openTaskConnections.add(handler);
            }
        } catch(IOException ioe) {
            System.err.print(ioe);
        } 
    }
    
    private void runDeviceServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(devicePort);

            while (true) {
                Logger.doLog(this, "STATUS", "Device Connection manager waiting for connections...");

                Socket socket = serverSocket.accept();

                Logger.doLog(this, "STATUS", "Device Connection manager accepted new connection from "
                        + socket.getInetAddress());

                DeviceConnectionHandler handler = connectionHandlerFactory.createDeviceConnectionHandler(socket, nextConnectionId++);

                Thread handlerThread = new Thread(handler);
                handlerThread.start();

                openDeviceConnections.add(handler);
            }
        } catch (IOException ioe) {
            System.err.print(ioe);
        }
    }
}
