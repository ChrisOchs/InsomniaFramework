package centraltaskmanager.tasks;

import centraltaskmanager.FileHelper;
import centraltaskmanager.Options;
import centraltaskmanager.devices.Device;
import centraltaskmanager.devices.DeviceManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author Chris
 */
public class Task {
    
    private long taskId;
    
    private String taskName;
    
    private ArrayList<File> inputFiles = new ArrayList<File>();
    
    private ArrayList<File> scriptFiles = new ArrayList<File>();
    
    private boolean initialized = false;
    
    private boolean executing = false;
    
    private String directoryStr;

    private final List<Tasklette> assignedTasklettes = new ArrayList<Tasklette>();
    private final List<Tasklette> executingTasklettes = Collections.synchronizedList(new ArrayList<Tasklette>());
    private final List<Tasklette> completedTasklettes = Collections.synchronizedList(new ArrayList<Tasklette>());
    
    private TaskManager taskManager;
    
    protected Task(TaskManager taskManager, long taskId, String taskName) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskManager = taskManager;
        
        this.directoryStr = String.format("tasks/task_%d/", this.taskId);

        try {
            
            if(!Options.DEBUG_MODE) {
                createDataStore();
            } else {
                inputFiles.add(new File(directoryStr + "input/numbers.txt"));
                scriptFiles.add(new File(directoryStr +  "scripts/script.txt"));
            }
            
            initialized = true;
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    
    public boolean isInitialized() {
        return initialized;
    }

    public long getId() {
        return taskId;
    }
    
    private void createDataStore() throws IOException {
        File taskDirectory = new File(directoryStr);
        taskDirectory.mkdirs();
        
        File inputDirectory = new File(directoryStr + "input/");
        inputDirectory.mkdirs();
        
        File scriptDirectory = new File(directoryStr + "scripts/");
        scriptDirectory.mkdirs();
        
        File inputPartitionDirectory = new File(directoryStr + "inputPartition/");
        inputPartitionDirectory.mkdirs();
        
        File outputDirectory = new File(directoryStr + "output/");
        outputDirectory.mkdirs();
    }
    
    public boolean addInputFile(String fileName, int fileLength, BufferedReader fileIn) {
        File inputFile = new File(String.format("tasks/task_%d/input/%s", this.taskId, fileName));

        try {
            FileHelper.copyFromBufferedReaderToFile(fileIn, fileLength, inputFile);
        } catch(IOException ioe) {
            
            System.err.println(ioe);
            
            if(inputFile.exists()) {
                inputFile.delete();
            }
            
            return false;
        }
        
        inputFiles.add(inputFile);
        
        return true;
    }
    
    public boolean addScriptFile(String fileName, int fileLength, BufferedReader fileIn) {
        File scriptFile = new File(String.format("tasks/task_%d/scripts/%s", this.taskId, fileName));

        try {
            FileHelper.copyFromBufferedReaderToFile(fileIn, fileLength, scriptFile);
        } catch (IOException ioe) {
            
            System.err.println(ioe);
            
            if (scriptFile.exists()) {
                scriptFile.delete();
            }
            
            return false;
        }

        scriptFiles.add(scriptFile);

        return true;
    }

    public void executeTask(DeviceManager deviceManager) throws UninitializedTaskException {

        if (!initialized) {
            throw new UninitializedTaskException("Task " + taskId + " uninitialized.");
        }

        ArrayList<Device> devices;

        if (this instanceof ComputationTask) {
            devices = deviceManager.getAvailableDevices();
        } else if (this instanceof SensingTask) { //Blargh
            devices = deviceManager.getAvailableDevicesWithSensors(((SensingTask) this).getSensorTypes());
        } else { // TODO: Photo Task
            devices = deviceManager.getAvailableDevices();
        }

        ArrayList<Device> chosenDevices = new ArrayList<Device>();

        final int DEVICE_LIMIT = 4;

        if (devices.size() > DEVICE_LIMIT) {
            for (int c = 0; c < DEVICE_LIMIT; c++) {
                int randomIndex = (int) (Math.random() * devices.size());

                Device device = devices.get(randomIndex);

                while (chosenDevices.contains(device)) {
                    randomIndex = (int) (Math.random() * devices.size());
                    device = devices.get(randomIndex);
                }

                chosenDevices.add(device);
            }
        } else {

            if (devices.isEmpty()) {
                return;
            } else {
                chosenDevices.addAll(devices);
            }
        }

        if (this instanceof ComputationTask) { // BLARGH
            File inputFile = inputFiles.get(0);

            try {
                Scanner scanner = new Scanner(inputFile);

                ArrayList<String> lines = new ArrayList<String>();

                while (scanner.hasNext()) {
                    lines.add(scanner.nextLine());
                }
                
                int [] deviceLines = new int[devices.size()];
                
                final int mustBeat = devices.size() / 2; // 50% of devices
                
                final double withPercentage = 0.95;
                
                final int regularShare = lines.size() / devices.size();
                
                final double extraSharePercent = 1.3;
                
                final int bonusShare = (int)((double)regularShare * extraSharePercent);
                
                int devicesGivenExtra = 0;
                
                int nonBonusLines = lines.size();
                
                for(int i = 0; i < devices.size(); i++) {
                    Device device = devices.get(i);
                    int beatCount = 0;
                    
                    for(int j = 0; j < devices.size(); j++) {
                        if(i != j) {
                            
                            Device otherDevice = devices.get(j);
                            
                            // Split the work among devices based on relative power
                            // Test version used Elo scoring
                        }
                    }
                    
                    if(beatCount > mustBeat) {
                        deviceLines[i] = bonusShare;
                        devicesGivenExtra++;
                        nonBonusLines -= bonusShare;
                    } else {
                        deviceLines[i] = -1;
                    }
                }
                
                int remainingSharePerDevice = nonBonusLines / (devices.size() - devicesGivenExtra);
                
                for(int c = 0; c < deviceLines.length; c++) {
                    if(deviceLines[c] == -1) {
                        deviceLines[c] = remainingSharePerDevice;
                    }
                }
                
                int processedLines = 0;

                for (int c = 0; c < chosenDevices.size(); c++) {
                    int lineCount = deviceLines[c];

                    List<String> partitionLines;

                    if (c != chosenDevices.size() - 1) {
                        partitionLines = lines.subList(processedLines, processedLines + lineCount);
                    } else {
                        partitionLines = lines.subList(processedLines, lines.size());
                    }

                    String filePartitionName = inputFile.getName();

                    filePartitionName = String.format("%s_%d.txt",
                            filePartitionName.substring(0, filePartitionName.lastIndexOf(".")),
                            c);

                    File partitionFile = new File(directoryStr + "inputPartition/" + filePartitionName);
                    File outputFile = new File(directoryStr + "output/" + filePartitionName);

                    PrintWriter writer = new PrintWriter(partitionFile);

                    for (String line : partitionLines) {
                        writer.println(line);
                    }

                    writer.close();

                    processedLines += lineCount;

                    Tasklette tasklette = new Tasklette(this, partitionFile, outputFile);
                    tasklette.setDeviceExecutedOn(chosenDevices.get(c));
                    chosenDevices.get(c).assignTask(tasklette);

                    assignedTasklettes.add(tasklette);
                }

                this.executing = true;

            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else if (this instanceof SensingTask) {
            File inputFile = inputFiles.get(0);

            int processedLines = 0;

            for (int c = 0; c < chosenDevices.size(); c++) {
                String filePartitionName = inputFile.getName();

                filePartitionName = String.format("%s_%d.txt",
                        filePartitionName.substring(0, filePartitionName.lastIndexOf(".")),
                        c);

                File outputFile = new File(directoryStr + "output/" + filePartitionName);

                Tasklette tasklette = new Tasklette(this, inputFile, outputFile);
                tasklette.setDeviceExecutedOn(chosenDevices.get(c));
                chosenDevices.get(c).assignTask(tasklette);

                assignedTasklettes.add(tasklette);
            }

            this.executing = true;
        } else if (this instanceof PhotoTask) {
            
        }
    }
    
    public void update() {
        if(executing && !isComplete()) {
            
            synchronized(executingTasklettes) {
                for(Tasklette tasklette : executingTasklettes) {
                    if(tasklette.serverExecutionTime() > 60000) {
                        // Reassign delayed tasks, or report error
                    }
                }
            }
        }
    }
    
    private class DeviceExecution {
        public Device device;
        public long executionTime;
        public double eloDelta = 0;
        
        public DeviceExecution(Device device, long executionTime) {
            this.device = device;
            this.executionTime = executionTime;
        }
    }

    
    public boolean isComplete() {
        return executing && assignedTasklettes.isEmpty() && executingTasklettes.isEmpty();
    }
    
    public void taskletteExecuting(Tasklette tasklette) {
        assignedTasklettes.remove(tasklette);
        executingTasklettes.add(tasklette);
    }
    
    public void taskletteComplete(Tasklette tasklette) {
        executingTasklettes.remove(tasklette);
        completedTasklettes.add(tasklette);
    }
    
    public ArrayList<File> getScriptFiles() {
        return scriptFiles;
    }
}
