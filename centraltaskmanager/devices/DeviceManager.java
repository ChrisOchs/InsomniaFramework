/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package centraltaskmanager.devices;

import centraltaskmanager.DatabaseManager;
import centraltaskmanager.Options;
import centraltaskmanager.tasks.SensingTask.SensorType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author Chris
 */
public class DeviceManager {
    
    private DatabaseManager dbManager;
    
    private HashMap<String, Device> devices = new HashMap<String, Device>();
    
    public DeviceManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }
    
    public void initializeDebug() {
        if (Options.DEBUG_MODE) {
            devices.put("abcd", new Device("abcd", "", "", "", "", new String[0]));
            devices.put("1234", new Device("1234", "", "", "", "", new String[0]));
            devices.put("ab12", new Device("ab12", "", "", "", "", new String[0]));
        }
    }
    
    public boolean deviceExists(String deviceId) {
        return devices.containsKey(deviceId);
    }
    
    public Device registerDevice(String make, String model, 
            String androidVersion, String deviceId, String [] sensors) {
        
        String uniqueDeviceId = UUID.randomUUID().toString();
        
        if(sensors == null) {
            sensors = new String[0];
        }
        
        Device device = new Device(uniqueDeviceId, make, model, androidVersion, deviceId, sensors);
        devices.put(uniqueDeviceId, device);
        
        return device;
    }
    
    public Device getDeviceFromUniqueId(String uniqueId) {
        return devices.get(uniqueId);
    }
    
    public ArrayList<Device> getAvailableDevices() {
        ArrayList<Device> availableDevices = new ArrayList<Device>();
        
        for(Device device : devices.values()) {
            if(device.onStandby()) {
                availableDevices.add(device);
            }
        }
        
        return availableDevices;
    }

    public ArrayList<Device> getAvailableDevicesWithSensors(ArrayList<SensorType> sensors) {
        ArrayList<Device> availableDevices = new ArrayList<Device>();

        for (Device device : devices.values()) {
            if (device.onStandby() && device.hasSensors(sensors)) {
                availableDevices.add(device);
            }
        }

        return availableDevices;
    }
}
