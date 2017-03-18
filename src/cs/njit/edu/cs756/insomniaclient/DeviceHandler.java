package cs.njit.edu.cs756.insomniaclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;

/**
 *
 * @author Chris O
 */
public class DeviceHandler {

    public void initializeDeviceInformation(Activity activity) {
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);

        Sensor accelorometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        WifiManager wifi = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        if(accelorometerSensor != null) {
            DeviceInformation.availableSensors.add(InsomniaTask.SensorReadType.Accelerometer);
        }
        
        if(gyroSensor != null) {
            DeviceInformation.availableSensors.add(InsomniaTask.SensorReadType.Gyroscope);
        }
        
        if(wifi.isWifiEnabled()) {
            DeviceInformation.availableSensors.add(InsomniaTask.SensorReadType.Wifi);
        }
        
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            DeviceInformation.availableSensors.add(InsomniaTask.SensorReadType.GPS);
        }
        
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            DeviceInformation.availableSensors.add(InsomniaTask.SensorReadType.Bluetooth);
        }
    }
}
