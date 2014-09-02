/*
 * ADPStratEvent.java
 *
 * Created by gcb on December 10, 2003, 6:40 PM
 * Last modified by gcb on 12/14/03 3:29 PM
 * 05-Apr-04:  Adding javadoc comments
 * 17-June-04:  Do not draw if plot group for event is toggled off . . .
 * 18-June-04:  Adding (optional) plotting of labels . . .
 * 06-Oct-04:  Adding toString()
 * 07-Oct-04:  A change to toString() . . . switching to printing of numerical value age, depths
 * 13-Dec-2004:  Making an extension of ADPDataType
 * 15-Dec-2004:  Adding DatumID
 * 22-Dec-2004:  Adding getDatumString
 */

/**
 * A stratigraphic event.
 *
 * @version 1.10 22-Dec-2004
 * @author  gcb
 */

import java.util.*;
import java.awt.*;

public class ADPStratEvent extends ADPDataType {
    /** The plotting group for the event -- different groups plot with different symbols */
    public String EventGroup;
    /** The name of the event (e.g., scientific name of crispy critter) */
    public String EventName;
    /** The short plotting label for the event -- not really used at the moment */
    public String EventLabel;
    /** Event's min age as read from strat event file (may be empty) */
    public String strMinAge;
    /** Event's max age as read from strat event file (may be empty) */
    public String strMaxAge;
    /** Event's min depth as read from strat event file (may be in core-section,cm format or empty) */
    public String strMinDepth;
    /** Event's max depth as read from strat event file (may be in core-section,cm format or empty) */
    public String strMaxDepth;
    /** Event's min age as double (may be NaN) */
    public double MinAge;
    /** Event's max age as double (may be NaN) */
    public double MaxAge;
    /** Midpoint of events age range */
    public double MidAge;
    /** Event's min depth as double (may be NaN) */
    public double MinDepth;
    /** Event's max depth as double (may be NaN) */
    public double MaxDepth;
    /** Midpoint of event's depth range */
    public double MidDepth;
    /** datum_id from neptune_datum_def table; 0 for locally-read data; added 12/15/04 2:14 PM */
    public int DatumID;
    
    /**
     * Creates a new instance of ADPStratEvent from ugly long list of arguments
     *
     * @param grp the event's plot group
     * @param evtname the event's name
     * @param evtlabel the event's short label
     * @param minage string representation of min age as read from file (may be empty)
     * @param maxage string representation of max age as read from file (may be empty)
     * @param mindepth string representation of min depth as read from file (may be in core-section,cm format or empty)
     * @param maxdepth string representation of max dept as read from file (may be in core-section,cm format or empty)
     * @param datumid datum_id field from neptune_datum_def field or 0 for local data; added 12/15/04 2:58 PM
     */
    
    public ADPStratEvent(String grp, String evtname, String evtlabel, String minage, String maxage, String mindepth, String maxdepth, int datumid ) {
        EventGroup = grp;
        EventName = evtname;
        EventLabel = evtlabel;
        strMinAge = minage;
        if ( strMinAge == null ) {
            MinAge = Double.NaN;
        } else {
            try {
                MinAge = Double.parseDouble(strMinAge);
            } catch ( NumberFormatException nfe ) {
                MinAge = Double.NaN;
            }
        }
        strMaxAge = maxage;
        if ( strMaxAge == null ) {
            MaxAge = Double.NaN;
        } else {
            try {
                MaxAge = Double.parseDouble(strMaxAge);
            } catch ( NumberFormatException nfe ) {
                MaxAge = Double.NaN;
            }
        }
        if ( Double.isNaN(MaxAge) ) {
            MidAge = MinAge;
        } else if ( Double.isNaN(MinAge) ) {
            MidAge = MaxAge;
        } else {
            MidAge = 0.5*(MinAge+MaxAge);
        }
        strMinDepth = mindepth;  // interpret min depth & max depth using setDepthRange . . .
        strMaxDepth = maxdepth;
        if ( strMinDepth != null && strMinDepth.startsWith("\"") && strMinDepth.endsWith("\"") ) {  // strip off quotes
            strMinDepth = strMinDepth.substring(1,strMinDepth.length()-1);
        }
        if ( strMaxDepth != null && strMaxDepth.startsWith("\"") && strMaxDepth.endsWith("\"") ) {  // strip off quotes
            strMaxDepth = strMaxDepth.substring(1,strMaxDepth.length()-1);
        }
        DatumID = datumid;  // 0 for locally-read data . . .
        //System.out.println( EventGroup + ", " + EventName + ", " + EventLabel + ", " + strMinAge + ", " + strMaxAge + ", " + strMinDepth + ", " + strMaxDepth );
    }
    
   
    /**
     * Tries to turn string representation of min and max depth for event into
     * numeric min and max depths for event, using core-depth data.
     *
     * @param CoreDepths the Hashtable of depth ranges for each core
     */
    
