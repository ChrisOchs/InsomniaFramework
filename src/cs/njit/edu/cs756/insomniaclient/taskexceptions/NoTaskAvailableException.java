package cs.njit.edu.cs756.insomniaclient.taskexceptions;

/**
 *
 * @author Chris O
 */
public class NoTaskAvailableException extends Exception {
    public NoTaskAvailableException() {
        super("No tasks assigned.");
    }
}
