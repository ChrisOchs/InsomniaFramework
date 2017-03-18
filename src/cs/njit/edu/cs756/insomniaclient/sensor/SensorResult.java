package cs.njit.edu.cs756.insomniaclient.sensor;

/**
 *
 * @author Chris O
 */
public class SensorResult<T> {
    public long timeStamp = System.currentTimeMillis();
    
    public T data;
    
    public SensorResult(T data) {
        this.data = data;
    }
}
