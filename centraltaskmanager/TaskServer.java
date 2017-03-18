package centraltaskmanager;

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Chris
 */
public class TaskServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        TaskServer taskServer = new TaskServer(44553, 44663);
        
        if(!Options.DEBUG_MODE) {
            try {
                FileHelper.delete(new File("tasks/"));
            } catch(IOException ioe) {
                System.err.println(ioe);
            }
        }
    }
    
    private int taskPort;
    private int devicePort;
    
    private ConnectionManager connectionManager;
    
    public TaskServer(int taskPort, int devicePort) {
        this.taskPort = taskPort;
        this.devicePort = devicePort;
        this.connectionManager = new ConnectionManager(taskPort, devicePort);
        
        Logger.doLog(this, "STATUS", "Task Server started...");
    }
}
