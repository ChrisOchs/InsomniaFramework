package cs.njit.edu.cs756.insomniaclient.sensor;

import android.net.wifi.ScanResult;

/**
 *
 * @author Chris O
 */
public class WifiSensorResult {
    public String SSID;
    public String BSSID;
    public int frequency;
    public int level;
    
    public WifiSensorResult(ScanResult scanResult) {
        this.BSSID = scanResult.BSSID;
        this.SSID = scanResult.SSID;
        this.frequency = scanResult.frequency;
        this.level = scanResult.level;
    }
    
    public String toString() {
        return SSID + " | " + BSSID + " | " + frequency + " | " + level;
    }
}