    public void setDepthRange( Hashtable CoreDepths ) {
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        if ( strMinDepth == null ) {
            MinDepth = Double.NaN;
        } else {
            try {
                MinDepth = Double.parseDouble( strMinDepth );  // if succeeds, depth is already mbsf (presumably)
            } catch ( NumberFormatException nfe ) {  // depth in core-section,cm format
                if ( CoreDepths==null ) {
                    MinDepth = Double.NaN;
                } else {
                    try {
                        StringTokenizer biff = new StringTokenizer( strMinDepth, "-" );
                        Integer coreid = new Integer( Integer.parseInt( biff.nextToken() ) );  // before the dash
                        ADPDepthRange cdr = (ADPDepthRange) CoreDepths.get( coreid );
                        String secnocm = biff.nextToken();  // after the dash
                        //System.out.println( "secnocm: " + secnocm );
                        if ( secnocm.toUpperCase().equals("CC") ) {
                            MinDepth = cdr.MaxDepth;
                        } else {
                            biff = new StringTokenizer( secnocm, "," );
                            int secno = Integer.parseInt( biff.nextToken() );
                            int cm = Integer.parseInt( biff.nextToken() );
                            MinDepth = cdr.MinDepth + (secno-1.0)*1.5 + 0.01*cm;
                            //System.out.println( "coreid, secno, cm, MinDepth: " + coreid + ", " + secno + ", " + cm + ", " + df2.format( MinDepth ) );
                        }
                    } catch ( Exception ex ) {
                        //System.out.println( ex.getMessage() );
                        MinDepth = Double.NaN;
                    }
                }
            }
        }
        
        if ( strMaxDepth == null ) {
            MaxDepth = Double.NaN;
        } else {
            try {
                MaxDepth = Double.parseDouble( strMaxDepth );  // if succeeds, depth is already mbsf (presumably)
            } catch ( NumberFormatException nfe ) {  // depth in core-section,cm format
                if ( CoreDepths==null ) {
                    MaxDepth = Double.NaN;
                } else {
                    try {
                        StringTokenizer biff = new StringTokenizer( strMaxDepth, "-" );
                        Integer coreid = new Integer( Integer.parseInt( biff.nextToken() ) );  // before the dash
                        ADPDepthRange cdr = (ADPDepthRange) CoreDepths.get( coreid );
                        String secnocm = biff.nextToken();  // after the dash
                        //System.out.println( "secnocm: " + secnocm );
                        if ( secnocm.toUpperCase().equals("CC") ) {
                            MaxDepth = cdr.MaxDepth;
                        } else {
                            biff = new StringTokenizer( secnocm, "," );
                            int secno = Integer.parseInt( biff.nextToken() );
                            int cm = Integer.parseInt( biff.nextToken() );
                            MaxDepth = cdr.MinDepth + (secno-1.0)*1.5 + 0.01*cm;
                            //System.out.println( "coreid, secno, cm, MaxDepth: " + coreid + ", " + secno + ", " + cm + ", " + df2.format( MinDepth ) );
                        }
                    } catch ( Exception ex ) {
                        //System.out.println( ex.getMessage() );
                        MaxDepth = Double.NaN;
                    }
                }
            }
        }  
        
        if ( Double.isNaN(MaxDepth) ) {
            MidDepth = MinDepth;
        } else if ( Double.isNaN(MinDepth) ) {
            MidDepth = MaxDepth;
        } else {
            MidDepth = 0.5*(MinDepth+MaxDepth);
        }
   
    }
    
    /**
     * Draws the symbol for the event on the plot, including age and depth ranges
     *
     * @param g the graphics context
     * @param xmg pixel location of left-hand plot margin
     * @param ymg pixel location of upper plot margin
     * @param RefAge the age associated with the left-hand plot margin
     * @param RefDepth the depth associated with the upper plot margin
     * @param xScale the scale for the age axis (pixels/Ma)
     * @param yScale the scale for the depth axis (pixels/meter)
     */
    
