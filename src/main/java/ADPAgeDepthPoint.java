/*
 * ADPAgeDepthPoint.java
 *
 * Created on December 20, 2003, 8:08 PM
 * 
 * 05-Apr-04: Adding javadoc comments
 */

/**
 * An age-depth data point, as two doubles
 *
 * @version 0.90 07-Apr-04
 * @author  gcb
 */

public class ADPAgeDepthPoint {
    
    /** The age of the event, in millions of years (Ma) before present */
    public double Age;
    /** The depth of the event, in meters below sea floor (mbsf) */
    public double Depth;
    
    /** Creates a new instance of ADPAgeDepthPoint filled with NaN's */
    public ADPAgeDepthPoint( ) {
        Age = Double.NaN;
        Depth = Double.NaN;
    }
    
    /**
     * Creates a new instance of ADPAgeDepthPoint using two doubles
     *
     * @param age the age of the event
     * @param depth the depth of the event
     */
    public ADPAgeDepthPoint( double age, double depth ) {
        Age = age;
        Depth = depth;
    }
    
}
