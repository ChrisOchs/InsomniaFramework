package cs.njit.edu.cs756.insomniaclient;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask.SensorReadType;
import cs.njit.edu.cs756.insomniaclient.InsomniaTask.TaskType;
import cs.njit.edu.cs756.insomniaclient.sensor.SensorReader;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.InvalidSensorException;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.NoTaskAvailableException;
import cs.njit.edu.cs756.insomniaclient.taskexceptions.UnknownTaskTypeException;
import cs.njit.edu.cs756.luaengine.KahluaRunner;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class InsomniaClient extends Activity {
    
    private TaskHandler taskHandler;
    
    private DeviceHandler deviceHandler;
    
    private TextView outputTextView;
    
    private KahluaRunner taskRunner;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);

        outputTextView = (TextView) this.findViewById(R.id.testTextView);
        
        taskHandler = new TaskHandler();
        
        deviceHandler = new DeviceHandler();
        deviceHandler.initializeDeviceInformation(this);
        
        taskRunner = new KahluaRunner();
    }

    protected void onRestart() {
        super.onRestart();
    }

    protected void onResume() {
        super.onResume();
    }
    
    public void retrieveTask(View view) {
        Log.d("Task Retrieval", "Attempting connection...");
        
        outputTextView.setText("Retrieving a task if available!");
        
        try {
            Log.d("Task Retrieval", "Creating socket...");

            Socket socket = new Socket("10.0.2.2", 44663);

            Log.d("Task Retrieval", "Connection to Central Task Manager successful.");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            InsomniaTask task;

            try {
                task = taskHandler.getTask(in, out);

                Log.d("Task Status", "Task received. Executing task.");

                outputTextView.setText("Executing script: \n\n" + task.getScript());

                if (task.getType() == TaskType.Sensing) {
                    SensorReader reader;

                    try {
                        reader = new SensorReader(this, task.getSensorsUsed(), task.getSensorReadTime());
                        Thread senseThread = new Thread(reader);

                        senseThread.start();

                        while (!reader.isComplete()) {}
                        
                        HashMap<SensorReadType, Object> results = reader.getSensorResults();
                    } catch (InvalidSensorException ise) {
                        Log.e("SensorReader Initialization", ise.getLocalizedMessage());
                    }
                } else if(task.getType() == TaskType.Computation) {
                    taskRunner.executeComputationTask(task);
                }

                Log.d("Task Status", "Task execution successful. Sending results.");
                
                taskHandler.sendTaskResults(in, out, task);

                outputTextView.setText("Task complete, execution time: " + task.getExecutionTime());
            } catch (NoTaskAvailableException ntae) {
                Log.d("Task Status", "No task assigned.");
            } catch (UnknownTaskTypeException utte) {
                Log.d("Task Error", "Unknown task type assigned.");
            }

            out.writeBytes("finished\n");

            in.close();
            out.close();
            socket.close();
        } catch (UnknownHostException uhe) {
            System.err.println(uhe);
        } catch (IOException ioe) {
            System.err.println(ioe);
        }

        Log.d("Task Retrieval", "Mobile Device Test complete");
    }
}
