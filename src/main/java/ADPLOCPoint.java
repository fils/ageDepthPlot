/*
 * ADPLOCPoint.java
 *
 * Created on December 15, 2004, 3:07 PM
 * 12/20/04 1:57 PM:  Adding KeyDatumName
 * 22-Dec-04:  Adding setKeyDatum and setComment
 */

/**
 * A control point on the line of correlation; these used to be just ADPAgeDepthPoints,
 * but now need key datum index and comment. 
 *
 * @version 1.10 05-Jan-2005
 * @author gcb
 */

import java.util.*;

public class ADPLOCPoint extends ADPDataType {
    
    public double Age;
    public double Depth;
    public int KeyDatumIndex;    // index of strat event (datum) on which this point is hung; 0 if none (strat event indices start at one)
    public String KeyDatumName;  // name of strat event on which point is hung
    public String Comment;
    
    /** Creates a new instance of ADPLOCPoint */
    public ADPLOCPoint() {
    }
    
    /** Creates a new instance of ADPLOCPoint from just age and depth */
    public ADPLOCPoint( double age, double depth ) {
        Age = age;
        Depth = depth;
        KeyDatumIndex = 0;
        KeyDatumName = "[None]";
        Comment = "";
    }
    
    /** Creates a new instance of ADPLOCPoint using all four fields */
    public ADPLOCPoint( double age, double depth, int kdi, String comment ) {
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        Age = age;
        Depth = depth;
        if ( ADPApp.StratEvents==null ) {  // test added 05-Jan-2005
        		KeyDatumIndex = 0;
        		KeyDatumName = "[None]";
        } else {
	        KeyDatumIndex = kdi;
	        if ( kdi == 0 ) {
	            KeyDatumName = "[None]";
	        } else {
	            ADPStratEvent se = (ADPStratEvent) ADPApp.StratEvents.get(new Integer(kdi));
	            KeyDatumName = se.getDatumLabel();
	        }
	    	}
        Comment = comment;
    }
    
    public String toString() {
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        return( "Age: " + df2.format(Age) + ", " +
                "Depth: " + df2.format(Depth) + ", " +
                "Key datum: " + KeyDatumName + ", " +
                "Comment: " + Comment );
    }
    
    public String[] fieldNames() {
        String[] fldNames = { "Age",
                              "Depth",
                              "KeyDatum",
                              "Comment" };
        return fldNames;        
    }
    
    public Vector toVector() {
        Vector vec = new Vector();
        vec.addElement( new Double(Age) );
        vec.addElement( new Double(Depth) );
        vec.addElement( new String(KeyDatumName) );
        vec.addElement( new String(Comment) );
        return vec;
    }
    
    /**
     * Sets KeyDatumIndex and KeyDatumName; added 12/22/04 4:35 PM
     * @param datumLabel Datum label associated with one of the strat events; KeyDatumIndex is 0 if no matc
     */
    public void setKeyDatum( String datumLabel ) {
        //System.out.println( "datumLabel: " + datumLabel );
        KeyDatumIndex = 0;   // initialize to no-match case
        KeyDatumName = "[None]";
        if ( ADPApp.StratEvents != null ) {
	        int nevt = ADPApp.StratEvents.size();
	        for (int i=1; i<=nevt; i++) {
	            ADPStratEvent se = (ADPStratEvent) ADPApp.StratEvents.get(new Integer(i));
	            if ( se.getDatumLabel().equals(datumLabel) ) {
	                KeyDatumIndex = i;
	                KeyDatumName = se.getDatumLabel();
	                break;
	            }
	        }
        }
        //System.out.println( "KeyDatumIndex: " + KeyDatumIndex );
    }
    
    public void setComment(String comment) {
        Comment = comment;
    }
    
}
