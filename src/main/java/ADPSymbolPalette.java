/*
 * ADPPlotGroups.java
 *
 * Created on January 12, 2004, 8:48 PM
 * 05-Apr-04:  Adding javadoc comments
 * 17-June-04:  Adding checkboxes for legend and plot-toggling vector . . .
 * 09-Mar-05:  Trying to fix it so that plot first four plot groups are reserved...
 */

/**
 * Palette of plotting symbols for ADP; maybe a little klugey
 *
 * @version 1.15 09-Mar-05
 * @author  gcb
 */

import java.util.*;
import java.awt.*;

public class ADPSymbolPalette {
    
    /** The Vector of plot codes for each group */
    public static Vector PlotCodes;
    /** Vector of booleans determining whether group is plotted (17-June-04) */
    public static Vector PlotGroupOn;
    /** Vector determining whether group is even present, mainly for reserved groups (09-Mar-05) */
    public static Vector PlotGroupPresent;
    /** Vector of Rects representing checkbox locations; re-initialized every time legend drawn (17-June-04) */
    public static Vector PlotGroupBoxes;
    
    /** Initializes PlotCodes as new, empty Vector */
    public static void initPalette() {
        PlotCodes = new Vector();
        PlotGroupOn = new Vector();
        PlotGroupPresent = new Vector();
        PlotGroupBoxes = null;
        // Set up first four groups for Forams, Diatoms, Rads, Nannos
        // 0 - Forams
        PlotCodes.add( "F" );
        PlotGroupOn.add( new Boolean(true) );
        PlotGroupPresent.add( new Boolean(false) );
        // 1 - Diatoms
        PlotCodes.add( "D" );
        PlotGroupOn.add( new Boolean(true) );
        PlotGroupPresent.add( new Boolean(false) );
        // 2 - Rads
        PlotCodes.add( "R" );
        PlotGroupOn.add( new Boolean(true) );
        PlotGroupPresent.add( new Boolean(false) );
        // 3 - Nannos
        PlotCodes.add( "N" );
        PlotGroupOn.add( new Boolean(true) );
        PlotGroupPresent.add( new Boolean(false) );
    }
    
    /**
     * Checks whether a plot group code is in PlotCodes yet and adds it if not
     *
     * @param the plot group code
     */
    public static void addEvent( String grp ) {
    	if ( grp == null ) return;
    	// 09-Mar-05:  Trying to reserve first four slots for F, D, R, N
     	if ( grp.equals("F") ) { // Forams
    		PlotGroupOn.set(0,new Boolean(true));
    		PlotGroupPresent.set(0,new Boolean(true));
    	} else if ( grp.equals("D") ) {
    		PlotGroupOn.set(1,new Boolean(true));
    		PlotGroupPresent.set(1,new Boolean(true));
    	} else if ( grp.equals("R") ) {
    		PlotGroupOn.set(2,new Boolean(true));
     		PlotGroupPresent.set(2, new Boolean(true));
    	} else if ( grp.equals("N") ) {
    		PlotGroupOn.set(3,new Boolean(true));
    		PlotGroupPresent.set(3, new Boolean(true));   	
    	} else if ( PlotCodes.indexOf(grp) < 0 ) {
        	PlotCodes.add( grp );
            PlotGroupOn.add( new Boolean(true) );
            PlotGroupPresent.add( new Boolean(true) );
        }
    }
    
