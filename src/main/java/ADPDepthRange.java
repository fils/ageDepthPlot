/*
 * ADPDepthRange.java
 *
 * Created on December 10, 2003, 1:07 PM
 * Last modified by gcb on 12/14/03 3:27 PM
 * 05-Apr-04: Adding javadoc comments
 */

/**
 * A depth range
 *
 * @version 0.90 07-Apr-04
 * @author  gcb
 */

public class ADPDepthRange {    
 
    /** The minimum depth */
    public double MinDepth;
    /** The maximum depth */
    public double MaxDepth;
    
    /** Creates a new instance of ADPDepthRange, all NaN */
    public ADPDepthRange() {
        MinDepth = Double.NaN;
        MaxDepth = Double.NaN;
    }
    
   /** Creates a new instance of ADPDepthRange from two doubles*/
    public ADPDepthRange(double mindepth, double maxdepth) {
        MinDepth = mindepth;
        MaxDepth = maxdepth;
    }    
    
}
