/*
 * ADPAxisParameters.java
 *
 * Created on January 17, 2004, 2:08 PM
 * 05-Apr-04:  Adding javadoc comments
 */

/**
 * Axis parameters for age-depth plot
 *
 * @version 0.90 07-Apr-04
 * @author  gcb
 */

public class ADPAxisParameters {
    /** The minimum depth value (top of depth axis) */
    public double MinDepth;
    /** The maximum depth value (bottom end of depth axis) */
    public double MaxDepth;
    /** The major (labeled) increment along the depth axis */
    public double DepthIncMajor;
    /** The minor (unlabeled) increment along the depth axis */
    public double DepthIncMinor;
    /** The minimum age (left end of age axis) */
    public double MinAge;
    /** The maximum age (right end of age axis) */
    public double MaxAge;
    /** The major (labeled) increment along the age axis */
    public double AgeIncMajor;
    /** The minor (unlabeled) increment along the age axis */
    public double AgeIncMinor;
    
    /** Creates a new (empty) instance of ADPAxisParameters */
    public ADPAxisParameters() {
    }
    
    /**
     * Creates a new (instance of ADPAxisParameters using eight doubles
     *
     * @param dmin minimum depth
     * @param dmax maximum depth
     * @param dincMajor major depth increment
     * @param dincMinor minor depth increment
     * @param amin minimum age
     * @param amax maximum age
     * @param aincMajor major age increment
     * @param aincMinor minor age increment
     */
    
    public ADPAxisParameters( double dmin, double dmax, double dincMajor, double dincMinor,
                              double amin, double amax, double aincMajor, double aincMinor ) {
        MinDepth = dmin;
        MaxDepth = dmax;
        DepthIncMajor = dincMajor;
        DepthIncMinor = dincMinor;
        MinAge = amin;
        MaxAge = amax;
        AgeIncMajor = aincMajor;
        AgeIncMinor = aincMinor;
    }
    
    /**
     * Creates a new instance of ADPAxis parameters from an existing instance
     *
     *  @param adpAP existing set of axis parameters to copy to new one
     */
    
    public ADPAxisParameters( ADPAxisParameters adpAP ) {
        MinDepth = adpAP.MinDepth;
        MaxDepth = adpAP.MaxDepth;
        DepthIncMajor = adpAP.DepthIncMajor;
        DepthIncMinor = adpAP.DepthIncMinor;
        MinAge = adpAP.MinAge;
        MaxAge = adpAP.MaxAge;
        AgeIncMajor = adpAP.AgeIncMajor;
        AgeIncMinor = adpAP.AgeIncMinor;
    }

}
