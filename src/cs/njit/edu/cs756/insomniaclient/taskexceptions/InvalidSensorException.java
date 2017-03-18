package cs.njit.edu.cs756.insomniaclient.taskexceptions;

/**
 *
 * @author Chris O
 */
public class InvalidSensorException extends Exception {
    public InvalidSensorException(String type) {
        super("Invalid sensor accessed: " + type);
    }
}