    public void draw( Graphics g, int xmg, int ymg, double RefAge, double RefDepth, double xScale, double yScale, boolean ShowLabel ) {
        //java.text.DecimalFormat df2 = new java.text.DecimalFormat( "0.00" );
        /* skip if plot code not matched or this group is toggled off (added 17-June-04) */
        /* adding plotting of labels */
        Font CurrentFont = g.getFont();
        Font SmallBold = new Font("Serif", Font.PLAIN, 10);
        int igrp = ADPSymbolPalette.PlotCodes.indexOf(EventGroup);
        if ( igrp<0 ) return;
        boolean GroupOn = true;
        try {
            GroupOn = ( (Boolean) ADPSymbolPalette.PlotGroupOn.get(igrp) ).booleanValue();
        } catch ( Exception x ) {
            return;
        }
        if ( !GroupOn ) return;        
        if ( Double.isNaN(MidAge) || Double.isNaN(MidDepth) ) return;
        if ( !Double.isNaN(MinAge) && !Double.isNaN(MaxAge) ) {
            //System.out.println( "MinAge, MaxAge, MidDepth: " + df2.format(MinAge) + ", " + df2.format(MaxAge) + ", " + df2.format(MidDepth) );
            g.drawLine( xmg+(int)(xScale*(MinAge-RefAge)), ymg+(int)(yScale*(MidDepth-RefDepth)), xmg+(int)(xScale*(MaxAge-RefAge)), ymg+(int)(yScale*(MidDepth-RefDepth)) );
        }
        if ( !Double.isNaN(MinDepth) && !Double.isNaN(MaxDepth) ) {
            //System.out.println( "MinDepth, MaxDepth, MidAge: " + df2.format(MinDepth) + ", " + df2.format(MaxDepth) + ", " + df2.format(MidAge) );
            g.drawLine( xmg+(int)(xScale*(MidAge-RefAge)), ymg+(int)(yScale*(MinDepth-RefDepth)), xmg+(int)(xScale*(MidAge-RefAge)), ymg+(int)(yScale*(MaxDepth-RefDepth)) );
            //g.drawLine( xmg+(int)(xScale*MidAge)+1, ymg+(int)(yScale*MinDepth), xmg+(int)(xScale*MidAge)+1, ymg+(int)(yScale*MaxDepth) );
        }
        if ( EventGroup != null ) {  // draw a centered symbol
            int ix = xmg+(int)(xScale*(MidAge-RefAge));
            int iy = ymg+(int)(yScale*(MidDepth-RefDepth));
            ADPSymbolPalette.drawSymbol( g, ix, iy, EventGroup );
            if (ShowLabel) {
                g.setFont( SmallBold );
                g.drawString( EventLabel, ix+7, iy-3 );
                g.setFont( CurrentFont );              
            }
        }
        
        
    }
    
    public String toString() {
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        return  EventGroup + "\t" + EventName + "\t" + EventLabel + "\t" +
                (Double.isNaN(MinAge) ? "" : df2.format(MinAge)) + "\t" +
                (Double.isNaN(MaxAge) ? "" : df2.format(MaxAge)) + "\t" +
                (Double.isNaN(MinDepth) ? "" : df2.format(MinDepth)) + "\t" +
                (Double.isNaN(MaxDepth) ? "" : df2.format(MaxDepth));
    }

    /** @return components of object packed into a Vector (keeping a few behind the scenes) */
    public Vector toVector() {
        Vector vec = new Vector();
        vec.addElement( new String(EventGroup) );
        vec.addElement( new String(EventName) );
        vec.addElement( new String(EventLabel) );
        vec.addElement( new Double(MinAge) );
        vec.addElement( new Double(MaxAge) );
        vec.addElement( new Double(MinDepth) );
        vec.addElement( new Double(MaxDepth) );
        //vec.addElement( new Integer(DatumID) );
        return vec;     
    }
       
    /** @return array of names of components of object (also keeping a few behind the scenes) */
    public String[] fieldNames() {
        String[] fldNames = { "Event Group",
                              "Event Name",
                              "Event Label",
                              "Min. Age",
                              "Max. Age",
                              "Min. Depth",
                              "Max. Depth" };

        return fldNames;
    }
    
    /**
     * for populating KeyDatumName field in LOCDataTable; added 12/22/04 4:19 PM
     * @return concatenation of MidAge, MidDepth, and EventName
     */
    public String getDatumLabel() {
        java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        return "[" + df2.format(MidAge) + " Ma, " + df2.format(MidDepth) + " mbsf]: " + EventName;
    }

    
}
