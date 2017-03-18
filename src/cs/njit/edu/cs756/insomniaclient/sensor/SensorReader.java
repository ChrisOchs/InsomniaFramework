package cs.njit.edu.cs756.insomniaclient.sensor;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import cs.njit.edu.cs756.insomniaclient.DeviceInformation;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask.SensorReadType;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.InvalidSensorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Chris O
 */
public class SensorReader implements Runnable {
    
    private final int BLUETOOTH_READ_INTERVAL = 10000;
    
    private final int WIFI_READ_INTERVAL = 5000;
    
    private final int GPS_READ_INTERVAL = 1000;
    
    private final int SENSOR_READ_INTERVAL = 250;
    
    private boolean complete = false;
    
    private ArrayList<SensorReadType> sensors;
    
    private HashMap<SensorReadType, Object> sensorResults;
    
    private Activity activity;
    
    private int duration;
    
    public SensorReader(Activity activity, ArrayList<SensorReadType> sensors, int duration) throws InvalidSensorException {
        this.sensors = sensors;
        this.activity = activity;
        
        this.duration = duration;
        
        for(SensorReadType type : sensors) {
            if(!DeviceInformation.availableSensors.contains(type)) {
                throw new InvalidSensorException(type.name());
            }
        }
    }
    
    public boolean isComplete() {
        return complete;
    }
    
    public void run() {
        LocationManager locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        SensorManager sensorManager = (SensorManager) activity.getSystemService(Activity.SENSOR_SERVICE);
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();;
        
        AccelerometerListener accelerometerListener = null;
        GyroscopeListener gyroscopeListener = null;
        WifiResultReceiver wifiReceiver = null;
        GPSLocationListener gpsLocationListener = null;
        BluetoothResultReceiver bluetoothReceiver = null;
        
        
        if(sensors.contains(SensorReadType.Accelerometer)) {
            accelerometerListener = new AccelerometerListener();
            sensorManager.registerListener(accelerometerListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if(sensors.contains(SensorReadType.Gyroscope)) {
            gyroscopeListener = new GyroscopeListener();
            sensorManager.registerListener(gyroscopeListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        }
        
        if (sensors.contains(SensorReadType.Wifi)) {
            wifiReceiver = new WifiResultReceiver(wifiManager);

            if (wifiManager.isWifiEnabled()) {
                IntentFilter i = new IntentFilter();
                i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
                activity.registerReceiver(wifiReceiver, i);
            }
        }

        if (sensors.contains(SensorReadType.GPS)) {
            
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                
                gpsLocationListener = new GPSLocationListener();

                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        GPS_READ_INTERVAL,
                        1, 
                        gpsLocationListener);
            }
        }
        
        if(sensors.contains(SensorReadType.Bluetooth)) {
            bluetoothReceiver = new BluetoothResultReceiver(bluetoothAdapter);
            
            if(bluetoothAdapter.isEnabled()) {
                IntentFilter i = new IntentFilter();
                i.addAction(BluetoothDevice.ACTION_FOUND);
                activity.registerReceiver(bluetoothReceiver, i);
            }
        }
        
        long startTime = System.currentTimeMillis();
        
        long wifiLastRead = startTime;
        long bluetoothLastRead = startTime;
        
        
        while(System.currentTimeMillis() - startTime < duration) {
            
            if(wifiManager != null && System.currentTimeMillis() - wifiLastRead > WIFI_READ_INTERVAL) {
                wifiLastRead = System.currentTimeMillis();
                wifiManager.startScan();
            }
            
            if(bluetoothAdapter != null && !bluetoothAdapter.isDiscovering() && System.currentTimeMillis() - bluetoothLastRead > BLUETOOTH_READ_INTERVAL) {
                bluetoothLastRead = System.currentTimeMillis();
                bluetoothAdapter.startDiscovery();
            }
        }
        
        sensorResults = new HashMap<SensorReadType, Object>();
        
        if(accelerometerListener != null) {
            sensorManager.unregisterListener(accelerometerListener);
            sensorResults.put(SensorReadType.Accelerometer, accelerometerListener.getResults());
        }
        
        if(gyroscopeListener != null) {
            sensorManager.unregisterListener(gyroscopeListener);
            sensorResults.put(SensorReadType.Gyroscope, gyroscopeListener.getResults());
        }
        
        if(wifiReceiver != null) {
            activity.unregisterReceiver(wifiReceiver);
            sensorResults.put(SensorReadType.Wifi, wifiReceiver.getResults());
        }
        
        if(gpsLocationListener != null) {
            locationManager.removeUpdates(gpsLocationListener);
            sensorResults.put(SensorReadType.GPS, gpsLocationListener.getResults());
        }
        
        if(bluetoothAdapter != null) {
            activity.unregisterReceiver(bluetoothReceiver);
            sensorResults.put(SensorReadType.Bluetooth, bluetoothReceiver.getResults());
        }
        
        this.complete = true;
    }
    
    public HashMap<SensorReadType, Object> getSensorResults() {
        return sensorResults;
    }
    
    private class AccelerometerListener implements SensorEventListener {
        
        private ArrayList<SensorResult<Double[]>> results = new ArrayList<SensorResult<Double[]>>();
        
        private long lastRead = System.currentTimeMillis();
        
        public AccelerometerListener() {
            
        }
        
        public void onAccuracyChanged(Sensor sensor, int arg) {
            // Do nothing...
        }
        
        public void onSensorChanged(SensorEvent sensorEvent) {
            
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long readTime = System.currentTimeMillis();

                if (readTime - lastRead > SENSOR_READ_INTERVAL) {
                    Double[] values = new Double[]{(double) sensorEvent.values[0], (double) sensorEvent.values[1], (double) sensorEvent.values[2]};

                    results.add(new SensorResult<Double[]>(values));
                    
                    lastRead = readTime;
                }
            }
        }
        
        public ArrayList<SensorResult<Double[]>> getResults() {
            return results;
        }
    }
    
