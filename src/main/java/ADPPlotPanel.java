/*
 * ADPPlotPanel.java
 *
 * Created on November 25, 2003, 3:09 PM
 * Last modified by gcb on 6/17/04 12:01 PM
 * 07-Apr-04:  Adding javadoc comments
 * 17-June-04: Adding toggling of plot groups . . .
 * 18-June-04: Changing plot & axis titles to variables for editing and adding label-plotting
 * 22-Dec-04:  Adding calls to ADPApp.updateLOCTable after changes to LOC
 * 29-Dec-04:  Fixing zoom to handle clicking in one spot, I hope . . .
 * 05-Jan-05:  Fixed bug where last LOC point in region would jump up when moved (DepthBound1
 *             mistakenly set to MaxAge rather than MaxDepth)
 * 05-Jan-05:  Working on responding to double-clicks . . .
 */

/**
 * The ADP plot panel
 *
 * @version 1.10 05-Jan-05
 * @author  gcb
 */

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.text.*;
import java.awt.event.*;

public class ADPPlotPanel extends JPanel implements MouseListener, MouseMotionListener {
    
    /** Determines response to mouse events */
    private int ActionMode;
    /** Minimum value (mbsf) on depth axis */
    public double MinDepth;
    /** Maximum value (mbsf) on depth axis */
    public double MaxDepth;
    /** Major (labeled) tick interval on depth axis */
    public double DepthIncMajor;
    /** Minor (unlabeled) tick interval on depth axis */
    public double DepthIncMinor;
    /** Minimum value (Ma) on age axis */
    public double MinAge;
    /** Maximum value (Ma) on age axis */
    public double MaxAge;
    /** Major (labeled) tick interval on age axis */
    public double AgeIncMajor;
    /** Minor (unlabeled) tick interval on age axis */
    public double AgeIncMinor;
    /** Width of entire plot area (including margins) in pixels */
    private int pxWidth;
    /** Height of entire plot area (including margins) in pixels */
    private int pxHeight;
    /** x margin width in pixels */
    private int xMargin = 100;
    /** y margin height in pixels */
    private int yMargin = 70;
    /** Location of right side of plot in pixels */
    private int xRight;
    /** Location of bottom of plot in pixels */
    private int yBot;
    /** Age range (Ma) represented by x axis */
    private double AgeWidth;
    /** Depth range (m) represented by y axis */
    private double DepthHeight;
    /** Scale along x (age) axis in pixels/Ma */
    private double xScale;
    /** Scale along y (depth) axis in pixels/m */
    private double yScale;
    /** A decimal format specification */
    private DecimalFormat df2;   // added 1/15/04 1:20 PM
    /** Index of Selected LOC point, if moving one */
    private int iSelLOCPoint;    // added 01/16/04
    /** Lower age bound for LOC point move */
    private double AgeBound0;
    /** Upper age bound for LOC point move */
    private double AgeBound1;
    /** Upper (numerically lower) depth bound for LOC point move */
    private double DepthBound0;
    /** Lower (numerically higher) depth bound for LOC point move */
    private double DepthBound1;
    /** x coordinate of anchoring corner of selection box */
    private int xSel0;           // added 2/13/04 9:26 PM
    /** y coordinate of anchoring corner of selection box */
    private int ySel0;
    /** x coordinate of dragged corner of selection box */
    private int xSel1;
    /** y coordinate of dragged corner of selection box */
    private int ySel1;
    /** Plot title */
    public String PlotTitle;
    /** X (age) axis title */
    public String XAxisTitle;
    /** Y (depth) axis title */
    public String YAxisTitle;
    /** Whether event labels are displayed */
    public boolean ShowLabels;    
    
