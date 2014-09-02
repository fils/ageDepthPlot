/*
 * NeptuneHoleFinder.java
 *
 * Created on January 7, 2004, 3:08 PM (as ADPHoleFinder)
 * Copied from ADPHoleFinder to CDACHoleFinder on 2/4/04 1:04 PM
 * 
 * Modified 17-Mar-04:  Replacing pre-built ESRI toolbars with JToolBar with
 * only the desired buttons (zoom full, zoom in, zoom out, pan, identify)
 *
 * 24-Mar-04:  Working in the evening on window setup & pick listener . . .
 * 31-Mar-04:  Working on stuff some more . . .
 * 05-Apr-04:  And on and on . . .
 * 12-Apr-04:  One last attempt to make selection work, now with my own JTable
 * 14-Apr-04:  Renamed NeptuneHoleFinder; working on rendering of layers;
 *                  adding return of selected HoleID
 * 18-Apr-04:  Added sample count to tblNeptuneHoles, after reducing actual input
 *                  table to those 174 holes with >5 samples.  Sigh.
 * 02-July-04:  Trying to get layers from over the network . . .
 * 02-July-04:   . . . but it's just too stupid:  needs com/esri/axl/UnMarshallingException.class,
 *              but there is no such thing.
 * 16-July-04:  Working on data connections . . .
 * 19-July-04:  And on and on . . .
 * 28-July-04:  Switching to roll-your-own GIS using Jeremy's map query . . .
 * 29-July-04:  etc.
 * 23-Aug-04:  Starting on adding select/zoom buttons
 * 01-Sept-04:  Adding code to deal with resizing and zooming . . .
 * 03-Sept-04:  continuing on that . . .
 * 06-Oct-04:   Trying to clean up details of map handling
 * 17-Oct-04:   Getting rid of JOptionPane in paint function.  Shoulda known better.
 */

