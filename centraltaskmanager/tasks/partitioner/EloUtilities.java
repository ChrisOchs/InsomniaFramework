/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package centraltaskmanager.tasks.partitioner;

import java.util.Arrays;

/**
 *
 * @author Chris
 */
public class EloUtilities {
    
    public static final double K_SCORE = 42;
    
    public static double standardDeviation(double [] values) {
        double sum = 0;
        
        if(values.length < 2) {
            return Double.NaN;
        }
        
        for(double value : values) {
            sum += value;
        }
        
        double mean = sum / values.length;
        
        sum = 0;
        
        for(double value : values) {
            sum += Math.pow(value - mean, 2);
        }
        
        return Math.sqrt((1.0/(values.length - 1.0)) * sum);
    }
    
    public static double EloExpected(double device1Score, double device2Score) {
        return (1 / (1 + Math.pow(10, (device2Score - device1Score) / 400)));
    }
}