    /** Creates a new instance of ADPPlotPanel */
    public ADPPlotPanel() {
        setBackground( Color.white );
        ActionMode = 0;  // zero for moving age model control points
        MinDepth = 0.0;
        MaxDepth = 1000.0;
        DepthIncMajor = 200.0;
        DepthIncMinor = 40.0;
        MinAge = 0.0;
        MaxAge = 100.0;
        AgeIncMajor = 20;
        AgeIncMinor = 4;
        addMouseListener( this );
        addMouseMotionListener( this );
        df2 = new DecimalFormat( "0.00" );
        iSelLOCPoint = -1;    // no selected LOCPoint initially . . .
        xSel0 = -1;           // no selection box initially (gcb, 2/13/04 9:31 PM)
        ySel0 = -1;
        xSel1 = -1;
        ySel1 = -1;
        PlotTitle = "Age-Depth Plot for Hole [null]";
        XAxisTitle = "Age (Ma)";
        YAxisTitle = "Depth (mbsf)";
        ShowLabels = false;
    }
    
    public void paint( Graphics g ) {
        super.paint(g);
        pxWidth = this.getWidth();
        pxHeight   = this.getHeight();
        AgeWidth = MaxAge - MinAge;
        DepthHeight = MaxDepth - MinDepth;
        xScale = (pxWidth-2.0*xMargin)/AgeWidth;
        yScale = (pxHeight-2.0*yMargin)/DepthHeight;
        xRight = xMargin + (int)(AgeWidth*xScale);   // added 1/15/04 1:17 PM
        yBot = yMargin + (int)(DepthHeight*yScale);  // ditto
        g.setClip( xMargin, yMargin, (int)(AgeWidth*xScale), (int)(DepthHeight*yScale) );
        g.setColor( Color.black );
        // if set to add points to LOC, draw rectangles bounding legal insertion ares
        if ( ActionMode==0 && iSelLOCPoint >= 0 ) {   // moving a LOC point
            g.setColor( Color.lightGray );            // draw rectangle showing legal move region
            int px0 = xMargin+(int)((AgeBound0-MinAge)*xScale);
            int py0 = yMargin+(int)((DepthBound0-MinDepth)*yScale);
            int px1 = xMargin+(int)((AgeBound1-MinAge)*xScale);
            int py1 = yMargin+(int)((DepthBound1-MinDepth)*yScale);
            int[] xpoints = { px0, px1, px1, px0 };
            int[] ypoints = { py0, py0, py1, py1 };
            g.drawPolygon( xpoints, ypoints, 4 );
            g.setColor( Color.black );
        }
        if ( ActionMode==1 && ADPApp.LOCPoints != null ) {
            g.setColor( Color.lightGray );
            Enumeration eloc = ADPApp.LOCPoints.elements();
            int px0 = xMargin;
            int py0 = yMargin;
            while ( eloc.hasMoreElements() ) {
                ADPLOCPoint adp = (ADPLOCPoint) eloc.nextElement();
                int px1 = xMargin+(int)((adp.Age-MinAge)*xScale);
                int py1 = yMargin+(int)((adp.Depth-MinDepth)*yScale);
                int[] xpoints = { px0, px1, px1, px0 };
                int[] ypoints = { py0, py0, py1, py1 };
                g.drawPolygon( xpoints, ypoints, 4 );
                px0 = px1;
                py0 = py1;
            }
            int px1 = xRight;
            int py1 = yBot;
            int[] xpoints = { px0, px1, px1, px0 };
            int[] ypoints = { py0, py0, py1, py1 };
            g.drawPolygon( xpoints, ypoints, 4 );
            g.setColor( Color.black );
        }

        if ( ADPApp.StratEvents != null ) {
            Enumeration se = ADPApp.StratEvents.elements();
            while ( se.hasMoreElements() ) {
                ADPStratEvent evt = (ADPStratEvent) se.nextElement();
                evt.draw(g,xMargin,yMargin,MinAge,MinDepth,xScale,yScale,ShowLabels);
            }
        }
        if ( ADPApp.LOCPoints != null ) {
            Color darkerGreen = Color.green.darker().darker();
            g.setColor( darkerGreen  );
            Enumeration eloc = ADPApp.LOCPoints.elements();
            ADPLOCPoint adp = (ADPLOCPoint) eloc.nextElement();
            int px0 = xMargin+(int)((adp.Age-MinAge)*xScale);
            int py0 = yMargin+(int)((adp.Depth-MinDepth)*yScale);
            while ( eloc.hasMoreElements() ) {
                adp = (ADPLOCPoint) eloc.nextElement();
                int px1 = xMargin+(int)((adp.Age-MinAge)*xScale);
                int py1 = yMargin+(int)((adp.Depth-MinDepth)*yScale);
                g.drawLine( px0, py0, px1, py1 );
                px0 = px1;
                py0 = py1;
            }
            eloc = ADPApp.LOCPoints.elements();   // start over again for control poinnts, which need to be on top of everything
            while ( eloc.hasMoreElements() ) {
                adp = (ADPLOCPoint) eloc.nextElement();
                int px = xMargin+(int)((adp.Age-MinAge)*xScale);
                int py = yMargin+(int)((adp.Depth-MinDepth)*yScale);
                g.setColor( Color.white );
                g.fillRect( px-3, py-3, 6, 6 );
                g.setColor( darkerGreen );
                g.drawRect( px-3, py-3, 6, 6 );
            }
        }
        g.setClip( null );
        g.setColor( Color.black );
        g.setFont( new Font("Serif", Font.BOLD, 12 ) );
        DecimalFormat df1 = new DecimalFormat("0.0");
        g.drawRect( xMargin, yMargin, (int) (xScale*AgeWidth), (int) (yScale*DepthHeight) );
        if ( DepthIncMinor > 0.0 ) {
           double dmin = DepthIncMinor*Math.ceil(MinDepth/DepthIncMinor);
           for ( double depth=dmin; depth<(MaxDepth+.1*DepthIncMinor); depth+=DepthIncMinor ) {
                g.drawLine( xMargin, yMargin+(int)((depth-MinDepth)*yScale), xMargin-7, yMargin+(int)((depth-MinDepth)*yScale) );
            }
        }
        if ( DepthIncMajor > 0.0 ) {
            double dmin = DepthIncMajor*Math.ceil(MinDepth/DepthIncMajor);
            for ( double depth=dmin; depth<(MaxDepth+.1*DepthIncMajor); depth+=DepthIncMajor ) {
                g.drawLine( xMargin, yMargin+(int)((depth-MinDepth)*yScale), xMargin-10, yMargin+(int)((depth-MinDepth)*yScale) );
                drawJustifiedString( g, df1.format(depth), xMargin-13, yMargin+(int)((depth-MinDepth)*yScale), 1.0, 0.3 );
            }
        }
        if ( AgeIncMinor > 0.0 ) {
            double amin = AgeIncMinor*Math.ceil(MinAge/AgeIncMinor);
            for ( double age=amin; age<(MaxAge+.1*AgeIncMinor); age+=AgeIncMinor ) {
                g.drawLine( xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+7 );
            }
        }
        if ( AgeIncMajor > 0.0 ) {
            double amin = AgeIncMajor*Math.ceil(MinAge/AgeIncMajor);
            for ( double age=amin; age<(MaxAge+.1*AgeIncMajor); age+=AgeIncMajor ) {
                g.drawLine( xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+10 );
                drawJustifiedString( g, df1.format(age), xMargin+(int)((age-MinAge)*xScale), yMargin+(int)(DepthHeight*yScale)+13, 0.5, 1.0 );
            }
        }
        g.setFont( new Font("Serif", Font.BOLD, 14) );
        drawJustifiedString( g, XAxisTitle, xMargin+(int)(0.5*AgeWidth*xScale), yMargin+(int)(DepthHeight*yScale)+30, 0.5, 1.0 );
        
        int ix = xMargin - 50;
        int iy = yMargin + (int) ((DepthHeight/2.0)*yScale);
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(ix,iy);
        g2.rotate(-Math.PI/2.0);
        drawJustifiedString(g2,YAxisTitle,0,0,0.5,0.0);
        g2.rotate(Math.PI/2.0);
        g2.translate(-ix,-iy);
        ADPSymbolPalette.drawLegend( g, xMargin+(int)(AgeWidth*xScale)+15, yMargin+10 );
        g.setFont( new Font("Serif", Font.BOLD, 18) );
        drawJustifiedString( g, PlotTitle, xMargin+(int)(0.5*AgeWidth*xScale), yMargin-50, 0.5, 1.0 );
 
        // draw selection box on  top if there
        if ( ActionMode==3 && (xSel0+ySel0+xSel1+ySel1) > 3 ) {
            g.setColor( Color.gray );
            int[] xpoints = { xSel0, xSel1, xSel1, xSel0 };
            int[] ypoints = { ySel0, ySel0, ySel1, ySel1 };
            g.drawPolygon( xpoints, ypoints, 4 );
            g.setColor( Color.black );
        }
    }
    