/**
 * Dialog box for selecting holes from the Neptune database.
 *
 * @author  gcb
 * @version 1.00 17-Oct-04
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;
import java.util.*;


public class NeptuneHoleFinder extends JDialog implements ActionListener {
    private JSplitPane jsp;     // trying JSplitPanes again, 01 Sept 04
    private MapPanel mp;
    private Vector Holes;
    private double[] llBox = { -180.0, 180.0, -90.0, 90.0 };     // longitude, latitude limits of world map
    private int[] pxDim = { 720, 360 };     // intial pixel dimensions of world map
    private Vector ColumnNames;
    private Vector RowData;
    private JTable tblNeptuneHoles;
    private JButton btnOpen;
    private JButton btnCancel;
    private NeptuneHoleInfo SelHole;
    private Vector SelHoles;
    private int paintno;
    private int ActionMode;     // selecting holes (0) or zooming (1)
    private JButton btnSelect;  // selection button
    private JButton btnZoomIn;  // zoom-in button
    private JButton btnZoomOut; // zoom-to-whole-world button
    private JLabel lblLatLong;  // current long, lat (added 9/3/04 3:49 PM)
    private JLabel lblHoleID;   // added 10/5/04 3:15 PM
    private boolean NeedNewMap;  // added 9/1/04 1:23 PM; need to get new map?

    /** Creates a new instance of CDACHoleFinder */
    public NeptuneHoleFinder(JFrame owner, Vector holes, int hfWidth, int hfHeight ) {
        super(owner,"Neptune Hole Locator",true);  // true for modal dialog box
        
        setSize( hfWidth, hfHeight );
        
        paintno = 0;
        SelHoles = null;
        ActionMode = 0;    // default: select holes
        
        Container c = getContentPane();
        c.setBackground( Color.white );
        c.setLayout( new BorderLayout() );
        
        // copy holes into local Vector
        Holes = new Vector();
        Enumeration urg = holes.elements();
        while (urg.hasMoreElements()) {
            NeptuneHoleInfo hi = (NeptuneHoleInfo) urg.nextElement();
            Holes.add(hi);
        }
        
        //jsp = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        
        //WorldMapGrabber wmg = new WorldMapGrabber();
        //leMonde = wmg.getImage(llBox,pxDim);
        /*
        if ( leMonde != null ) {
            //System.out.println("llBox: " + llBox[0] + ", " + llBox[1] + ", " + llBox[2] + ", " + llBox[3] );
            //System.out.println("Dim: " + leMonde.getWidth() + ", " + leMonde.getHeight() );
        }
         **/
        
        
        //JPanel NorthPanel = new JPanel();  // for holding map panel and select/zoom button panel
        //NorthPanel.setLayout( new BorderLayout() );
        
        Font bf = new Font( "Serif", Font.BOLD, 18 );
        
        java.net.URL giffy = null;  // 30-Aug-04: Trying to switch to images on buttons
        
        JPanel tp = new JPanel();  // tp, to hold bp on left, lp on right
        tp.setLayout( new BorderLayout() );
        
        JPanel bp = new JPanel();        // for select, zoom buttons
        //bp.setLayout( new GridLayout( 7, 1, 1, 5 ) );
        bp.setLayout( new FlowLayout(FlowLayout.LEFT) );
        giffy = getClass().getResource("btnSelect.gif");
        btnSelect = new JButton( new ImageIcon(giffy) );  // pointer image would be better
        //btnSelect.setFont( bf );
        //btnSelect.setForeground( Color.blue );
        btnSelect.setToolTipText( "Select holes" );
        btnSelect.setSize(20,20);
        btnSelect.setBackground( Color.gray );
        btnSelect.addActionListener(this);
        bp.add( btnSelect );
        giffy = getClass().getResource("btnZoomIn.gif");
        btnZoomIn = new JButton( new ImageIcon(giffy) );
        //btnZoomIn = new JButton( "Z+" );
        //btnZoomIn.setFont( bf );
        //btnZoomIn.setForeground( Color.blue );
        btnZoomIn.setToolTipText( "Zoom in (drag rectangle)" );
        btnZoomIn.setMaximumSize( new Dimension(24,24));
        btnZoomIn.setBackground( Color.lightGray );
        btnZoomIn.addActionListener( this );
        bp.add( btnZoomIn );
        giffy = getClass().getResource("btnZoomOut.gif");
        btnZoomOut = new JButton( new ImageIcon(giffy) );
        //btnZoomOut = new JButton( "Z -" );
        //btnZoomOut.setFont( bf );
        //btnZoomOut.setForeground( Color.blue );
        btnZoomOut.setToolTipText( "Zoom out (to full extent)" );
        btnZoomOut.setMaximumSize(new Dimension(24,24));
        btnZoomOut.setBackground( Color.lightGray );
        btnZoomOut.addActionListener( this );
        bp.add( btnZoomOut );
        
        tp.add( bp, BorderLayout.WEST );
        
        JPanel lp = new JPanel();
        lp.setLayout( new FlowLayout(FlowLayout.RIGHT) );
        lp.add( new JLabel("Lat, Long: ") );
        lblLatLong = new JLabel("   ");
        lblLatLong.setForeground( Color.red );
        lp.add( lblLatLong );
        lp.add( new JLabel("   Hole ID: ") );
        lblHoleID = new JLabel("   ");
        lblHoleID.setForeground( Color.red );
        lp.add( lblHoleID );
        
        tp.add( lp, BorderLayout.EAST );
        
        
        c.add( tp, BorderLayout.NORTH );

        jsp = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        
        mp = new MapPanel();
        //mp.setSize( 800, 440 );
        
        jsp.setTopComponent( mp );
        
        //NorthPanel.add( mp, BorderLayout.CENTER );
        
        //jsp.setTopComponent( NorthPanel );
        //c.add( NorthPanel, BorderLayout.NORTH );
        
        //JPanel SouthPanel = new JPanel();  // for table of selected data and panel w/ OK, Cancel buttons
        //SouthPanel.setLayout( new BorderLayout() );
       
        ColumnNames = new Vector();       // column names for tblNeptuneHoles
        ColumnNames.add("HoleID");
        ColumnNames.add("Latitude");
        ColumnNames.add("Longitude");
        ColumnNames.add("SampleCount");
        ColumnNames.add("WaterDepth");
        ColumnNames.add("MetersPenetrated");
        ColumnNames.add("MetersRecovered");
        ColumnNames.add("Ocean");
        RowData = new Vector();
        for (int i=0; i<50; i++ ) {        // bunch of space-filled cells to begin with
            Vector row = new Vector();
            for (int j=1; j<ColumnNames.size(); j++ ) {  // skip #SHAPE# field
                row.add( new String(" ") );
            }
            RowData.add( row );
        }
        tblNeptuneHoles = new JTable( RowData, ColumnNames );
        JScrollPane moo = new JScrollPane( tblNeptuneHoles );
        //moo.setSize( 800, 100 );
        
        jsp.setBottomComponent( moo );
        //jsp.setSize( 800, 700 );
        jsp.setDividerLocation( (int)(0.70*hfHeight) );  // try to give map 70% of overall height
        c.add( jsp, BorderLayout.CENTER );
        
        //c.add( moo, BorderLayout.CENTER );
        //SouthPanel.add( moo, BorderLayout.CENTER );
          
        //jsp.setTopComponent( mp );
        //jsp.setDividerLocation( 0.8 );
        
        //getContentPane().add( jsp, BorderLayout.CENTER );
        
        // Panel for Retrieve and Cancel buttons . . .
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout( new FlowLayout() );
        btnOpen = new JButton( "Retrieve Selected Data" );
        btnOpen.addActionListener( this );
        buttonPanel.add( btnOpen );
        btnCancel = new JButton( "Cancel" );
        btnCancel.addActionListener( this );
        buttonPanel.add( btnCancel );
        c.add( buttonPanel, BorderLayout.SOUTH ); 
        //SouthPanel.add( buttonPanel, BorderLayout.SOUTH );
        
        //setSize(800,600);
    }
    
    public NeptuneHoleInfo showDialog() { 
        SelHole = null;
        show();
        return SelHole;
    }
    
    public void actionPerformed( ActionEvent e ) {
        if ( e.getSource() == btnSelect ) {
            ActionMode = 0;  // selecting holes
            btnSelect.setBackground( Color.gray );
            btnZoomIn.setBackground( Color.lightGray );
        } else if ( e.getSource() == btnZoomIn ) {
            ActionMode = 1;  // zooming in
            btnSelect.setBackground( Color.lightGray );
            btnZoomIn.setBackground( Color.gray );
        } else if ( e.getSource() == btnZoomOut ) {
            // reset longitude, latitude limits to whole world
            llBox[0] = -180.0;
            llBox[1] =  180.0;
            llBox[2] =  -90.0;
            llBox[3] =   90.0;
            NeedNewMap = true;
            mp.repaint();
            //ActionMode = 0;  // return to selection mode . . . or maybe not (22-Sept-04)
            //btnSelect.setBackground( Color.gray );
            //btnZoomIn.setBackground( Color.lightGray );           
        } else if ( e.getSource() == btnOpen ) {
            int selRow = tblNeptuneHoles.getSelectedRow();
            if ( selRow < 0 ) {
                SelHole = null;
            } else {
                String hid = tblNeptuneHoles.getValueAt( selRow, 0 ).toString();
                String lat = tblNeptuneHoles.getValueAt( selRow, 1 ).toString();
                String lon = tblNeptuneHoles.getValueAt( selRow, 2 ).toString();
                String nos = tblNeptuneHoles.getValueAt( selRow, 3 ).toString();
                String dep = tblNeptuneHoles.getValueAt( selRow, 4 ).toString();
                String pen = tblNeptuneHoles.getValueAt( selRow, 5 ).toString();
                String rec = tblNeptuneHoles.getValueAt( selRow, 6 ).toString();
                String sea = tblNeptuneHoles.getValueAt( selRow, 7 ).toString();
                if ( hid.equals(" ") ) {
                    SelHole = null;
                } else {
                    SelHole = new NeptuneHoleInfo( hid, lat, lon, nos, dep, pen, rec, sea );
                }
            }
            this.hide();
        } else if ( e.getSource() == btnCancel ) {
            SelHole = null;
            this.hide();
        }
            
    }
    
    private class MapPanel extends JPanel implements MouseListener, MouseMotionListener, ComponentListener {
        private int minMargin = 40;  // will stay fixed
        private int xMargin = 40;    // will probably change
        private int yMargin = 40;    // ditto
        private int pxWidth;    // pixel width of map
        private int pxHeight;   // pixel height of map
        private double xScale;
        private double yScale;
        private int xSel0;      // x coord (pixels), first corner of selection box
        private int ySel0;      // y coord (pixels), first corner of selection box
        private int xSel1;      // x coord (pixels), second corner of selection box
        private int ySel1;      // y coord (pixels), second corner of selection box
        private WorldMapGrabber wmg;
        private BufferedImage leMonde;  // world image
        private java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");  // added 02-Sep-04 for long, lat label
        
        public MapPanel() {
            setBackground( Color.white );
            addMouseListener(this);
            addMouseMotionListener(this);
            addComponentListener(this);
            xSel0 = -1;  // no selection box, initially
            ySel0 = -1;
            xSel1 = -1;
            ySel1 = -1;  
            //setSize( 800, 440 );
            wmg = new WorldMapGrabber();
            NeedNewMap = true;
        }
        
        //public Dimension getMinimumSize() {
        //    return new Dimension( 800, 440 );
        //}
        
        //public Dimension getPreferredSize() {
        //    return new Dimension( 800, 440 );
        //}
        
        public void paint( Graphics g ) {
            super.paint(g);
            pxWidth = this.getWidth() - 2*minMargin;   // widest possible pixel width
            pxHeight = this.getHeight() - 2*minMargin; // tallest possible pixel height
            if ( llBox[0] < -180.0 ) llBox[0] = -180.0;
            if ( llBox[1] >  180.0 ) llBox[1] =  180.0;
            if ( llBox[2] <  -90.0 ) llBox[2] =  -90.0;
            if ( llBox[3] >   90.0 ) llBox[3] =   90.0;
            xScale = ((double)pxWidth)/(llBox[1]-llBox[0]);  // resulting x scale
            yScale = ((double)pxHeight)/(llBox[3]-llBox[2]); // resulting y scale
            if ( xScale < yScale ) {   // x is limiting dimension
                pxHeight = (int) ( xScale*(llBox[3]-llBox[2]) );  // shrink height to match
                xMargin = minMargin;
                yMargin = ( this.getHeight() - pxHeight ) / 2;    // ( panel height - map height ) / 2
            } else {                   // y is limiting dimension
                pxWidth = (int) ( yScale*(llBox[1]-llBox[0]) );   // shrink width to match
                yMargin = minMargin;
                xMargin = ( this.getWidth() - pxWidth ) / 2;     // ( panel width - map width ) / 2
            }
            if (NeedNewMap) {   // should be true if we need to query for new map (01 Sept. 04)
                //System.out.println("trying to get new map...");
                Cursor oldCursor = this.getParent().getCursor();
                this.getParent().setCursor( new Cursor(Cursor.WAIT_CURSOR) );

                leMonde = wmg.getImage(llBox,pxWidth,pxHeight);
                //if ( leMonde == null ) {
                //    JOptionPane.showMessageDialog(null,"Could not obtain world relief image.");
                //}
                NeedNewMap = false;
                this.getParent().setCursor( oldCursor );
            }
            //System.out.println("painting..."+(paintno++));
            if (leMonde != null) {
                g.drawImage((Image)leMonde,xMargin,yMargin,this);
                pxWidth = leMonde.getWidth();
                pxHeight = leMonde.getHeight();
            } else {
                // just pxWidth, pxHeight at whatever they were
                g.setColor(Color.red);
                g.drawString("Unable to obtain world relief image",10,this.getHeight()-10);
                g.setColor(Color.black);
            }
            xScale = ((double)pxWidth)/(llBox[1]-llBox[0]);
            yScale = ((double)pxHeight)/(llBox[3]-llBox[2]);
            g.drawRect( xMargin-1, yMargin-1, pxWidth+2, pxHeight+2 );
            g.setColor( Color.lightGray );
            for (double xlong=(-180.0); xlong<181.0; xlong+=30.0) {
                if ( llBox[0] < xlong  && xlong < llBox[1] ) {  // only draw grid lines in range...
                    int px = xMargin + (int)(xScale*(xlong-llBox[0]));
                    g.drawLine( px, yMargin, px, yMargin+pxHeight );
                }
            }
            for (double ylat=(-90.0); ylat<91.0; ylat+=30.0) {
                if ( llBox[2] < ylat && ylat < llBox[3] ) {     // ditto . . .
                    int py = yMargin + (int)(yScale*(llBox[3]-ylat));
                    g.drawLine( xMargin, py, xMargin+pxWidth, py );
                }
            }
            g.setColor(Color.black);
            Enumeration eh = Holes.elements();
            while (eh.hasMoreElements()) {
                NeptuneHoleInfo hi = (NeptuneHoleInfo) eh.nextElement();
                try {
                    double xlong = Double.parseDouble(hi.longitude);
                    double ylat = Double.parseDouble(hi.latitude);
                    // draw circle at hole location if in map limits . . .
                    if ( llBox[0] < xlong  && xlong < llBox[1] && llBox[2] < ylat && ylat < llBox[3] ) {
                        int px = xMargin + (int)(xScale*(xlong-llBox[0]));
                        int py = yMargin + (int)(yScale*(llBox[3]-ylat));
                        g.fillOval(px,py,5,5);
                    }
                } catch (Exception ex) {
                    // just don't draw anything
                }
            }
            if ( SelHoles != null ) {
                g.setColor( Color.orange );
                Enumeration seh = SelHoles.elements();
                while (seh.hasMoreElements()) {
                    NeptuneHoleInfo shi = (NeptuneHoleInfo) seh.nextElement();
                    try {
                        double xlong = Double.parseDouble(shi.longitude);
                        double ylat = Double.parseDouble(shi.latitude);
                        int px = xMargin + (int)(xScale*(xlong-llBox[0]));
                        int py = yMargin + (int)(yScale*(llBox[3]-ylat));
                        g.fillOval(px,py,5,5);
                    } catch (Exception ex) {
                    // just don't draw anything
                    }
                }
                g.setColor( Color.black );
            }
            if ( ActionMode==1 && (xSel0+ySel0+xSel1+ySel1) > 3 ) {
                g.setColor( Color.orange );
                int[] xpoints = { xSel0, xSel1, xSel1, xSel0 };
                int[] ypoints = { ySel0, ySel0, ySel1, ySel1 };
                g.drawPolygon( xpoints, ypoints, 4 );
                g.setColor( Color.black );
            }
       }
        
        public void mouseClicked(MouseEvent e) {
            if ( ActionMode != 0 ) return;  // Only handle mouse clicks if in selection mode (23 Aug 04)
            int px = e.getX();
            int py = e.getY();
            //System.out.println("Mouse clicked at (" + px + ", " + py + ")");
            SelHoles = new Vector();
            for (int i=0; i<50; i++) {
                for (int j=0; j<ColumnNames.size(); j++) {
                    tblNeptuneHoles.setValueAt(" ",i,j);
                }
            }
            Enumeration eh = Holes.elements();
            int irow = 0;
            while (eh.hasMoreElements()) {
                NeptuneHoleInfo hi = (NeptuneHoleInfo) eh.nextElement();
                try {
                    double xlong = Double.parseDouble(hi.longitude);
                    double ylat = Double.parseDouble(hi.latitude);
                    int pxh = xMargin + (int)(xScale*(xlong-llBox[0]));
                    int pyh = yMargin + (int)(yScale*(llBox[3]-ylat));
                    if ( Math.abs((px-pxh))<5 && Math.abs(py-pyh)<5 ) {
                        SelHoles.add(hi);
                        tblNeptuneHoles.setValueAt(hi.holeID,irow,0);
                        tblNeptuneHoles.setValueAt(hi.latitude,irow,1);
                        tblNeptuneHoles.setValueAt(hi.longitude,irow,2);
                        tblNeptuneHoles.setValueAt(hi.sampleCount,irow,3);
                        tblNeptuneHoles.setValueAt(hi.waterDepth,irow,4);
                        tblNeptuneHoles.setValueAt(hi.metersPen,irow,5);
                        tblNeptuneHoles.setValueAt(hi.metersRec,irow,6);
                        tblNeptuneHoles.setValueAt(hi.oceanCode,irow,7);
                        irow++;
                    }
                } catch (Exception ex) {
                    // just don't draw anything
                }
            }
            if ( SelHoles.size() > 0 ) tblNeptuneHoles.setRowSelectionInterval(0,0);
            this.repaint();
        }
    
        public void mousePressed(MouseEvent e) {
            if ( ActionMode != 1 ) return;  // deal with this only if starting zoom-in selection box (23 Aug 04)
            int x = e.getX();
            int y = e.getY();
            //System.out.println( "Mouse pressed at (" + x + "," + y + ")" );
            if ( x < xMargin || x > xMargin+pxWidth || y < yMargin || y > yMargin+pxHeight ) return;  // outside plot area
            xSel0 = x;  
            ySel0 = y; 
        }
        
        public void mouseDragged(MouseEvent e) {
            mouseMoved(e);
            if ( ActionMode != 1 ) return;  // skip it if not dragging out zoom in selection box
            int x = e.getX();
            int y = e.getY();
            //System.out.println( "Mouse dragged to (" + x + "," + y + ")" );
            if ( xSel0 < 0 || ySel0 < 0 ) return;  // no selection box started
            if ( x < (xMargin-5) || x > (xMargin+pxWidth+5) || y < (yMargin+5) || y > (yMargin+pxHeight+5) ) {
                // too far off plot region; nix selection box
                xSel0 = -1;
                ySel0 = -1;
                xSel1 = -1;
                ySel1 = -1;
            } else {
                // re-set second corner . . .
                xSel1 = x;
                ySel1 = y;
            }
            this.repaint();
        }
    
        public void mouseReleased(MouseEvent e) {
            if ( ActionMode != 1 ) return;  // skip if not completing zoom-in box
            if ( xSel0 < 0 || ySel0 < 0 ) { // selection has already been cancelled
                xSel1 = -1;
                ySel1 = -1;
                this.repaint();
                return;
            }
            xSel1 = e.getX();
            ySel1 = e.getY();
            if ( Math.abs(xSel1-xSel0)<10 || Math.abs(ySel1-ySel0)<10 ) {  // too small; forget it
                xSel0 = -1;
                ySel0 = -1;
                xSel1 = -1;
                ySel1 = -1;
                this.repaint();
                return;
            }
            // put corner in current long-lat limits, if necessary
            if ( xSel1 < xMargin ) xSel1 = xMargin;
            if ( xSel1 > xMargin+pxWidth ) xSel1 = xMargin + pxWidth;
            if ( ySel1 < yMargin ) ySel1 = yMargin;
            if ( ySel1 > yMargin+pxHeight ) ySel1 = yMargin + pxHeight;
            // get long-lat limits for zoom box
            // llBox[0] is longitude of current left edge of map, corresponding with xMargin
            if ( xSel1 > xSel0 ) {   // selection box was dragged right
                llBox[1] = llBox[0] + (int)((xSel1-xMargin)/xScale);   // new right limit
                llBox[0] = llBox[0] + (int)((xSel0-xMargin)/xScale);   // new left limit
            } else {                 // selection box was dragged left
                llBox[1] = llBox[0] + (int)((xSel0-xMargin)/xScale);   // new right limit
                llBox[0] = llBox[0] + (int)((xSel1-xMargin)/xScale);   // new left limit
            }
            // llBox[3] is top edge of map, corresponding with yMargin
            if ( ySel1 > ySel0 ) {  // selection box was dragged down
                llBox[2] = llBox[3] - (int)((ySel1-yMargin)/yScale);   // new bottom latitude
                llBox[3] = llBox[3] - (int)((ySel0-yMargin)/yScale);   // new top latitude
            } else {
                llBox[2] = llBox[3] - (int)((ySel0-yMargin)/yScale);   // new bottom latitude
                llBox[3] = llBox[3] - (int)((ySel1-yMargin)/yScale);   // new top latitude
            }
            // clear selection box
            xSel0 = -1;
            ySel0 = -1;
            xSel1 = -1;
            ySel1 = -1;
            // return to select mode
            //ActionMode = 0;
            //btnSelect.setBackground( Color.gray );
            //btnZoomIn.setBackground( Color.lightGray );
            // repaint with new map
            NeedNewMap = true;
            this.repaint();
        }
    
        public void mouseEntered(MouseEvent e) {
        }
    
        public void mouseExited(MouseEvent e) {
        }
    
        public void mouseMoved(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            if ( xMargin<=x && x<=xMargin+pxWidth && yMargin<=y && y<=yMargin+pxHeight ) {
                double xlong = llBox[0] + (x-xMargin)/xScale;
                double ylat  = llBox[3] - (y-yMargin)/yScale;
                lblLatLong.setText( df2.format(ylat) + ", " + df2.format(xlong) );
            } else {
                lblLatLong.setText( "   " );
            }
            String hid = "   ";
            Enumeration eh = Holes.elements();
            while (eh.hasMoreElements()) {
                NeptuneHoleInfo hi = (NeptuneHoleInfo) eh.nextElement();
                double xlong = Double.parseDouble(hi.longitude);
                double ylat = Double.parseDouble(hi.latitude);
                int pxh = xMargin + (int)(xScale*(xlong-llBox[0]));
                int pyh = yMargin + (int)(yScale*(llBox[3]-ylat));
                if ( Math.abs(pxh-x)<=5 && Math.abs(pyh-y)<=5 ) {
                    hid = hi.holeID;
                    break;
                }
            }
            lblHoleID.setText( hid );
        }
    
        public void componentHidden(ComponentEvent e) {
        }
        
        public void componentMoved(ComponentEvent e) {
        }
        
        public void componentResized(ComponentEvent e) {
            //System.out.println("Map panel resized");
            NeedNewMap = true;
            this.repaint();
        }
        
        public void componentShown(ComponentEvent e) {
        }
        
    }
    
}
