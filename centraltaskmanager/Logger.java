package centraltaskmanager;

/**
 *
 * @author Chris
 */
public class Logger {
    public static void doLog(Object source, String type, String message) {
        System.out.println(String.format("%d|%s|%s: %s", 
                System.currentTimeMillis(), source.getClass().getSimpleName(), type, message));
    }
}