    public void setActionMode( int i ) {
        ActionMode = i;
    }
    
    
    public ADPAxisParameters getAxisParameters() {
        return new ADPAxisParameters( MinDepth, MaxDepth, DepthIncMajor, DepthIncMinor,
                                      MinAge, MaxAge, AgeIncMajor, AgeIncMinor );
    }
    
    public void setAxisParameters(ADPAxisParameters adpAP) {
        MinDepth = adpAP.MinDepth;
        MaxDepth = adpAP.MaxDepth;
        DepthIncMajor = adpAP.DepthIncMajor;
        DepthIncMinor = adpAP.DepthIncMinor;
        MinAge = adpAP.MinAge;
        MaxAge = adpAP.MaxAge;
        AgeIncMajor = adpAP.AgeIncMajor;
        AgeIncMinor = adpAP.AgeIncMinor;
    }

    public void setAxisParametersOrigin(ADPAxisParameters adpAP) {
        MinDepth = 0;
        MaxDepth = adpAP.MaxDepth;
        DepthIncMajor = adpAP.DepthIncMajor;
        DepthIncMinor = adpAP.DepthIncMinor;
        MinAge = 0;
        MaxAge = adpAP.MaxAge;
        AgeIncMajor = adpAP.AgeIncMajor;
        AgeIncMinor = adpAP.AgeIncMinor;
    }
    
