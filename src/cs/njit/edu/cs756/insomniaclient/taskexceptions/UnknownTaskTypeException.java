package cs.njit.edu.cs756.insomniaclient.taskexceptions;

/**
 *
 * @author Chris O
 */
public class UnknownTaskTypeException extends Exception {
    public UnknownTaskTypeException() {
        super("Unknown task type encountered.");
    }
}
