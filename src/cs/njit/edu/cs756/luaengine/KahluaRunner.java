package cs.njit.edu.cs756.luaengine;

import android.util.Log;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask.SensorReadType;
import cs.njit.edu.cs756.insomniaclient.sensor.BluetoothSensorResult;
import cs.njit.edu.cs756.insomniaclient.sensor.SensorResult;
import cs.njit.edu.cs756.insomniaclient.sensor.WifiSensorResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import se.krka.kahlua.converter.KahluaConverterManager;
import se.krka.kahlua.integration.LuaCaller;
import se.krka.kahlua.integration.expose.LuaJavaClassExposer;
import se.krka.kahlua.j2se.J2SEPlatform;
import se.krka.kahlua.j2se.KahluaTableImpl;
import se.krka.kahlua.luaj.compiler.LuaCompiler;
import se.krka.kahlua.vm.KahluaTable;
import se.krka.kahlua.vm.KahluaTableIterator;
import se.krka.kahlua.vm.KahluaThread;
import se.krka.kahlua.vm.LuaClosure;
import se.krka.kahlua.vm.Platform;

/**
 *
 * @author Chris O
 */
public class KahluaRunner {

    private final Platform platform = new J2SEPlatform();;
    private final KahluaTable env = platform.newEnvironment();;
    private final KahluaConverterManager manager = new KahluaConverterManager();;
    private final LuaJavaClassExposer exposer;
    private final LuaCaller caller = new LuaCaller(manager);;

    /**
     * @param args the command line arguments
     */

    public KahluaRunner() {
        KahluaTable java = platform.newTable();
        
        env.rawset("Java", java);
        exposer = new LuaJavaClassExposer(manager, platform, env, java);
        exposer.exposeGlobalFunctions(this);
    }
    
