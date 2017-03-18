package centraltaskmanager;

import java.util.UUID;

/**
 *
 * @author Chris
 */
public class DeviceManager {
    
    private DatabaseManager dbManager;
    
    public DeviceManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public boolean deviceExists(String deviceId) {
        // TODO: Determine if device was previously registered.
        
        return false;
    }
    
    public String registerDevice(String make, String model, 
            String androidVersion, String deviceId) {
        
        // TODO: Register the device to DB.
        
        return UUID.randomUUID().toString();
    }
}