    /**
     * Draws the symbol associated with an event's plot group.  At the moment,
     * the symbol drawn is hard-wired to the numeric index of the plot group code.
     *
     * @param g the graphics context
     * @param ix the x location of the center of the symbol, in pixels
     * @param iy the y location of the center of the symbol, in pixels
     * @param grp the plot group code
     */
    public static void drawSymbol( Graphics g, int ix, int iy, String grp ) {
            int igrp = PlotCodes.indexOf(grp);
            if ( igrp < 0 ) {
                // do nothing
            } else if ( igrp==0 ) {
                // draw a red box
                g.setColor( Color.red );
                int[] xpoints = { ix-3, ix-3, ix+3, ix+3 };
                int[] ypoints = { iy-3, iy+3, iy+3, iy-3 };
                g.drawPolygon( xpoints, ypoints, 4 ); 
                g.setColor( Color.black );
            } else if ( igrp==1 ) {
                // draw a blue triangle pointing down
                g.setColor( Color.blue );
                int[] xpoints = { ix-3, ix, ix+3 };
                int[] ypoints = { iy-3, iy+3, iy-3 };
                g.drawPolygon( xpoints, ypoints, 3 );
                g.setColor( Color.black );
            } else if ( igrp==2 ) {
                // draw a black triangle pointing down 
                g.setColor( Color.black );
                int[] xpoints = { ix-3, ix, ix+3 };
                int[] ypoints = { iy+3, iy-3, iy+3 };
                g.drawPolygon( xpoints, ypoints, 3 );
                g.setColor( Color.black );
            } else if ( igrp==3 ) {
                // draw a cyan diamond
                g.setColor( Color.cyan.darker().darker().darker() );
                int[] xpoints = { ix-3, ix, ix+3, ix };
                int[] ypoints = { iy, iy+3, iy, iy-3 };
                g.drawPolygon( xpoints, ypoints, 4 );
                g.setColor( Color.black );
            } else if ( igrp==4 ) {
                // draw a brown circle
                g.setColor( new Color(200,150,150) );  // is this brown?
                g.drawOval( ix-3, iy-3, 6, 6 );
                g.setColor( Color.black );
            } else if ( igrp==5 ) {
                // draw a magenta solid box
                g.setColor( Color.magenta );
                int[] xpoints = { ix-3, ix-3, ix+3, ix+3 };
                int[] ypoints = { iy-3, iy+3, iy+3, iy-3 };
                g.fillPolygon( xpoints, ypoints, 4 );
                g.setColor( Color.black );
            } else if ( igrp==6 ) {
                // draw a green solid triangle pointed down
                g.setColor( Color.green );
                int[] xpoints = { ix-3, ix, ix+3 };
                int[] ypoints = { iy-3, iy+3, iy-3 };
                g.fillPolygon( xpoints, ypoints, 3 );
                g.setColor( Color.black );
            } else if ( igrp==7 ) {
                // draw a black solid diamond
                g.setColor( Color.black );
                int[] xpoints = { ix-3, ix, ix+3, ix };
                int[] ypoints = { iy, iy+3, iy, iy-3 };
                g.fillPolygon( xpoints, ypoints, 4 );               
                g.setColor( Color.black );
            } else {  // everything else
                // draw a blue filled circle
                g.setColor( Color.blue );
                g.drawOval( ix-3, iy-3, 6, 6 );
                g.setColor( Color.black );
            }
    }
    
    /**
     * Draws the legend for the plot groups represented on the plot
     *
     * @param g the graphics context
     * @param ix the x location (center) of the symbols in the legend
     * @param iy the y location for the top of the legend, roughly
     */   
    public static void drawLegend( Graphics g, int ix, int iy ) {
        if ( PlotCodes == null ) return;
        PlotGroupBoxes = new Vector();
        int k = 0;   // keeps track of legend items actually drawn; for vertical placement (09-Mar-05)
        for ( int j=0; j<PlotCodes.size(); j++ ) {
        	//System.out.println( PlotCodes.get(j) + ": " + ((Boolean)PlotGroupPresent.get(j)).booleanValue() ); 
        	if ( ((Boolean)PlotGroupPresent.get(j)).booleanValue() ) {      		
	            PlotGroupBoxes.add( new Rectangle( ix, iy+k*20-5, 10, 10 ) );
	            g.drawRect(ix,iy+k*20-5,10,10);
	            if ( ((Boolean)PlotGroupOn.get(j)).booleanValue() ) {
	                g.drawLine(ix,iy+k*20-5,ix+10,iy+k*20+5);
	                g.drawLine(ix,iy+k*20+5,ix+10,iy+k*20-5);
	            }
	            String grp = (String) PlotCodes.get(j);
	            drawSymbol( g, ix+20, iy+k*20, grp );
	            if ( grp.equals("F") ) {
	            	g.drawString( "Forams", ix+30, iy+k*20+5 );
	            } else if ( grp.equals("D") ) {
	            	g.drawString( "Diatoms", ix+30, iy+k*20+5 );
	            } else if ( grp.equals("R") ) {
	            	g.drawString( "Rads", ix+30, iy+k*20+5 );
	            } else if ( grp.equals("N") ) {
	            	g.drawString( "Nannos", ix+30, iy+k*20+5 );
	            } else {
	            	g.drawString( grp, ix+30, iy+k*20+5 );
	            }
	            k++;
        	}
        }
        
    }
}
