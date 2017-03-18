package cs.njit.edu.cs756.insomniaclient.sensor;

import android.bluetooth.BluetoothDevice;

/**
 *
 * @author Chris O
 */
public class BluetoothSensorResult {
    public String address;
    public String name;
    public String bluetoothClass;
    
    public BluetoothSensorResult(BluetoothDevice device) {
        this.address = device.getAddress();
        this.name = device.getName();
        this.bluetoothClass = device.getBluetoothClass().toString();
    }
    
    public String toString() {
        return address + " | " + name + " | " + bluetoothClass;
    }
}