    private void drawJustifiedString( Graphics g, String str, int x, int y, double xjust, double yjust ) {
        // xjust: 0.0 for left-justified, 0.5 for centered, 1.0 for right-justified
        // yjust: 0.0 for bottom-aligned, 0.5 for centered, 1.0 for top-aligned
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D r2d2 = fm.getStringBounds(str,g);
        int xoff = (int)(xjust*r2d2.getWidth());
        int yoff = (int)(yjust*r2d2.getHeight());
        g.drawString(str,x-xoff,y+yoff);
    }
    
    public void mouseEntered(MouseEvent e) {
    }
    
    public void mouseExited(MouseEvent e) {
    }
    
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        /*
        if ( e.getButton() == MouseEvent.BUTTON1 ) {
        	     if ( e.isControlDown() ) {
        			JOptionPane.showMessageDialog(null,"Ctrl-click at: (" + x + "," + y + ")" );
        	     } else if ( e.isShiftDown() ) {
        	     	JOptionPane.showMessageDialog(null,"Shift-click at: (" + x + "," + y + ")" );
        	     } else if ( e.isAltDown() ) {
        			JOptionPane.showMessageDialog(null,"Alt-click at: (" + x + "," + y + ")" );
        	     } else if ( e.isMetaDown() ) {
        			JOptionPane.showMessageDialog(null,"Meta-click at: (" + x + "," + y + ")" ); 	     	
        	     } else {
        	     	JOptionPane.showMessageDialog(null,"Button 1 clicked at: (" + x + "," + y + ")" );
        	     }
        } else if ( e.getButton() == MouseEvent.BUTTON2 ) {
    			JOptionPane.showMessageDialog(null,"Button 2 clicked at: (" + x + "," + y + ")" );
        } else {
    			JOptionPane.showMessageDialog(null,"Something else happened at: (" + x + "," + y + ")" );
        }
        */
        /* Determine whether a group checkbox in the legend has been clicked . . . 17-June-04 */
        if ( ADPSymbolPalette.PlotGroupBoxes != null ) {
            for ( int i=0; i<ADPSymbolPalette.PlotGroupBoxes.size(); i++ ) {
                Rectangle gbox = (Rectangle) ADPSymbolPalette.PlotGroupBoxes.get(i);
                if ( gbox.contains(x,y) ) {
                	   //System.out.println("Clicked box " + i);
                    boolean GroupOn = ( (Boolean) ADPSymbolPalette.PlotGroupOn.get(i) ).booleanValue();
                    ADPSymbolPalette.PlotGroupOn.set(i,new Boolean(!GroupOn));
                    this.repaint();
                    return;
                }
                
            }
        }
        if ( x < xMargin || x > xRight || y < yMargin || y > yBot ) return;  // outside plot area
        double age = MinAge + (x-xMargin)/xScale;
        double depth = MinDepth + (y-yMargin)/yScale;
        if ( ActionMode == 1 ) {         // adding an age model control point, if in legal region
            if ( ADPApp.LOCPoints == null ) return;   // no LOC to work with
            double age0 = MinAge;
            double depth0 = MinDepth;
            double age1;
            double depth1;
            int npoints = ADPApp.LOCPoints.size();
            int i;
            for (i=0; i<=npoints; i++) {
                if ( i<npoints ) {
                    ADPLOCPoint adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i);
                    age1 = adp.Age;
                    depth1 = adp.Depth;
                } else {
                    age1 = MaxAge;
                    depth1 = MaxDepth;
                }
                if ( age0 < age && age < age1 && depth0 < depth && depth < depth1 ) break;
                age0 = age1;
                depth0 = depth1;
            }
            if ( i <= npoints ) {     // clicked in an allowed region . . .
                ADPApp.LOCPoints.insertElementAt( new ADPLOCPoint(age,depth), i );
                this.repaint();
                ADPApp.updateLOCTable();  // added 12/22/04 2:48 PM
            }
        } else if ( ActionMode == 2 ) {  // deleting an age model control point
            if ( ADPApp.LOCPoints == null ) return;   // no LOC to work with
            if ( ADPApp.LOCPoints.size()<3 ) return;  // have to keep at least two points . . .
            for (int i=0; i<ADPApp.LOCPoints.size(); i++) {
                ADPLOCPoint adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i);
                int px = xMargin + (int) ((adp.Age-MinAge)*xScale);
                int py = yMargin + (int) ((adp.Depth-MinDepth)*yScale);
                if ( Math.abs(px-x)<3 && Math.abs(py-y)<3 ) {  // have to click within three pixels of point . . .
                    //System.out.println("Should be removing LOCPoint " + i);
                    ADPApp.LOCPoints.remove(i);                // to delete it
                    this.repaint();
                    ADPApp.updateLOCTable();  // added 12/22/04 2:54 PM
                    break;
                }
            }
        }
    }
    
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if ( x < xMargin || x > xRight || y < yMargin || y > yBot ) return;  // outside plot area
        double age = MinAge + (x-xMargin)/xScale;
        double depth = MinDepth + (y-yMargin)/yScale;
        if ( ActionMode==0 ) {    // presumably selecting a LOCPoint to move
            if ( ADPApp.LOCPoints == null ) return;   // no LOC to work with
            for (int i=0; i<ADPApp.LOCPoints.size(); i++) {
                ADPLOCPoint adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i);
                int px = xMargin + (int) ((adp.Age-MinAge)*xScale);
                int py = yMargin + (int) ((adp.Depth-MinDepth)*yScale);
                if ( Math.abs(px-x)<3 && Math.abs(py-y)<3 ) {  // have to click within three pixels of point . . .
                    iSelLOCPoint = i;                          // to select it . . .
                    if ( iSelLOCPoint==0 ) {  // first one
                        AgeBound0 = MinAge;
                        DepthBound0 = MinDepth;
                    } else {
                        adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i-1);
                        AgeBound0 = adp.Age;
                        DepthBound0 = adp.Depth;
                    }
                    if ( iSelLOCPoint==ADPApp.LOCPoints.size()-1 ) {  // last one
                        AgeBound1 = MaxAge;
                        DepthBound1 = MaxDepth;
                    } else {
                        adp = (ADPLOCPoint) ADPApp.LOCPoints.get(i+1);
                        AgeBound1 = adp.Age;
                        DepthBound1 = adp.Depth;
                    }
                    // now limit to margins . . .
                    if ( AgeBound0 < MinAge ) AgeBound0 = MinAge;
                    if ( DepthBound0 < MinDepth ) DepthBound0 = MinDepth;
                    if ( AgeBound1 > MaxAge ) AgeBound1 = MaxAge;
                    if ( DepthBound1 > MaxDepth ) DepthBound1 = MaxDepth;  // set equal to MaxAge before (05-Jan-05)
                    this.repaint();
                    break;
                }
            }
        } else if ( ActionMode==3 ) {  // gcb, 2/13/04 9:43 PM
            //System.out.println( "Pressed at (" + x + ", " + y + ")" );
            xSel0 = x;
            ySel0 = y;
        }
    }
    
    public void mouseReleased(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if ( ActionMode==0 ) {     // have just released a LOC point being moved . . .
            iSelLOCPoint = -1;     // Just let it go . . .
            this.repaint();
            ADPApp.updateLOCTable();   // added 12/22/04 2:58 PM
        } else if ( ActionMode==3 && xSel0+ySel0+xSel1+ySel1 > 3 ) {   // gcb, 2/13/04 9:43 PM
            if ( x > xSel0 ) {  // make it so xSel1 > xSel0 regardless of drag direction
                xSel1 = x;
            } else {
                xSel1 = xSel0;
                xSel0 = x;
            }
            if ( y > ySel0 ) {  // same with y's
                ySel1 = y;
            } else {
                ySel1 = ySel0;
                ySel0 = y;
            }
            //Zoom age axis by a factor of 2 around point if box is too small
            //in x direction (29-Dec-04)
            if ( (xSel1 - xSel0)<10 ) {
                double xMid = 0.5*(xSel0+xSel1);
                double aMid = MinAge + (xMid-xMargin)/xScale;  // middle age of box
                double AgeWidth = 0.5*(MaxAge - MinAge);  // halve age ramge
                MinAge = aMid - 0.5*AgeWidth;  // center on aMid
                MaxAge = aMid + 0.5*AgeWidth;
            } else {  // box is big enough; do what we had before
                MaxAge = MinAge + (xSel1-xMargin)/xScale;  // new MaxAge
                MinAge = MinAge + (xSel0-xMargin)/xScale;  // new MinAge
            }
            AgeIncMajor = ADPApp.pretty( (MaxAge-MinAge)/5.1 );
            AgeIncMinor = AgeIncMajor/5.0;
            MinAge = AgeIncMinor * Math.floor( MinAge/AgeIncMinor );  // adjust out to even minor increments
            MaxAge = AgeIncMinor * Math.ceil( MaxAge/AgeIncMinor );
            //Zoom depth axis by factor of 2 around point if box too small
            //in y direction (29-Dec-04)
            if ( (ySel1-ySel0)<10 ) {
                double yMid = 0.5*(ySel0+ySel1);
                double dMid = MinDepth + (yMid-yMargin)/yScale;  // middle depth
                double DepthHeight = 0.5*(MaxDepth-MinDepth);  // halve depth range
                MinDepth = dMid - 0.5*DepthHeight;  // center on dMid
                MaxDepth = dMid + 0.5*DepthHeight;
            } else {
                MaxDepth = MinDepth + (ySel1-yMargin)/yScale;  // new MaxDepth
                MinDepth = MinDepth + (ySel0-yMargin)/yScale;  // new MinDepth
            }
            DepthIncMajor = ADPApp.pretty( (MaxDepth-MinDepth)/5.1 );
            DepthIncMinor = DepthIncMajor/5.0;
            MinDepth = DepthIncMinor * Math.floor( MinDepth/DepthIncMinor );
            MaxDepth = DepthIncMinor * Math.ceil( MaxDepth/DepthIncMinor );
            xSel0 = -1;
            ySel0 = -1;
            xSel1 = -1;
            ySel1 = -1;
            this.repaint();
        }
    }
    
    public void mouseDragged( MouseEvent e ) {
        mouseMoved( e );   // still want to update labels at bottom (1/16/04 8:13 PM)
        int x = e.getX();
        int y = e.getY();
        if ( x < xMargin-30 || x > xRight+30 || y < yMargin-30 || y > yBot+30 ) {  // dragged too far off plot . . .
            iSelLOCPoint = -1;   // let it go . . .
            xSel0 = -1;          // or let selection rectangle go . . .
            ySel0 = -1;
            xSel1 = -1;
            ySel1 = -1;
            this.repaint();
        }
        if ( ActionMode==0 && iSelLOCPoint >= 0 ) {  // moving a LOC point
            double age = MinAge + (x-xMargin)/xScale;
            double depth = MinDepth + (y-yMargin)/yScale;
            ADPLOCPoint adp = (ADPLOCPoint) ADPApp.LOCPoints.get(iSelLOCPoint);
            if ( age < AgeBound0 ) {
                age = AgeBound0;
            } else if ( age > AgeBound1 ) {
                age = AgeBound1;
            }
            if ( depth < DepthBound0 ) {
                depth = DepthBound0;
            } else if ( depth > DepthBound1 ) {
                depth = DepthBound1;
            }
            ADPApp.LOCPoints.set( iSelLOCPoint, new ADPLOCPoint(age,depth) );
            this.repaint();
        } else if ( ActionMode==3 && (xSel0+ySel0) > 1 ) {
            //System.out.println( "Dragged to ( " + x + ", " + y + ")" );
            xSel1 = x;
            ySel1 = y;
            this.repaint();
        }
            
    }
    
    public void mouseMoved( MouseEvent e ) {
        int x = e.getX();
        int y = e.getY();
        //System.out.println( "x, y: " + x + ", " + y );
        if ( xMargin < x && x < xRight && yMargin < y && y < yBot ) {
            double age = MinAge + (x-xMargin)/xScale;
            double depth = MinDepth + (y-yMargin)/yScale;
            ADPApp.lblMouseLoc.setText( df2.format(age) + " Ma,  " + df2.format(depth) + " m" );
            //System.out.println( "Age,  Depth:  " + df2.format(age) + " Ma,  " + df2.format(depth) + " m" );
            ADPStratEvent selse = null;
            if ( ADPApp.StratEvents != null ) {
                Enumeration ese = ADPApp.StratEvents.elements();
                while ( ese.hasMoreElements() ) {
                    ADPStratEvent se = (ADPStratEvent) ese.nextElement();
                    /* make sure event is being displayed before updating event label (17-June-04) */
                    boolean GroupOn = false;
                    try {
                        int igrp = ADPSymbolPalette.PlotCodes.indexOf(se.EventGroup);
                        GroupOn = ( (Boolean)ADPSymbolPalette.PlotGroupOn.get(igrp) ).booleanValue();
                    } catch (Exception ex) {
                        GroupOn = false;
                    }
                    if ( GroupOn && !Double.isNaN(se.MidAge) && !Double.isNaN(se.MidDepth) ) {
                        int px = xMargin + (int) ((se.MidAge-MinAge)*xScale);
                        int py = yMargin + (int) ((se.MidDepth-MinDepth)*yScale);
                        if ( Math.abs(x-px)<5 && Math.abs(y-py)<5 ) {
                            selse = se;
                            break;
                        }
                    }
                }
            }
            if ( selse==null ) {
                ADPApp.lblStratEvent.setText( "          " );
            } else {
                ADPApp.lblStratEvent.setText( selse.EventName );
            }
        } else {
            ADPApp.lblMouseLoc.setText( "          " );
            ADPApp.lblStratEvent.setText( "          " );
        }
    }
    
    
    
    
}