    private void executeTask(InsomniaTask task, HashMap<SensorReadType, Object> sensorResults) throws IOException {
                KahluaThread thread = new KahluaThread(platform, env);
        
        Log.d("Task Status", "Creating final Insomnia task script.");
        
        String script = LuaScripts.createInsomniaScript(task.getScript());
        
        Log.d("Task Status", "Setting up input for task execution.");

        String[] inputLines;
        
        KahluaTable inputTable = platform.newTable();
        KahluaTable sensorInputTable = platform.newTable();
        
        if (task.getInput() != null) {
            inputLines = task.getInput().split("\n");

            for (int c = 0; c < inputLines.length; c++) {
                inputTable.rawset(c + 1, inputLines[c]);
            }
        }
        
        if(sensorResults != null) {
            if(sensorResults.containsKey(SensorReadType.Accelerometer)) {
                KahluaTable accelerometerTable = platform.newTable();
                ArrayList<SensorResult<Double[]>> accelerometerResults = (ArrayList<SensorResult<Double[]>>)sensorResults.get(SensorReadType.Accelerometer);
                
                for(int c = 0; c < accelerometerResults.size(); c++) {
                    SensorResult<Double[]> result = accelerometerResults.get(c);
                    
                    KahluaTable resultTable = platform.newTable();
                    
                    resultTable.rawset("timestamp", result.timeStamp);
                    resultTable.rawset("x", result.data[0]);
                    resultTable.rawset("y", result.data[1]);
                    resultTable.rawset("z", result.data[2]);
                    
                    accelerometerTable.rawset(c + 1, resultTable);
                }
                
                sensorInputTable.rawset("accelerometer", accelerometerTable);
            }

            if (sensorResults.containsKey(SensorReadType.Gyroscope)) {
                KahluaTable gyroscopeTable = platform.newTable();
                ArrayList<SensorResult<Double[]>> gyroscopeResults = (ArrayList<SensorResult<Double[]>>) sensorResults.get(SensorReadType.Gyroscope);

                for (int c = 0; c < gyroscopeResults.size(); c++) {
                    SensorResult<Double[]> result = gyroscopeResults.get(c);

                    KahluaTable resultTable = platform.newTable();

                    resultTable.rawset("timestamp", result.timeStamp);
                    resultTable.rawset("x", result.data[0]);
                    resultTable.rawset("y", result.data[1]);
                    resultTable.rawset("z", result.data[2]);

                    gyroscopeTable.rawset(c + 1, resultTable);
                }
                
                sensorInputTable.rawset("gyroscope", gyroscopeTable);
            }
            
            if (sensorResults.containsKey(SensorReadType.Wifi)) {
                KahluaTable wifiTable = platform.newTable();
                ArrayList<SensorResult<ArrayList<WifiSensorResult>>> wifiResults = (ArrayList<SensorResult<ArrayList<WifiSensorResult>>>) sensorResults.get(SensorReadType.Wifi);

                for (int c = 0; c < wifiResults.size(); c++) {
                    SensorResult<ArrayList<WifiSensorResult>> result = wifiResults.get(c);

                    KahluaTable resultTable = platform.newTable();

                    resultTable.rawset("timestamp", result.timeStamp);
                    
                    KahluaTable accessPointsTable = platform.newTable();
                    
                    ArrayList<WifiSensorResult> accessPoints = result.data;
                    
                    for(int a = 0; a < accessPoints.size(); a++) {
                        WifiSensorResult accessPoint = accessPoints.get(a);
                        
                        KahluaTable accessPointTable = platform.newTable();
                        
                        accessPointTable.rawset("ssid", accessPoint.SSID);
                        accessPointTable.rawset("bssid", accessPoint.BSSID);
                        accessPointTable.rawset("frequency", accessPoint.frequency);
                        accessPointTable.rawset("level", accessPoint.level);
                        
                        accessPointsTable.rawset(a + 1, accessPointTable);
                    }
                    
                    resultTable.rawset("aps", accessPointsTable);

                    wifiTable.rawset(c + 1, resultTable);
                }
                
                sensorInputTable.rawset("wifi", wifiTable);
            }
            
            if (sensorResults.containsKey(SensorReadType.GPS)) {
                KahluaTable gpsTable = platform.newTable();
                ArrayList<SensorResult<Double[]>> gpsResults = (ArrayList<SensorResult<Double[]>>) sensorResults.get(SensorReadType.GPS);

                for (int c = 0; c < gpsResults.size(); c++) {
                    SensorResult<Double[]> result = gpsResults.get(c);

                    KahluaTable resultTable = platform.newTable();

                    resultTable.rawset("timestamp", result.timeStamp);
                    resultTable.rawset("lat", result.data[0]);
                    resultTable.rawset("lon", result.data[1]);

                    gpsTable.rawset(c + 1, resultTable);
                }
                
                sensorInputTable.rawset("gps", gpsTable);
            }
            
            if(sensorResults.containsKey(SensorReadType.Bluetooth)) {
                KahluaTable bluetoothTable = platform.newTable();
                ArrayList<SensorResult<BluetoothSensorResult>> bluetoothResults = (ArrayList<SensorResult<BluetoothSensorResult>>) sensorResults.get(SensorReadType.Bluetooth);
                
                for(int c = 0; c < bluetoothResults.size(); c++) {
                    SensorResult<BluetoothSensorResult> result = bluetoothResults.get(c);
                    
                    KahluaTable resultTable = platform.newTable();
                    
                    resultTable.rawset("timestamp", result.timeStamp);
                    resultTable.rawset("address", result.data.address);
                    resultTable.rawset("name", result.data.name);
                    resultTable.rawset("bluetoothclass", result.data.bluetoothClass);
                    
                    bluetoothTable.rawset(c + 1, resultTable);
                }
                
                sensorInputTable.rawset("bluetooth", bluetoothTable);
            }
        }

        long startTime = System.currentTimeMillis();

        Log.d("Task Status", "Compiling task script.");
        
        LuaClosure closure = LuaCompiler.loadstring(script, "Task", env);

        Log.d("Task Status", "Executing script.");
        
        env.rawset("input", inputTable);
        env.rawset("sensorInput", sensorInputTable);
        
        thread.call(closure, null);
        
        KahluaTableImpl output = (KahluaTableImpl)env.rawget("output");
        
        long endTime = System.currentTimeMillis();
        
        task.setExecutionTime(endTime - startTime);
        
        Log.d("Task Status", "Executing successful. Retrieving output.");
        
        ArrayList<String> outputLines = new ArrayList<String>();
        
        KahluaTableIterator iterator = output.iterator();
        
        while(iterator.advance()) {
            String value = iterator.getValue().toString();
            outputLines.add(value);
        }
        
        task.setOutput(outputLines);
        
        Log.d("Task Status", "Task execution complete.");
    }
    
    public void executeComputationTask(InsomniaTask task) throws IOException {
        executeTask(task, null);
    }
    
    public void executeSensingTask(InsomniaTask task, HashMap<SensorReadType, Object> sensingResults) throws IOException {
        executeTask(task, sensingResults);
    }
}