    private class GyroscopeListener implements SensorEventListener {
        
        private ArrayList<SensorResult<Double[]>> results = new ArrayList<SensorResult<Double[]>>();
        
        private long lastRead = System.currentTimeMillis();
        
        public GyroscopeListener() {
            
        }
        
        public void onAccuracyChanged(Sensor sensor, int arg) {
            // Do nothing...
        }
        
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                Log.d("Gyroscope Listener", Arrays.toString(sensorEvent.values));
                
                Double [] values = new Double[] {(double)sensorEvent.values[0], (double)sensorEvent.values[1], (double)sensorEvent.values[2]};
                
                results.add(new SensorResult<Double[]>(values));
            }
        }
        
        public ArrayList<SensorResult<Double[]>> getResults() {
            return results;
        }
    }
    
    private class WifiResultReceiver extends BroadcastReceiver {
        
        private ArrayList<SensorResult<ArrayList<WifiSensorResult>>> results = new ArrayList<SensorResult<ArrayList<WifiSensorResult>>>();
       
        private WifiManager wifiManager;
        
        public WifiResultReceiver(WifiManager wifiManager) {
            this.wifiManager = wifiManager;
        }

        public void onReceive(Context c, Intent i) {
            
            Log.d("Wifi Scan", "Receiving wifi scan...");
            
            List<ScanResult> scanResults = wifiManager.getScanResults();
            
            ArrayList<WifiSensorResult> accessPoints = new ArrayList<WifiSensorResult>();
            
            for(ScanResult scanResult : scanResults) {
                WifiSensorResult result = new WifiSensorResult(scanResult);
                
                accessPoints.add(result);
                
                Log.d("Wifi Scan Result", result.toString());
            }
            
            results.add(new SensorResult<ArrayList<WifiSensorResult>>(accessPoints));
        }
        
        public ArrayList<SensorResult<ArrayList<WifiSensorResult>>> getResults() {
            return results;
        }
    }
    
    private class BluetoothResultReceiver extends BroadcastReceiver {
        private BluetoothAdapter bluetoothAdapter;
        
        private ArrayList<SensorResult<BluetoothSensorResult>> results = new ArrayList<SensorResult<BluetoothSensorResult>>();

        public BluetoothResultReceiver(BluetoothAdapter adapter) {
            this.bluetoothAdapter = adapter;
        }

        public void onReceive(Context c, Intent i) {

            String action = i.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = i.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                results.add(new SensorResult<BluetoothSensorResult>(new BluetoothSensorResult(device)));
            }
        }
        
        public ArrayList<SensorResult<BluetoothSensorResult>> getResults() {
            return results;
        }
    }

    private class GPSLocationListener implements LocationListener {
        
        private ArrayList<SensorResult<Double[]>> results = new ArrayList<SensorResult<Double[]>>();
        
        public GPSLocationListener() {
            Log.d("GPS Listener", "Created");
        }

        public void onLocationChanged(Location location) {
            
            Double [] values = new Double[] {(double)location.getLatitude(), (double)location.getLongitude()};
            
            results.add(new SensorResult<Double[]>(values));

            String message = String.format(
                    "New Location \n Longitude: %1$s \n Latitude: %2$s",
                    location.getLongitude(),
                    location.getLatitude()
            );
            
            Log.d("GPS Listener", "Received: " + message);
        }

        public void onStatusChanged(String s, int i, Bundle b) {
            Log.d("GPS Listener", "Status changed: " + s);
        }

        public void onProviderDisabled(String s) {
            Log.d("GPS Listener", "Disabled: " + s);
        }

        public void onProviderEnabled(String s) {
            Log.d("GPS Listener", "Enabled: " + s);
        }
        
        public ArrayList<SensorResult<Double[]>> getResults() {
            return results;
        }
    }

}
