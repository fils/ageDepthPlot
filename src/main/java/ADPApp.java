/*
 * ADPApp.java
 *
 * Created on November 11, 2003, 3:15 PM
 * Does it ever change?
 * 05-Apr-04:  Adding javadoc and declaring it Version 1.0
 * 07-Apr-04:  Decided to call it version 0.90, since still planning a few revisions
 * 16-Apr-04:  Adding NeptuneHoleFinder (map interface to Neptune)
 * 18-June-04:  Adding Edit menu for changing bits of plot . . .
 * 18-June-04:  . . . and starting on save as SVG
 * 23-June-04:  Working on screen capture & save as PNG & JPEG . . .
 * 25-June-04:  Continuing on saving of plot . . .
 * 01-July-04:  Wrapped WritePlotAsSVG.  Beautiful.
 * 02-July-04:  Working producing my own jar file of necessary ESRI MapObjects stuff, esri_mo20lite.jar
 *              . . . by unjarring original jar files (esri_mo20.jar & esri_mo2res.jar) & systematically
 *              deleting subdirectories (and maybe restoring them) to find out which ones are really
 *              needed.  Still end up with monster jar file (~2 MB).  Could probably reduce it somewhat
 *              more, but ran out of patience.
 * 02-July-04:  Giving up on stupid ESRI thing for the moment.  See NeptuneHoleFinder.java.
 *              Will comment this stuff out for now.
 * 22-Sept-04:  Wrapping up changes to ADPIO.writePlotAsSVG and calling it Version 0.99
 * 27-Sept-04:  Working on GetNeptuneData -- extracting appropriate datums for hole . . .
 * 28-Sept-04:  Working on query into taxonomy table to get synonyms . . .
 * 29-Sept-04:  Etc.
 * 01-Oct-04:   Working on translating taxon occurrences to LAD datums
 * 02-Oct-04:   Now working on FAD datums . . .
 * 03-Oct-04:   . . . finishing that up
 * 04-Oct-04:   Starting cleanup work . . . calling it Version 1.00
 * 05-Oct-04:   Working on retrieving age model from database
 * 06-Oct-04:   Finalizing Version 1.00
 * 07-Oct-04:   A few more little changes . . .
 * 17-Oct-04:   Trying to improve response to failure to get world relief image
 * 03-Dec-04:   Adding "Save LOC to Neptune..."
 * 10-Dec-04:   Working it some more.  Have been in interim . . . was that really 03-Dec-04?
 * 13-Dec-04:   Working on data sheet . . .
 * 22-Dec-04:   Working on LOC data sheet . . .
 * 27-Dec-04:   Adding DatumID, Comment in writeLOCtoDB
 * 29-Dec-04:   Trying to wrap some things up after above
 * 05-Jan-05:   Fixed up-jumping LOC point bug
 * 05-Jan-05:   Looking into responding to double-clicks on plot . . .
 * 06-Jan-05:   Rejecting datums for which both min age and max age are null or
 *                  both min depth and max depth are null
 * 14-Jan-05:   Switching from JDBC to . . . Josh's general-purpose SQL query via http
 * 28-Feb-05:   Finally getting back to switching from JDBC to QDF
 * 02-Mar-05:   Working more on GetNeptuneData()
 * 07-Mar-05:   Ditto
 * 14-Apr-05:   Changing GetNeptuneData to retrieve age model with max revision number,
 *              fixing up mapping of quality codes, and adding interpreted_by to label...
 */

/**
 * Main class for ADP application
 * @version 1.25 14-Apr-05
 * @author gcb
 */

// DRF was here with imports

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.util.Date;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

//  for assertioins
import java.io.IOException;


import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.FileOutputStream;
import java.io.*;
//import java.io.*;

public class ADPApp extends JFrame implements ActionListener {
    private ADPPlotPanel adpPP;

    private int appWidth;
    private int appHeight;
    private int appLocX;
    private int appLocY;

    private JPanel adpInfoPanel;  // information panel
    //private JLabel lblHoleID;
    //private JLabel lblOtherInfo;
    //private String OtherStuff;
    private JButton btnMovePoint;
    private JButton btnAddPoint;
    private JButton btnDeletePoint;
    private JButton btnAddHiatus;
    private JButton btnZoomIn;   // for zooming to selected rectangular region (gcb, 11/Feb/04)
    private JButton btnZoomOut;  // for zooming back out to default limits
    private JButton btnAxisParams;
    //private JButton btnSelectHole;
    //private JComboBox drpHole;
    //private JButton btnExit;
    //private static Vector HoleIDs;
    //private static Vector HoleFiles;
    /**
     * The static strings for janusAmp uploads  DRF
     */
    private static String jampUser;
    private static String jampPassword;
    /**
     * The current hole ID
     */
    public static String HoleID;
    /**
     * Opening comment line from core-depth data file
     */
    public static String CoreFileComment;
    private JLabel lblCoreFile;
    private static Hashtable CoreDepths;
    /**
     * Opening comment line from strat event data file
     */
    public static String StratFileComment;
    private JLabel lblStratFile;
    /**
     * Stratigraphic event data read from strat event file
     */
    public static Hashtable StratEvents = null;
    /**
     * Opening comment line from LOC file
     */
    public static String LOCFileComment;
    private JLabel lblLOCFile;
    /**
     * The vector of line-of-correlation data points
     */
    public static Vector LOCPoints = null;
    /**
     * The label showing the mouse location
     */
    public static JLabel lblMouseLoc;    // added 1/15/04
    /**
     * The label identifying a strat event near the current mouse location
     */
    public static JLabel lblStratEvent;  // added 1/15/04

    private JMenuItem itmGetNeptuneData;  // added 4/16/04 3:55 PM

    private JMenuItem itmGetLocalData;  // added 4/16/04 3:55 PM

    private JMenuItem itmGetJanusAmpData; // DRF
//    private JMenuItem itmGetJanusAmpDatav2; // DRF

    private JMenuItem itmSaveToJanusAmp; // DRF


    private JMenuItem itmReadData;  // added 1/13/04 1:23 PM
    private JMenuItem itmReadLOC;   // added 1/13/04 9:41 PM
    private JMenuItem itmSavePlot;  // added 6/18/04 2:44 PM
    private JMenuItem itmSaveData;
    private JMenuItem itmSaveLOC;
    private JMenuItem itmSaveLOCtoDB;     // added 12/8/04 1:29 PM
    private JMenuItem itmExit;

    private JMenuItem itmClearJampData;   //  DRF 
    private JMenuItem itmShowLabels;      // added 6/18/04 1:55 PM
    private JMenuItem itmEditPlotTitle;   // added 6/18/04 1:55 PM
    private JMenuItem itmEditXAxisTitle;  // added 6/18/04 1:55 PM
    private JMenuItem itmEditYAxisTitle;  // added 6/18/04 1:56 PM
    private JMenuItem itmShowData;        // added 10/7/04 7:59 AM
    private JMenuItem itmShowLOCData;     // added 12/15/04 4:10 PM

    private static Dimension ss;     // screen dimensions...

    private JDialog dlgBusy;  // added 10/6/04 3:19 PM
    private TextArea ta;
    //following made static 12/22/04 2:51 PM
    private static JFrame EvtDataSheet;   // added 12/13/04 3:20 PM
    private static JTable EvtTable;       // added 12/15/04 12:34 PM; changed to JTable 12/15/04 1:22 PM
    private static JFrame LOCDataSheet;   // added 12/13/04 3:20 PM
    private static JTable LOCTable;       // added 12/15/04 12:40 PM; changed to JTable 12/15/04 1:23 PM

    /**
     * Constructor for main app window
     */

    ADPApp() {
        super("CHRONOS Age-Depth Plot, Version 2.0");
        // 1.3 added JanusAMP support over 1.25

        Container c = getContentPane();
        c.setLayout(new BorderLayout(5, 5));

        Toolkit tk = Toolkit.getDefaultToolkit();
        ss = tk.getScreenSize();
        appWidth = (int) (0.80 * ss.width);
        appLocX = (int) (0.10 * ss.width);
        appHeight = (int) (0.80 * ss.height);
        appLocY = (int) (0.10 * ss.height);

        EvtDataSheet = null;  // added 12/13/04 3:23 PM
        EvtTable = null;      // added 12/15/04 12:40 PM
        LOCDataSheet = null;  // added 12/13/04 3:23 PM
        LOCTable = null;      // added 12/15/04 12:41 PM       

        JMenuBar mbar = new JMenuBar();
        setJMenuBar(mbar);
        JMenu mnuFile = new JMenu("File");
        mbar.add(mnuFile);
        mnuFile.setMnemonic('F');
        itmGetNeptuneData = new JMenuItem("Get Neptune Data...");
        itmGetNeptuneData.setMnemonic('N');
        itmGetNeptuneData.addActionListener(this);
        mnuFile.add(itmGetNeptuneData);

        itmGetLocalData = new JMenuItem("Get Local Data...");
//        itmGetLocalData.setMnemonic('N');
        itmGetLocalData.addActionListener(this);
        mnuFile.add(itmGetLocalData);

        itmGetJanusAmpData = new JMenuItem("Get JanusAmp Data...");
        itmGetJanusAmpData.setAccelerator(KeyStroke.getKeyStroke('J', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        itmGetJanusAmpData.setMnemonic('J');
        itmGetJanusAmpData.addActionListener(this);
        mnuFile.add(itmGetJanusAmpData);


        itmReadData = new JMenuItem("Read Data...");
        itmReadData.setAccelerator(KeyStroke.getKeyStroke('R', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        itmReadData.setMnemonic('R');
        itmReadData.addActionListener(this);
        mnuFile.add(itmReadData);


        itmReadLOC = new JMenuItem("Read LOC...");
        itmReadLOC.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        itmReadLOC.setMnemonic('L');
        itmReadLOC.addActionListener(this);
        mnuFile.add(itmReadLOC);
        itmSavePlot = new JMenuItem("Save Plot...");
        itmSavePlot.setAccelerator(KeyStroke.getKeyStroke('P', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

        itmSavePlot.setMnemonic('P');
        itmSavePlot.addActionListener(this);
        mnuFile.add(itmSavePlot);
        itmSaveData = new JMenuItem("Save Projected Data...");
//        itmSaveData.setMnemonic('P');
        itmSaveData.addActionListener(this);
        //itmSaveData.setEnabled( false );
        mnuFile.add(itmSaveData);
        itmSaveLOC = new JMenuItem("Save LOC...");
        itmSaveLOC.setAccelerator(KeyStroke.getKeyStroke('S', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));

        itmSaveLOC.setMnemonic('V');
        itmSaveLOC.addActionListener(this);
        //itmSaveLOC.setEnabled( false );
        mnuFile.add(itmSaveLOC);
        itmSaveLOCtoDB = new JMenuItem("Save LOC to Neptune...");
        itmSaveLOCtoDB.setMnemonic('C');
        itmSaveLOCtoDB.addActionListener(this);
        itmSaveLOCtoDB.setEnabled(false);
        mnuFile.add(itmSaveLOCtoDB);

        itmSaveToJanusAmp = new JMenuItem("Save LOC to JanusAmp...");
        itmSaveToJanusAmp.setAccelerator(KeyStroke.getKeyStroke('W', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        itmSaveToJanusAmp.setMnemonic('W');
        itmSaveToJanusAmp.addActionListener(this);
        mnuFile.add(itmSaveToJanusAmp);


        itmExit = new JMenuItem("Exit");
        itmExit.setAccelerator(KeyStroke.getKeyStroke('Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask(), false));
        itmExit.setMnemonic('Q');
        itmExit.addActionListener(this);
        mnuFile.add(itmExit);


        JMenu mnuEdit = new JMenu("Edit");
        mbar.add(mnuEdit);
        mnuEdit.setMnemonic('E');


        itmShowLabels = new JMenuItem("Show Event Labels");
        itmShowLabels.setMnemonic('L');
        itmShowLabels.addActionListener(this);
        mnuEdit.add(itmShowLabels);

        itmClearJampData = new JMenuItem("Clear JanusAMP Login Settings");
        itmClearJampData.addActionListener(this);
        mnuEdit.add(itmClearJampData);

        itmEditPlotTitle = new JMenuItem("Plot Title...");
        itmEditPlotTitle.setMnemonic('P');
        itmEditPlotTitle.addActionListener(this);
        mnuEdit.add(itmEditPlotTitle);
        itmEditXAxisTitle = new JMenuItem("X Axis Title...");
        itmEditXAxisTitle.setMnemonic('X');
        itmEditXAxisTitle.addActionListener(this);
        mnuEdit.add(itmEditXAxisTitle);
        itmEditYAxisTitle = new JMenuItem("Y Axis Title...");
        itmEditYAxisTitle.setMnemonic('Y');
        itmEditYAxisTitle.addActionListener(this);
        mnuEdit.add(itmEditYAxisTitle);
        itmShowData = new JMenuItem("Show Data Window");
        itmShowData.setMnemonic('D');
        itmShowData.addActionListener(this);
        itmShowData.setEnabled(false);
        mnuEdit.add(itmShowData);
        itmShowLOCData = new JMenuItem("Show LOC Data Window");
        itmShowLOCData.setMnemonic('L');
        itmShowLOCData.addActionListener(this);
        itmShowLOCData.setEnabled(false);
        mnuEdit.add(itmShowLOCData);

        adpPP = new ADPPlotPanel();
        adpPP.setSize(new Dimension((int) (0.80 * appWidth), (int) (0.80 * appHeight)));
        adpPP.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        adpPP.setActionMode(0);
        c.add(adpPP, BorderLayout.CENTER);

        Font bf = new Font("Serif", Font.BOLD, 24);
        JPanel PalettePanel = new JPanel();
        PalettePanel.setLayout(new GridLayout(7, 1, 1, 5));
        btnMovePoint = new JButton("< >");
        btnMovePoint.setFont(bf);
        btnMovePoint.setForeground(Color.blue);
        btnMovePoint.setToolTipText("Move control point");
        btnMovePoint.setBackground(Color.gray);
        btnMovePoint.addActionListener(this);
        PalettePanel.add(btnMovePoint);
        btnAddPoint = new JButton("+");
        btnAddPoint.setFont(bf);
        btnAddPoint.setForeground(Color.blue);
        btnAddPoint.setToolTipText("Add control point");
        btnAddPoint.setBackground(Color.lightGray);
        btnAddPoint.addActionListener(this);
        PalettePanel.add(btnAddPoint);
        btnDeletePoint = new JButton("-");
        btnDeletePoint.setFont(bf);
        btnDeletePoint.setForeground(Color.blue);
        btnDeletePoint.setToolTipText("Delete control point");
        btnDeletePoint.setBackground(Color.lightGray);
        btnDeletePoint.addActionListener(this);
        PalettePanel.add(btnDeletePoint);
        /*
        btnAddHiatus = new JButton("H");
        btnAddHiatus.setFont( bf );
        btnAddHiatus.setForeground( Color.blue );
        btnAddHiatus.setToolTipText("Add hiatus");
        btnAddHiatus.setBackground( Color.lightGray );
        btnAddHiatus.addActionListener(this);
        btnAddHiatus.setEnabled(false);
        PalettePanel.add(btnAddHiatus);
         */
        btnZoomIn = new JButton("Z+");
        btnZoomIn.setFont(bf);
        btnZoomIn.setForeground(Color.blue);
        btnZoomIn.setToolTipText("Zoom in (to selection)");
        btnZoomIn.setBackground(Color.lightGray);
        btnZoomIn.addActionListener(this);
        PalettePanel.add(btnZoomIn);
        btnZoomOut = new JButton("Z-");
        btnZoomOut.setFont(bf);
        btnZoomOut.setForeground(Color.blue);
        btnZoomOut.setToolTipText("Zoom out (to full extent)");
        btnZoomOut.setBackground(Color.lightGray);
        btnZoomOut.addActionListener(this);
        PalettePanel.add(btnZoomOut);
        btnAxisParams = new JButton("A");
        btnAxisParams.setFont(bf);
        btnAxisParams.setForeground(Color.blue);
        btnAxisParams.setToolTipText("Change axis parameters");
        btnAxisParams.setBackground(Color.lightGray);
        btnAxisParams.addActionListener(this);
        //btnAxisParams.setEnabled(false);
        PalettePanel.add(btnAxisParams);
        c.add(new JScrollPane(PalettePanel), BorderLayout.WEST);

        //JPanel NorthPanel = new JPanel();
        //NorthPanel.setLayout(new BorderLayout());

        adpInfoPanel = new JPanel();
        adpInfoPanel.setLayout(new GridLayout(3, 1, 5, 5));
        CoreFileComment = "[NONE]";
        lblCoreFile = new JLabel("  Core file info:  " + CoreFileComment);
        adpInfoPanel.add(lblCoreFile);
        StratFileComment = "[NONE]";
        lblStratFile = new JLabel("  Strat file info:  " + StratFileComment);
        adpInfoPanel.add(lblStratFile);
        LOCFileComment = "[NONE]";
        lblLOCFile = new JLabel("  LOC file info:  " + LOCFileComment);
        adpInfoPanel.add(lblLOCFile);


        c.add(adpInfoPanel, BorderLayout.NORTH);

        JPanel adpPlotInfoPanel = new JPanel();  // added 1/15/04 1:09 PM
        adpPlotInfoPanel.setLayout(new FlowLayout());
        adpPlotInfoPanel.add(new JLabel("Age, Depth:   "));
        lblMouseLoc = new JLabel("          ");
        lblMouseLoc.setForeground(Color.red);
        adpPlotInfoPanel.add(lblMouseLoc);
        adpPlotInfoPanel.add(new JLabel("   Event:   "));
        lblStratEvent = new JLabel("          ");
        lblStratEvent.setForeground(Color.red);
        adpPlotInfoPanel.add(lblStratEvent);
        c.add(adpPlotInfoPanel, BorderLayout.SOUTH);

        //NorthPanel.add(adpIP,BorderLayout.CENTER);

        /*
       JPanel adpBP = new JPanel();  // panel for Start over & Exit buttons
       adpBP.setLayout(new FlowLayout() );
       btnSelectHole = new JButton("Select Hole");
       btnSelectHole.addActionListener(this);
       adpBP.add(btnSelectHole);
       drpHole = new JComboBox(HoleIDs);
       drpHole.addActionListener(this);
       adpBP.add(drpHole);
       btnExit = new JButton("Exit");
       btnExit.addActionListener(this);
       adpBP.add(btnExit);

       NorthPanel.add(adpBP,BorderLayout.EAST);

       c.add( NorthPanel, BorderLayout.NORTH );
        */

        setSize(appWidth, appHeight);
        setLocation(appLocX, appLocY);

        show();
    }

    /**
     * Action handler for menu items and palette buttons
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == itmGetNeptuneData) {
            Cursor oldCursor = this.getCursor();  // cursor change added 12/29/04 1:44 PM
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (GetNeptuneData()) {    // added 4/16/04 3:58 PM
                lblCoreFile.setText("  Core file info:  " + CoreFileComment);
                lblStratFile.setText("  Strat file info:  " + StratFileComment);
                lblLOCFile.setText("  LOC file info:  " + LOCFileComment);
                RefreshEvtDataSheet();
                EvtDataSheet.show();
                updateLOCTable();
                LOCDataSheet.show();
                itmShowData.setEnabled(true);
                itmShowLOCData.setEnabled(true);
                adpPP.setAxisParameters(getDefaultAxisParameters());
                itmSaveLOCtoDB.setEnabled(true);  // allow saving of LOC to db; 12/8/04 2:28 PM
                adpPP.repaint();
            }
            this.setCursor(oldCursor);
        }
        else if (e.getSource() == itmGetLocalData) {
            Cursor oldCursor = this.getCursor();  // cursor change added 12/29/04 1:44 PM
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (GetLocalData()) {    // added 4/16/04 3:58 PM
                lblCoreFile.setText("  Core file info:  " + CoreFileComment);
                lblStratFile.setText("  Strat file info:  " + StratFileComment);
                lblLOCFile.setText("  LOC file info:  " + LOCFileComment);
                RefreshEvtDataSheet();
                EvtDataSheet.show();
                updateLOCTable();
                LOCDataSheet.show();
                itmShowData.setEnabled(true);
                itmShowLOCData.setEnabled(true);
                adpPP.setAxisParameters(getDefaultAxisParameters());
                itmSaveLOCtoDB.setEnabled(true);  // allow saving of LOC to db; 12/8/04 2:28 PM
                adpPP.repaint();
            }
            this.setCursor(oldCursor);
        }
//        else if (e.getSource() == itmGetJanusAmpDataFOO) {
//            Cursor oldCursor = this.getCursor();  // cursor change added 12/29/04 1:44 PM
//            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
//            if (getJanusAmpData()) {    // added 4/16/04 3:58 PM
//                lblCoreFile.setText("  Core file info:  " + CoreFileComment);
//                lblStratFile.setText("  Strat file info:  " + StratFileComment);
//                lblLOCFile.setText("  LOC file info:  " + LOCFileComment);
//                RefreshEvtDataSheet();
//                EvtDataSheet.show();
//                updateLOCTable();
//                LOCDataSheet.show();
//                itmShowData.setEnabled(true);
//                itmShowLOCData.setEnabled(true);
//                adpPP.setAxisParameters(getDefaultAxisParameters());
//                itmSaveLOCtoDB.setEnabled(true);  // allow saving of LOC to db; 12/8/04 2:28 PM
//                adpPP.repaint();
//            }
//            this.setCursor(oldCursor);
//        }
        else if (e.getSource() == itmGetJanusAmpData) {
            Cursor oldCursor = this.getCursor();  // cursor change added 12/29/04 1:44 PM
            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            if (ReadJanusData()) {    // added 4/16/04 3:58 PM
//                InitLOC();       //  only case where no LOC?
                itmShowLOCData.setEnabled(true);
                lblCoreFile.setText("  Core file info:  " + CoreFileComment);
                lblStratFile.setText("  Strat file info:  " + StratFileComment);
                RefreshEvtDataSheet();
                EvtDataSheet.show();
                itmShowData.setEnabled(true);
                updateLOCTable();
                LOCDataSheet.show();
                itmShowLOCData.setEnabled(true);
                adpPP.PlotTitle = "Age-Depth Plot for Hole " + HoleID;
                adpPP.XAxisTitle = "Age (Ma)";
                adpPP.YAxisTitle = "Depth (mbsf)";
                adpPP.setAxisParametersOrigin(getDefaultAxisParameters());   //  DRF use Origin version to set 0,0
                itmSaveLOCtoDB.setEnabled(false);  // disallow saving of LOC for local data; 12/8/04 2:30 PM
                adpPP.repaint();
            }
            this.setCursor(oldCursor);
        } else if (e.getSource() == itmSaveToJanusAmp) {
            if (LOCPoints == null || LOCPoints.size() < 1) {
                JOptionPane.showMessageDialog(null, "No LOC data to write.");
                return;
            }
            if (writeLOCtoJanusAmp()) {
                JOptionPane.showMessageDialog(null, "Writing LOC to JanusAMP: Succeeded.");
            } else {
                JOptionPane.showMessageDialog(null, "Writing LOC to JanusAmp: Failed.");
            }
        } else if (e.getSource() == itmReadData) {
            if (ReadData()) {
                InitLOC();
                itmShowLOCData.setEnabled(true);
                lblCoreFile.setText("  Core file info:  " + CoreFileComment);
                lblStratFile.setText("  Strat file info:  " + StratFileComment);
                RefreshEvtDataSheet();
                EvtDataSheet.show();
                itmShowData.setEnabled(true);
                updateLOCTable();
                LOCDataSheet.show();
                itmShowLOCData.setEnabled(true);
                adpPP.PlotTitle = "Age-Depth Plot for Hole " + HoleID;
                adpPP.XAxisTitle = "Age (Ma)";
                adpPP.YAxisTitle = "Depth (mbsf)";
                adpPP.setAxisParameters(getDefaultAxisParameters());
                itmSaveLOCtoDB.setEnabled(false);  // disallow saving of LOC for local data; 12/8/04 2:30 PM
                adpPP.repaint();
            }
        } else if (e.getSource() == itmReadLOC) {
            if (ReadLOC()) {
                lblLOCFile.setText("  LOC file info:  " + LOCFileComment);
                updateLOCTable();
                LOCDataSheet.show();
                //RefreshLOCDataSheet();
                //LOCDataSheet.show();
                adpPP.setAxisParameters(getDefaultAxisParameters());
                adpPP.repaint();
            }
        } else if (e.getSource() == itmSavePlot) {
            ADPIO.writePlot(adpPP);
        } else if (e.getSource() == itmSaveData) {
            if (StratEvents == null || StratEvents.size() < 1) {
                JOptionPane.showMessageDialog(null, "No data to save.");
                return;
            }
            if (LOCPoints == null || LOCPoints.size() < 1) {
                JOptionPane.showMessageDialog(null, "Cannot project data without LOC.");
                return;
            }
            ADPIO.writeProjData(StratEvents, LOCPoints);
        } else if (e.getSource() == itmSaveLOC) {
            if (LOCPoints == null || LOCPoints.size() < 1) {
                JOptionPane.showMessageDialog(null, "No LOC data to write.");
                return;
            }
            ADPIO.writeLOCData(LOCPoints);
        } else if (e.getSource() == itmSaveLOCtoDB) {
            if (LOCPoints == null || LOCPoints.size() < 1) {
                JOptionPane.showMessageDialog(null, "No LOC data to write.");
                return;
            }
            if (writeLOCtoNeptune()) {
                JOptionPane.showMessageDialog(null, "Writing LOC to database: Succeeded.");
            } else {
                JOptionPane.showMessageDialog(null, "Writing LOC to database: Failed.");
            }
        } else if (e.getSource() == itmShowLabels) {  // added 18-June-04
            if (adpPP.ShowLabels) {
                adpPP.ShowLabels = false;
                itmShowLabels.setText("Show Event Labels");
            } else {
                adpPP.ShowLabels = true;
                itmShowLabels.setText("Hide Event Labels");
            }
            adpPP.repaint();
        } else if (e.getSource() == itmEditPlotTitle) {  // added 18-June-04
            String NewTitle = JOptionPane.showInputDialog("Plot title: ", adpPP.PlotTitle);
            if (NewTitle != null) {
                adpPP.PlotTitle = NewTitle;
                adpPP.repaint();
            }
        } else if (e.getSource() == itmEditXAxisTitle) {  // added 18-June-04
            String NewTitle = JOptionPane.showInputDialog("X axis title: ", adpPP.XAxisTitle);
            if (NewTitle != null) {
                adpPP.XAxisTitle = NewTitle;
                adpPP.repaint();
            }
        } else if (e.getSource() == itmEditYAxisTitle) {  // added 18-June-04
            String NewTitle = JOptionPane.showInputDialog("Y axis title: ", adpPP.YAxisTitle);
            if (NewTitle != null) {
                adpPP.YAxisTitle = NewTitle;
                adpPP.repaint();
            }
        } else if (e.getSource() == itmShowData) {
            if (EvtDataSheet != null) EvtDataSheet.setVisible(true);
        } else if (e.getSource() == itmShowLOCData) {
            if (LOCDataSheet != null) LOCDataSheet.setVisible(true);
        } else if (e.getSource() == btnMovePoint) {
            adpPP.setActionMode(0);  // for moving age model control points
            btnMovePoint.setBackground(Color.gray);
            btnAddPoint.setBackground(Color.lightGray);
            btnDeletePoint.setBackground(Color.lightGray);
            //btnAddHiatus.setBackground( Color.lightGray );
            btnZoomIn.setBackground(Color.lightGray);
            adpPP.repaint();
        } else if (e.getSource() == btnAddPoint) {
            adpPP.setActionMode(1);  // for adding age model control points
            btnMovePoint.setBackground(Color.lightGray);
            btnAddPoint.setBackground(Color.gray);
            btnDeletePoint.setBackground(Color.lightGray);
            //btnAddHiatus.setBackground( Color.lightGray );
            btnZoomIn.setBackground(Color.lightGray);
            adpPP.repaint();
        } else if (e.getSource() == btnDeletePoint) {
            adpPP.setActionMode(2);  // for deleting age model control points
            btnMovePoint.setBackground(Color.lightGray);
            btnAddPoint.setBackground(Color.lightGray);
            btnDeletePoint.setBackground(Color.gray);
            //btnAddHiatus.setBackground( Color.lightGray );
            btnZoomIn.setBackground(Color.lightGray);
            adpPP.repaint();
            /*
           } else if ( e.getSource() == btnAddHiatus ) {
               adpPP.setActionMode(3);  // for adding a hiatus
               btnMovePoint.setBackground( Color.lightGray );
               btnAddPoint.setBackground( Color.lightGray );
               btnDeletePoint.setBackground( Color.lightGray );
               btnAddHiatus.setBackground( Color.gray );
               adpPP.repaint();
            */
        } else if (e.getSource() == btnZoomIn) {
            adpPP.setActionMode(3);  // for making rectangular selection and zooming to it
            btnMovePoint.setBackground(Color.lightGray);
            btnAddPoint.setBackground(Color.lightGray);
            btnDeletePoint.setBackground(Color.lightGray);
            //btnAddHiatus.setBackground( Color.lightGray );
            btnZoomIn.setBackground(Color.gray);
            adpPP.repaint();
        } else if (e.getSource() == btnZoomOut) {
            adpPP.setAxisParameters(getDefaultAxisParameters());
            adpPP.repaint();
        } else if (e.getSource() == btnAxisParams) {
            // make axis parameters an array element to be able to pass by reference
            ADPAxisParameters[] adpAP = {adpPP.getAxisParameters()};
            dlgAxisParameters dap = new dlgAxisParameters(null);
            boolean goAhead = dap.showDialog(adpAP);
            dap.dispose();
            if (goAhead) {   // if axis parameter changes were accepted
                adpPP.setAxisParameters(adpAP[0]);
                adpPP.repaint();
            }
        } else if (e.getSource() == itmExit) {
            //
            System.exit(0);
        }
    }

    /**
     * Updates the strat event data sheet; added 15-Dec-2004
     */
    private void RefreshEvtDataSheet
    () {
        Rectangle bnds = null;
        if (EvtDataSheet == null) {
            bnds = new Rectangle((int) (0.25 * ss.width), (int) (0.60 * ss.height),
                    (int) (0.70 * ss.width), (int) (0.20 * ss.height));
        } else {
            bnds = EvtDataSheet.getBounds();
            EvtDataSheet.setVisible(false);
            EvtDataSheet = null;
            System.gc();
        }
        EvtDataSheet = new JFrame("Event Data");
        Container c = EvtDataSheet.getContentPane();
        c.setLayout(new BorderLayout());
        // ADPStratEventTable returns a table model
        EvtTable = new JTable(new ADPStratEventTable(StratEvents));
        c.add(new JScrollPane(EvtTable), BorderLayout.CENTER);
        EvtDataSheet.setBounds(bnds);
    }

    /**
     * Generates the LOC points data sheet; added 15-Dec-2004
     */
    private static void GenerateLOCDataSheet
    () {
        if (LOCPoints == null || LOCPoints.size() == 0) {
            LOCDataSheet = null;
            return;
        }
        LOCDataSheet = new JFrame("LOC Control Points");
        LOCDataSheet.setBounds((int) (0.27 * ss.width), (int) (0.65 * ss.height),
                (int) (0.70 * ss.width), (int) (0.20 * ss.height));
        Container c = LOCDataSheet.getContentPane();
        c.setLayout(new BorderLayout());
        // ADPLOCDataTable returns a table model
        LOCTable = new JTable(new ADPLOCDataTable(LOCPoints));
        LOCTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        LOCTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        c.add(new JScrollPane(LOCTable), BorderLayout.CENTER);
    }

    /**
     * updates contents of LOCTable
     */
    public static void updateLOCTable
    () {
        if (LOCPoints == null) return;  // added 05-Jan-2005
        if (LOCDataSheet == null) {
            GenerateLOCDataSheet();
        } else {
            int[] colWidth = new int[4];
            for (int i = 0; i < 4; i++) {
                colWidth[i] = LOCTable.getColumnModel().getColumn(i).getWidth();
            }
            LOCTable.setModel(new ADPLOCDataTable(LOCPoints));
            for (int i = 0; i < 4; i++) {
                LOCTable.getColumnModel().getColumn(i).setPreferredWidth(colWidth[i]);
            }
        }
        JComboBox comboBox = new JComboBox();
        comboBox.addItem("[None]");
        if (StratEvents != null) {
            Enumeration ese = StratEvents.elements();
            while (ese.hasMoreElements()) {
                ADPStratEvent se = (ADPStratEvent) ese.nextElement();
                comboBox.addItem(se.getDatumLabel());
            }
        }
        LOCTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
    }

    /**
     * Attempts to extract strat event data from JanusAmp
     *
     * @return true if strat data successfully read, false if not
     */
    private boolean getJanusAmpDataTest
    () {


        return true;
    }

    /**
     * Attempts to extract strat event data from JanusAmp
     *
     * @return true if strat data successfully read, false if not
     */
    private boolean getJanusAmpData() {

        java.io.BufferedReader buffy = null;
        NeptuneHoleInfo hinfo = new NeptuneHoleInfo("108_664D", "45", "45", "nos", "400", "20", "10", "Indian");

        //trying dlgBusy again, 29-Dec-04
        if (dlgBusy == null) {
            Frame BadDad = null;
            dlgBusy = new JDialog(BadDad, "Retrieving Neptune data...", false);
            dlgBusy.setBounds((int) (0.15 * ss.width), (int) (0.45 * ss.height), (int) (0.80 * ss.width), (int) (0.25 * ss.height));
            dlgBusy.getContentPane().setLayout(new BorderLayout());
            //dlgBusy.getContentPane().add( new JLabel("Retrieving Neptune data...") );
            ta = new TextArea(50, 1000);
            dlgBusy.getContentPane().add(ta, BorderLayout.CENTER);

        }
        ta.setText("Neptune Data for Hole " + hinfo.holeID + "\n");
        ta.append("Group\tName\tLabel\tMinAge(Ma)\tMaxAge(Ma)\tMinDepth(mbsf)\tMaxDepth(mbsf)\n");
        dlgBusy.setVisible(true);

        Hashtable tmpStratEvents = new Hashtable();
        int evtno = 0;
        String[] fg = {"F", "D", "R", "N"};

        for (int ifg = 0; ifg < 4; ifg++) {
            String FossilGroup = fg[ifg];

            // 27-Sept-04: getting sample depths & taxa . . .
            // 28-Feb-05:  Switching to QDF query
            // rs = null;
            // rsmd = null;
            // moreData = false;
            //System.out.println("Before sample query");
            buffy = null;
            try {
                // Query one
                // This gets the simapleid, sampledepth, taxonid and sytaxonid
                //  and places them in the 4 Vector objects that follow

                String query = "select neptune_sample.sample_id as SampleID, " +
                        "neptune_sample.sample_depth_mbsf as SampleDepth, " +
                        "neptune_sample_taxa.taxon_id as TaxonID, " +
                        "taxSyn(neptune_sample_taxa.taxon_id) as SynTaxonID " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' " +
                        "order by SampleDepth";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After sample query");
            // 27-Sept-04: Extracting sample-taxa info . . .

            Vector SampleID = new Vector();
            Vector SampleDepth = new Vector();
            Vector SampleOrigTaxonID = new Vector();  // what is actually listed for sample
            Vector SampleValidTaxonID = new Vector(); // added 02-Mar-05; presumably valid id's...

            //System.out.println("Before processing sample query");
            try {
                String inLine = buffy.readLine();   // pop off header line
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    //String sid = rs.getObject("SampleID").toString().trim();
                    String sid = ronny.nextToken().trim();
                    SampleID.add(sid);
                    String sdp = ronny.nextToken().trim();
                    SampleDepth.add(sdp);
                    String tid = ronny.nextToken().trim();
                    SampleOrigTaxonID.add(tid);
                    String syd = ronny.nextToken().trim();
                    SampleValidTaxonID.add(syd);
                    //System.out.println( "sampno, sample, depth, taxon, synTaxon: " + (++isamp) + ", " + sid + ", " + sdp + ", " + tid + ", " + syd );
                }
            } catch (Exception ex) {
                //dlgBusy.hide();
                //dlgBusy = null;
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After processing sample query");

            //System.out.println( "nsamp: " + SampleID.size() );

            if (SampleID.size() == 0) continue;  // go on to next fossil group

            // 02-Mar-05:  Getting distinct valid taxa directly from query

            buffy = null;
            try {
                //  Qeurry two

                String query = "select distinct taxSyn(neptune_sample_taxa.taxon_id) " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute distinct taxa query: " + ex.getMessage());
                return false;
            }

            Vector DistinctValidTaxa = new Vector();

            try {
                String inLine = buffy.readLine();   // pop off header line
                //System.out.println(inLine);
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    String dvt = inLine.trim();
                    //System.out.println(dvt);
                    DistinctValidTaxa.add(dvt);
                }
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing distinct taxa query: " + ex.getMessage());
                return false;
            }

            if (DistinctValidTaxa.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            buffy = null;
            try {
                String query = "select neptune_datum_def.* " +
                        "from neptune_datum_def inner join " +
                        "(select distinct taxSyn(neptune_sample_taxa.taxon_id) as vtid " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ) as bernie " +
                        "on neptune_datum_def.taxon_id = bernie.vtid " +
                        "where not(datum_age_min_ma is null and datum_age_max_ma is null)";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                buffy = null;
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Unable to execute datum query: " + ex.getMessage() );
                //return false;
            }

            if (buffy == null) continue;  // go on to next fossil group

            Vector Datums = new Vector();

            try {
                String inLine = buffy.readLine();  // header line
                //System.out.println(inLine);
                if (inLine == null) continue;      // no header line
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    Datums.add(new ADPNeptuneDatum(inLine));
                    //System.out.println(inLine);
                }
            } catch (Exception ex) {
                // maybe just try to ignore it and go on (09-Mar-05)
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Problems processing datum query: " + ex.getMessage() );
                //return false;
            }

            if (Datums.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            int nsamp = SampleDepth.size();

            // candidates for FAD and LAD datums . . .
            String[] FADThings = {"B", "FAD", "BT", "base", "Be"};
            String[] LADThings = {"T", "LAD", "TOP", "top", "Te"};

            // try to extract FAD/LAD datums for each distinct valid taxon (01-Oct-04)
            Enumeration etax = DistinctValidTaxa.elements();
            while (etax.hasMoreElements()) {

                String vtid = (String) etax.nextElement();  // id for current taxon . . .
                //System.out.println("vtid: " + vtid);

                //System.out.println( "got some for taxon " + vtid );

                // try to find minDepth & maxDepth for LAD . . .
                int isamp;
                String minDepth = null;
                String maxDepth = (String) SampleDepth.get(0);
                for (isamp = 0; isamp < nsamp; isamp++) {   // loop down from top until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(maxDepth)) {  // have moved down to next sample
                        minDepth = maxDepth;  // depth of next sample up
                        maxDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp==nsamp ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp < nsamp) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // the datum ID; added 12/15/04 2:48 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < LADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;
                                String dtype = datum.type;
                                //System.out.println( "LADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                // 10-Mar-05:  Adding check for taxon id since Datums now includes all vtid's
                                if (taxid.equals(vtid) & dtype.equals(LADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

                // now try to find minDepth & maxDepth for FAD . . .
                minDepth = (String) SampleDepth.get(nsamp - 1);
                maxDepth = null;
                for (isamp = (nsamp - 1); isamp >= 0; isamp--) {   // loop up from bottom until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(minDepth)) {  // have moved up to next sample
                        maxDepth = minDepth;  // depth of next sample down
                        minDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp<0 ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp >= 0) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // added 12/15/04 2:51 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < FADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;   // added 10-Mar-05
                                String dtype = datum.type;
                                //System.out.println( "FADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                if (taxid.equals(vtid) & dtype.equals(FADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

            }

        }                    //  end of loop on 4 fossil types

        if (tmpStratEvents.size() < 3) {
            //dlgBusy.hide();
            //dlgBusy = null;
            dlgBusy.setVisible(false);
            JOptionPane.showMessageDialog(null, "Too few datums found.");
            return false;
        }

        // apparently succeeded in reading data . . .
        StratEvents = OrderStratEvents(tmpStratEvents);
        //StratEvents = new Hashtable();
        ADPSymbolPalette.initPalette();
        Enumeration sekeys = StratEvents.keys();
        while (sekeys.hasMoreElements()) {
            Integer sekey = (Integer) sekeys.nextElement();
            ADPStratEvent se = (ADPStratEvent) StratEvents.get(sekey);
            se.setDepthRange(CoreDepths);
            ADPSymbolPalette.addEvent(se.EventGroup);
            //StratEvents.put( sekey, se );
        }
        //dlgBusy.setVisible(false);

        StratFileComment = "Data from Neptune Hole " + hinfo.holeID
                + ", lat: " + hinfo.latitude
                + ", long: " + hinfo.longitude;

        HoleID = hinfo.holeID;   // added 12/8/04 2:36 PM -- to allow saving of LOC for this hole

        //dlgBusy.hide();
        //dlgBusy = null;

        CoreDepths = null;   // forget core depths
        CoreFileComment = "[NONE]";

        // 10-Oct-05: trying to retrieve age model data
        // Turn HoleID to SiteHole, which is hole id without leg
        LOCPoints = null;
        int iu = hinfo.holeID.indexOf('_');
        String SiteHole = hinfo.holeID.substring(iu + 1).trim();  // this is the key in the age model table
        //System.out.println(SiteHole);

        // try to extract age model data . . .
        // 07-Mar-05:  Switching to QDF query
        // 14-Apr-05:  Get max revision number first...
        int rvno = -1;
        buffy = null;
        try {
            // select max(revision_no) from neptune_age_model where site_hole = '63A'
            //  returns a simple int that shows the max revsion number
            String query = "select max(revision_no) from neptune_age_model " +
                    "where site_hole = '" + SiteHole + "' ";
            buffy = ADPIO.getCSVReaderFromQuery(query);
            buffy.readLine();  // header line
            rvno = Integer.parseInt(buffy.readLine());
        } catch (Exception ex) {
            LOCPoints = null;   // presumably nothing there
        }

        // now get age model data if we got a revision number...
        if (rvno > -1) {
            buffy = null;
            try {
                //rs = null;
                //rsmd = null;
                //moreData = false;
                // select age_ma, depth_mbsf from neptune_age_model  where neptune_age_model.site_hole = '63A'  and revision_no = 0  order by neptune_age_model.age_ma
                //  returns the age depth pairs age_ma depth_mbsf
                String query = "select age_ma, depth_mbsf from neptune_age_model " +
                        "where neptune_age_model.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno + " " +
                        "order by neptune_age_model.age_ma";
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //System.out.println(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                //moreData = rs.first();
                //if (moreData) LOCPoints = new Vector();
                LOCPoints = new Vector();
                String inLine = buffy.readLine();  // header line, presumably
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println("age, depth: " + rs.getObject("age_ma").toString() + ", " + rs.getObject("depth_mbsf").toString());
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    double age = Double.parseDouble(ronny.nextToken());
                    double depth = Double.parseDouble(ronny.nextToken());
                    LOCPoints.add(new ADPLOCPoint(age, depth));
                    //moreData = rs.next();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Problem extracting LOC data: " + ex.getMessage());
                LOCPoints = null;
            }
        }

        //initialize LOCPoints to default if no age model found, else try to get history info
        if (LOCPoints == null || LOCPoints.size() == 0) {
            InitLOC();
            LOCFileComment = "[No LOC found in database]";
        } else {
            LOCFileComment = "[No LOC history found in database]";
            try {
                //rs = null;
                //rsmd = null;
                buffy = null;

                //  Grab the history info
                String query = "select revision_no, age_quality, date_worked, interpreted_by " +
                        "from neptune_age_model_history " +
                        "where neptune_age_model_history.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno;
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                if (buffy != null) {
                    String inLine = buffy.readLine();  // header
                    inLine = buffy.readLine();
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    String rvsn = ronny.nextToken().trim();
                    String qual = ronny.nextToken().trim();
                    if (qual.equals("E")) {
                        qual = "Excellent";
                    } else if (qual.equals("V") || qual.equals("VG")) {
                        qual = "Very Good";
                    } else if (qual.equals("G") || qual.equals("v.2 G") || qual.equals("X")) {
                        qual = "Good";
                    } else if (qual.equals("M")) {
                        qual = "Medium";
                    } else if (qual.equals("P")) {
                        qual = "Poor";
                    } else if (qual.equals("U")) {
                        qual = "Unknown";
                    } else {
                        qual = "Unknown";
                    }
                    String date = ronny.nextToken().trim();
                    String auth = ronny.nextToken().trim();
                    LOCFileComment = "Revision: " + rvsn + ";   Quality: " + qual +
                            ";   Date worked: " + date + "; Interpreted by: " + auth;
                }
            } catch (Exception ex) {
                LOCFileComment = "[No LOC history found in database]";
            }
        }

        adpPP.PlotTitle = "Age-Depth Plot for Neptune Hole " + hinfo.holeID;
        adpPP.XAxisTitle = "Age (Ma)";
        adpPP.YAxisTitle = "Depth (mbsf)";

        //dlgBusy.setTitle("Neptune data retrieved");

        dlgBusy.setVisible(false);
        return true;
    }


    private boolean GetNeptuneData() {

        /* 07-March-05:  Blowing away JDBC stuff
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String userName = "PASSWORD_HERE";
            String password = "PASSWORD_HERE";
            conn = DriverManager.getConnection("jdbc:postgresql://cdb0.geol.iastate.edu/neptune",userName,password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Unable to connect to Neptune database: " + ex.getMessage() );
            return false;
        }
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        boolean moreData = false;
        */

        // 14-Jan-2005:  Switching from JDBC to Josh's general-purpose SQL query via http

        //  ---- REST Code ---
        java.io.BufferedReader buffy = null;
        try {
            String query = "select neptune_hole_summary.hole_id as HoleID, " +
                    "min(neptune_hole_summary.latitude) as Latitude, " +
                    "min(neptune_hole_summary.longitude) as Longitude, " +
                    "count(neptune_sample.sample_id) as SampleCount, " +
                    "min(neptune_hole_summary.water_depth) as WaterDepth, " +
                    "min(neptune_hole_summary.meters_penetrated) as MetersPenetrated, " +
                    "min(neptune_hole_summary.meters_recovered) as MetersRecovered, " +
                    "min(neptune_hole_summary.ocean_code) as Ocean " +
                    "from neptune_hole_summary inner join neptune_sample " +
                    "on neptune_hole_summary.hole_id = neptune_sample.hole_id " +
                    "group by HoleID " +
                    "order by Latitude, Longitude";
            buffy = ADPIO.getCSVReaderFromQuery(query);
            // --- REST Code ---

            // -- SQL code ---
            //Statement stmt = conn.createStatement();
            //rs = stmt.executeQuery(query);
            //rsmd = rs.getMetaData();

            // -- SQL code ----
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to execute query: " + ex.getMessage());
            return false;
        }

        Vector Holes = new Vector();

        try {
            String inLine = buffy.readLine();  // header line
            //System.out.println(inLine);
            while ((inLine = buffy.readLine()) != null) {
                //System.out.println(inLine);
                StringTokenizer ronny = new StringTokenizer(inLine, ",");
                String hid = ronny.nextToken().trim();
                String lat = ronny.nextToken().trim();
                String lon = ronny.nextToken().trim();
                String nos = ronny.nextToken().trim();
                String dep = ronny.nextToken().trim();
                String pen = ronny.nextToken().trim();
                String rec = ronny.nextToken().trim();
                String sea = ronny.nextToken().trim();
                Holes.add(new NeptuneHoleInfo(hid, lat, lon, nos, dep, pen, rec, sea));
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Problem processing query: " + ex.getMessage());
            return false;
        }



        /*
        try {
            if ( !rs.first() ) {
                JOptionPane.showMessageDialog(null,"No hole summary data retrieved");
                return false;
            }
            do {
                String hid = rs.getObject("HoleID").toString().trim();
                String lat = rs.getObject("Latitude").toString().trim();
                String lon = rs.getObject("Longitude").toString().trim();
                String nos = rs.getObject("SampleCount").toString().trim();
                String dep = rs.getObject("WaterDepth").toString().trim();
                String pen = rs.getObject("MetersPenetrated").toString().trim();
                String rec = rs.getObject("MetersRecovered").toString().trim();
                String sea = rs.getObject("Ocean").toString().trim();
                Holes.add( new NeptuneHoleInfo(hid,lat,lon,nos,dep,pen,rec,sea) );
            } while ( rs.next() );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Problem processing query: " + ex.getMessage() );
            return false;
        }
        */

        /*
       try {
           conn.close();
       } catch (Exception ex) {
           JOptionPane.showMessageDialog(null,"Problem closing connection: " + ex.getMessage() );
       }
        */

        /*
       Enumeration moo = Holes.elements();
       int irec = 0;
       while ( moo.hasMoreElements() ) {
           NeptuneHoleInfo hi = (NeptuneHoleInfo) moo.nextElement();
           irec++;
           System.out.println( "record: " + irec + "; " + hi.toString() );
       }
        */


        int hfWidth = (int) (0.90 * appWidth);
        int hfHeight = (int) (0.90 * appHeight);
        NeptuneHoleFinder hf = new NeptuneHoleFinder(null, Holes, hfWidth, hfHeight);
        //hf.setSize( (int)(0.80*appWidth), (int)(0.80*appHeight) );
        hf.setLocation(appLocX + 20, appLocY + 20);
        NeptuneHoleInfo hinfo = hf.showDialog();
        hf.dispose();
        System.gc();
        if (hinfo == null) return false;

        /*
        try {
            if ( conn.isClosed() ) {
                try {
                    Class.forName("org.postgresql.Driver");
                    String userName = "PASSWORD_HERE";
                    String password = "PASSWORD_HERE";
                    conn = DriverManager.getConnection("jdbc:postgresql://cdb0.geol.iastate.edu/neptune",userName,password);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,"Unable to connect to Neptune database: " + ex.getMessage() );
                    return false;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Unable to connect to Neptune database: " + ex.getMessage() );
            return false;
        }
        */

        /* commenting out 27-Sept-04: Now trying to map out distinct taxa & datums
       rs = null;
       rsmd = null;
       moreData = false;
       try {
           String query = "select * from neptune_sample where hole_id = '" + hinfo.holeID + "'" +
                          " order by sample_depth_mbsf";
           Statement stmt = conn.createStatement();
           rs = stmt.executeQuery(query);
           rsmd = rs.getMetaData();
           moreData = rs.first();
       } catch (Exception ex) {
           JOptionPane.showMessageDialog(null,"Unable to execute query: " + ex.getMessage() );
           return false;
       }
       if ( !moreData ) {
           JOptionPane.showMessageDialog(null,"No data retrieved" );
           return false;
       }
        */

        //trying dlgBusy again, 29-Dec-04
        if (dlgBusy == null) {
            Frame BadDad = null;
            dlgBusy = new JDialog(BadDad, "Retrieving Neptune data...", false);
            dlgBusy.setBounds((int) (0.15 * ss.width), (int) (0.45 * ss.height), (int) (0.80 * ss.width), (int) (0.25 * ss.height));
            dlgBusy.getContentPane().setLayout(new BorderLayout());
            //dlgBusy.getContentPane().add( new JLabel("Retrieving Neptune data...") );
            ta = new TextArea(50, 1000);
            dlgBusy.getContentPane().add(ta, BorderLayout.CENTER);

        }
        ta.setText("Neptune Data for Hole " + hinfo.holeID + "\n");
        ta.append("Group\tName\tLabel\tMinAge(Ma)\tMaxAge(Ma)\tMinDepth(mbsf)\tMaxDepth(mbsf)\n");
        dlgBusy.setVisible(true);

        // cursor change added 12/29/04 2:06 PM
        // undoing that, 12/29/04 4:31 PM
        //Cursor oldCursor = getCursor();
        //setCursor(Cursor.WAIT_CURSOR);

        // 07-March-05:  Switching to loop over fossil groups

        Hashtable tmpStratEvents = new Hashtable();
        int evtno = 0;
        String[] fg = {"F", "D", "R", "N"};

        for (int ifg = 0; ifg < 4; ifg++) {
            String FossilGroup = fg[ifg];

            // 27-Sept-04: getting sample depths & taxa . . .
            // 28-Feb-05:  Switching to QDF query
            // rs = null;
            // rsmd = null;
            // moreData = false;
            //System.out.println("Before sample query");
            buffy = null;
            try {
                String query = "select neptune_sample.sample_id as SampleID, " +
                        "neptune_sample.sample_depth_mbsf as SampleDepth, " +
                        "neptune_sample_taxa.taxon_id as TaxonID, " +
                        "taxSyn(neptune_sample_taxa.taxon_id) as SynTaxonID " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' " +
                        "order by SampleDepth";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After sample query");

            // 27-Sept-04: Extracting sample-taxa info . . .

            Vector SampleID = new Vector();
            Vector SampleDepth = new Vector();
            Vector SampleOrigTaxonID = new Vector();  // what is actually listed for sample
            Vector SampleValidTaxonID = new Vector(); // added 02-Mar-05; presumably valid id's...

            //System.out.println("Before processing sample query");
            try {
                String inLine = buffy.readLine();   // pop off header line
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    //String sid = rs.getObject("SampleID").toString().trim();
                    String sid = ronny.nextToken().trim();
                    SampleID.add(sid);
                    String sdp = ronny.nextToken().trim();
                    SampleDepth.add(sdp);
                    String tid = ronny.nextToken().trim();
                    SampleOrigTaxonID.add(tid);
                    String syd = ronny.nextToken().trim();
                    SampleValidTaxonID.add(syd);
                    //System.out.println( "sampno, sample, depth, taxon, synTaxon: " + (++isamp) + ", " + sid + ", " + sdp + ", " + tid + ", " + syd );
                }
            } catch (Exception ex) {
                //dlgBusy.hide();
                //dlgBusy = null;
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After processing sample query");

            //System.out.println( "nsamp: " + SampleID.size() );

            if (SampleID.size() == 0) continue;  // go on to next fossil group

            // 02-Mar-05:  Getting distinct valid taxa directly from query

            buffy = null;
            try {
                String query = "select distinct taxSyn(neptune_sample_taxa.taxon_id) " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute distinct taxa query: " + ex.getMessage());
                return false;
            }

            Vector DistinctValidTaxa = new Vector();

            try {
                String inLine = buffy.readLine();   // pop off header line
                //System.out.println(inLine);
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    String dvt = inLine.trim();
                    //System.out.println(dvt);
                    DistinctValidTaxa.add(dvt);
                }
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing distinct taxa query: " + ex.getMessage());
                return false;
            }

            if (DistinctValidTaxa.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            /*

 // 28-Sept-04: getting distinct taxa & synonyms three levels deep (three times
  //             past initial (zeroth) query into taxonomy table . . .
   rs = null;
   rsmd = null;
   moreData = false;
   try {
   String query = "select distinct neptune_sample.hole_id, " +
   "neptune_taxonomy.taxon_id as TaxonID0, " +
   "neptune_taxonomy.taxon_status as TaxonStatus0, " +
   "neptune_taxonomy_1.taxon_id as TaxonID1, " +
   "neptune_taxonomy_1.taxon_status as TaxonStatus1, " +
   "neptune_taxonomy_2.taxon_id as TaxonID2, " +
   "neptune_taxonomy_2.taxon_status as TaxonStatus2, " +
   "neptune_taxonomy_3.taxon_id as TaxonID3, " +
   "neptune_taxonomy_3.taxon_status as TaxonStatus3 " +
   "from ((((neptune_sample_taxa inner join neptune_sample " +
   "on neptune_sample_taxa.sample_id = neptune_sample.sample_id) " +
   "inner join neptune_taxonomy " +
   "on neptune_sample_taxa.taxon_id = neptune_taxonomy.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_1 " +
   "on neptune_taxonomy.taxon_synon_to = neptune_taxonomy_1.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_2 " +
   "on neptune_taxonomy_1.taxon_synon_to = neptune_taxonomy_2.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_3 " +
   "on neptune_taxonomy_2.taxon_synon_to = neptune_taxonomy_3.taxon_id " +
   "where neptune_sample.hole_id = '" + hinfo.holeID + "'";
   Statement stmt = conn.createStatement();
   rs = stmt.executeQuery(query);
   rsmd = rs.getMetaData();
   moreData = rs.first();
   } catch (Exception ex) {
   //dlgBusy.hide();
    //dlgBusy = null;
     dlgBusy.setVisible(false);
     JOptionPane.showMessageDialog(null,"Unable to execute query: " + ex.getMessage() );
     return false;
     }
     if ( !moreData ) {
     //dlgBusy.hide();
      //dlgBusy = null;
       dlgBusy.setVisible(false);
       JOptionPane.showMessageDialog(null,"No taxon data retrieved" );
       return false;
       }

       Vector OrigTaxonID = new Vector();        // distinct taxa as listed in holes
       Vector OrigTaxonStatus = new Vector();    // status thereof
       Vector ValidTaxonID = new Vector();       // corresponding valid (hopefully) taxa, not nec. distinct
       Vector ValidTaxonStatus = new Vector();   // actual status assigned thereunto
       Vector DistinctValidTaxa = new Vector();  // just that (29-Sept-04)

       try {  // loop over set of original taxa, trying to rend down to valid taxa

       if ( !rs.first() ) {
       //dlgBusy.hide();
        //dlgBusy = null;
         dlgBusy.setVisible(false);
         JOptionPane.showMessageDialog(null,"No taxon data retrieved");
         return false;
         }

         do {

         String vtid = null;  // will hold ID of final "valid" taxon id
         String vstat = null;  // will hold actual status associated with final "valid" id

         Object tid = rs.getObject("TaxonID0");
         Object tst = rs.getObject("TaxonStatus0");
         if ( tid == null ) continue;   // shouldn't happen . . .
         vtid = tid.toString().trim();  // initialize "valid id" to original . . .
         if ( tst == null ) {
         vstat = " ";
         } else {
         vstat = tst.toString().trim();
         }
         OrigTaxonID.add( vtid );
         OrigTaxonStatus.add( vstat );

         int ilev = 0;
         while ( vstat.equals("S") && ilev < 3 ) {  // go to next level if current id is synonym
         ilev++;
         tid = rs.getObject("TaxonID" + ilev);
         tst = rs.getObject("TaxonStatus" + ilev);
         if ( tid == null || tst == null ) break;  // quit with what we had before
         vtid = tid.toString().trim();
         vstat = tst.toString().trim();
         }

         ValidTaxonID.add( vtid );
         ValidTaxonStatus.add( vstat );

         if ( !DistinctValidTaxa.contains(vtid) ) {  // added 29-Sept-04
         DistinctValidTaxa.add(vtid);
         } else {
         //System.out.println( "Repeated taxon id: " + vtid );
          }

          } while ( rs.next() );

          } catch (Exception ex) {
          //dlgBusy.hide();
           //dlgBusy = null;
            dlgBusy.setVisible(false);
            JOptionPane.showMessageDialog(null,"Problem processing taxon query: " + ex.getMessage() );
            return false;
            }
            */

            /*
   for ( int i=0; i<OrigTaxonID.size(); i++ ) {
   System.out.println( " " + i + ", " + OrigTaxonID.get(i) +
   ", " + OrigTaxonStatus.get(i) +
   ", " + ValidTaxonID.get(i) +
   ", " + ValidTaxonStatus.get(i) );
   }

   //System.out.println("Number of distinct valid taxa: " + DistinctValidTaxa.size() );

    // Map original taxon id's in each sample to corresponding "valid" taxon id's (29-Sept-04)

     Vector SampleOrigTaxonStatus = new Vector();   // status of original taxon id in sample; not using yet (29-Sept-04) but might . . .
     Vector SampleValidTaxonID = new Vector();      // "Valid" id's corresponding to original id's in samples
     Vector SampleValidTaxonStatus = new Vector();  // Actual status associated with above

     for ( int i=0; i<SampleOrigTaxonID.size(); i++ ) {
     int itax = OrigTaxonID.indexOf((String)SampleOrigTaxonID.get(i));  // index in list of distinct original taxa
     if ( itax < 0 ) {
     SampleOrigTaxonStatus.add(" ");
     SampleValidTaxonID.add(" ");
     SampleValidTaxonStatus.add(" ");
     } else {
     SampleOrigTaxonStatus.add( OrigTaxonStatus.get(itax) );
     SampleValidTaxonID.add( ValidTaxonID.get(itax) );
     SampleValidTaxonStatus.add( ValidTaxonStatus.get(itax) );
     }

     //System.out.println( "sampno, sample, depth, otid, ostat, vtid, vstat: " +
      //                       SampleID.get(i) + ", " +
       //                       SampleDepth.get(i) + ", " +
        //                       SampleOrigTaxonID.get(i) + ", " +
         //                       SampleOrigTaxonStatus.get(i) + ", " +
          //                       SampleValidTaxonID.get(i) + ", " +
           //                       SampleValidTaxonStatus.get(i) );


            }
            */

            //Hashtable tmpStratEvents = new Hashtable();
            //int nsamp = SampleDepth.size();
            //System.out.println("nsamp: " + nsamp);
            //System.out.println("SampleID.size() " + SampleID.size() );
            //int evtno = 0;

            // 10-Mar-05:  Getting datums for all distinct valid taxa (in fossil group) in one query:

            buffy = null;
            try {
                String query = "select neptune_datum_def.* " +
                        "from neptune_datum_def inner join " +
                        "(select distinct taxSyn(neptune_sample_taxa.taxon_id) as vtid " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ) as bernie " +
                        "on neptune_datum_def.taxon_id = bernie.vtid " +
                        "where not(datum_age_min_ma is null and datum_age_max_ma is null)";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                buffy = null;
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Unable to execute datum query: " + ex.getMessage() );
                //return false;
            }

            if (buffy == null) continue;  // go on to next fossil group

            Vector Datums = new Vector();

            try {
                String inLine = buffy.readLine();  // header line
                //System.out.println(inLine);
                if (inLine == null) continue;      // no header line
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    Datums.add(new ADPNeptuneDatum(inLine));
                    //System.out.println(inLine);
                }
            } catch (Exception ex) {
                // maybe just try to ignore it and go on (09-Mar-05)
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Problems processing datum query: " + ex.getMessage() );
                //return false;
            }

            if (Datums.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            int nsamp = SampleDepth.size();

            // candidates for FAD and LAD datums . . .
            String[] FADThings = {"B", "FAD", "BT", "base", "Be"};
            String[] LADThings = {"T", "LAD", "TOP", "top", "Te"};

            // try to extract FAD/LAD datums for each distinct valid taxon (01-Oct-04)
            Enumeration etax = DistinctValidTaxa.elements();
            while (etax.hasMoreElements()) {

                String vtid = (String) etax.nextElement();  // id for current taxon . . .
                //System.out.println("vtid: " + vtid);

                //System.out.println( "got some for taxon " + vtid );

                // try to find minDepth & maxDepth for LAD . . .
                int isamp;
                String minDepth = null;
                String maxDepth = (String) SampleDepth.get(0);
                for (isamp = 0; isamp < nsamp; isamp++) {   // loop down from top until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(maxDepth)) {  // have moved down to next sample
                        minDepth = maxDepth;  // depth of next sample up
                        maxDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp==nsamp ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp < nsamp) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // the datum ID; added 12/15/04 2:48 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < LADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;
                                String dtype = datum.type;
                                //System.out.println( "LADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                // 10-Mar-05:  Adding check for taxon id since Datums now includes all vtid's
                                if (taxid.equals(vtid) & dtype.equals(LADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

                // now try to find minDepth & maxDepth for FAD . . .
                minDepth = (String) SampleDepth.get(nsamp - 1);
                maxDepth = null;
                for (isamp = (nsamp - 1); isamp >= 0; isamp--) {   // loop up from bottom until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(minDepth)) {  // have moved up to next sample
                        maxDepth = minDepth;  // depth of next sample down
                        minDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp<0 ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp >= 0) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // added 12/15/04 2:51 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < FADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;   // added 10-Mar-05
                                String dtype = datum.type;
                                //System.out.println( "FADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                if (taxid.equals(vtid) & dtype.equals(FADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

            }

        }

        if (tmpStratEvents.size() < 3) {
            //dlgBusy.hide();
            //dlgBusy = null;
            dlgBusy.setVisible(false);
            JOptionPane.showMessageDialog(null, "Too few datums found.");
            return false;
        }

        // apparently succeeded in reading data . . .
        StratEvents = OrderStratEvents(tmpStratEvents);
        //StratEvents = new Hashtable();
        ADPSymbolPalette.initPalette();
        Enumeration sekeys = StratEvents.keys();
        while (sekeys.hasMoreElements()) {
            Integer sekey = (Integer) sekeys.nextElement();
            ADPStratEvent se = (ADPStratEvent) StratEvents.get(sekey);
            se.setDepthRange(CoreDepths);
            ADPSymbolPalette.addEvent(se.EventGroup);
            //StratEvents.put( sekey, se );
        }
        //dlgBusy.setVisible(false);

        StratFileComment = "Data from Neptune Hole " + hinfo.holeID
                + ", lat: " + hinfo.latitude
                + ", long: " + hinfo.longitude;

        HoleID = hinfo.holeID;   // added 12/8/04 2:36 PM -- to allow saving of LOC for this hole

        //dlgBusy.hide();
        //dlgBusy = null;

        CoreDepths = null;   // forget core depths
        CoreFileComment = "[NONE]";

        // 10-Oct-05: trying to retrieve age model data
        // Turn HoleID to SiteHole, which is hole id without leg
        LOCPoints = null;
        int iu = hinfo.holeID.indexOf('_');
        String SiteHole = hinfo.holeID.substring(iu + 1).trim();  // this is the key in the age model table
        //System.out.println(SiteHole);

        // try to extract age model data . . .
        // 07-Mar-05:  Switching to QDF query
        // 14-Apr-05:  Get max revision number first...
        int rvno = -1;
        buffy = null;
        try {
            String query = "select max(revision_no) from neptune_age_model " +
                    "where site_hole = '" + SiteHole + "' ";
            buffy = ADPIO.getCSVReaderFromQuery(query);
            buffy.readLine();  // header line
            rvno = Integer.parseInt(buffy.readLine());
        } catch (Exception ex) {
            LOCPoints = null;   // presumably nothing there
        }

        // now get age model data if we got a revision number...
        if (rvno > -1) {
            buffy = null;
            try {
                //rs = null;
                //rsmd = null;
                //moreData = false;
                String query = "select age_ma, depth_mbsf from neptune_age_model " +
                        "where neptune_age_model.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno + " " +
                        "order by neptune_age_model.age_ma";
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //System.out.println(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                //moreData = rs.first();
                //if (moreData) LOCPoints = new Vector();
                LOCPoints = new Vector();
                String inLine = buffy.readLine();  // header line, presumably
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println("age, depth: " + rs.getObject("age_ma").toString() + ", " + rs.getObject("depth_mbsf").toString());
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    double age = Double.parseDouble(ronny.nextToken());
                    double depth = Double.parseDouble(ronny.nextToken());
                    LOCPoints.add(new ADPLOCPoint(age, depth));
                    //moreData = rs.next();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Problem extracting LOC data: " + ex.getMessage());
                LOCPoints = null;
            }
        }

        //initialize LOCPoints to default if no age model found, else try to get history info
        if (LOCPoints == null || LOCPoints.size() == 0) {
            InitLOC();
            LOCFileComment = "[No LOC found in database]";
        } else {
            LOCFileComment = "[No LOC history found in database]";
            try {
                //rs = null;
                //rsmd = null;
                buffy = null;
                String query = "select revision_no, age_quality, date_worked, interpreted_by " +
                        "from neptune_age_model_history " +
                        "where neptune_age_model_history.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno;
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                if (buffy != null) {
                    String inLine = buffy.readLine();  // header
                    inLine = buffy.readLine();
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    String rvsn = ronny.nextToken().trim();
                    String qual = ronny.nextToken().trim();
                    if (qual.equals("E")) {
                        qual = "Excellent";
                    } else if (qual.equals("V") || qual.equals("VG")) {
                        qual = "Very Good";
                    } else if (qual.equals("G") || qual.equals("v.2 G") || qual.equals("X")) {
                        qual = "Good";
                    } else if (qual.equals("M")) {
                        qual = "Medium";
                    } else if (qual.equals("P")) {
                        qual = "Poor";
                    } else if (qual.equals("U")) {
                        qual = "Unknown";
                    } else {
                        qual = "Unknown";
                    }
                    String date = ronny.nextToken().trim();
                    String auth = ronny.nextToken().trim();
                    LOCFileComment = "Revision: " + rvsn + ";   Quality: " + qual +
                            ";   Date worked: " + date + "; Interpreted by: " + auth;
                }
            } catch (Exception ex) {
                LOCFileComment = "[No LOC history found in database]";
            }
        }

        adpPP.PlotTitle = "Age-Depth Plot for Neptune Hole " + hinfo.holeID;
        adpPP.XAxisTitle = "Age (Ma)";
        adpPP.YAxisTitle = "Depth (mbsf)";

        //dlgBusy.setTitle("Neptune data retrieved");

        dlgBusy.setVisible(false);
        return true;

    }



    private boolean GetLocalData() {

        // 07-March-05:  Blowing away JDBC stuff
        // March 2011:  bringing back JDBC stuff  :)
        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String userName = "PASSWORD_HERE";
            String password = "PASSWORD_HERE";
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/nsb_alt",userName,password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Unable to connect to Local Neptune database: " + ex.getMessage() );
            return false;
        }
        ResultSet rs = null;
        ResultSetMetaData rsmd = null;
        boolean moreData = false;


        // 14-Jan-2005:  Switching from JDBC to Josh's general-purpose SQL query via http

        //  ---- REST Code ---

        // REST ONLY?
        java.io.BufferedReader buffy = null;


        try {
            String query = "select neptune_hole_summary.hole_id as HoleID, " +
                    "min(neptune_hole_summary.latitude) as Latitude, " +
                    "min(neptune_hole_summary.longitude) as Longitude, " +
                    "count(neptune_sample.sample_id) as SampleCount, " +
                    "min(neptune_hole_summary.water_depth) as WaterDepth, " +
                    "min(neptune_hole_summary.meters_penetrated) as MetersPenetrated, " +
                    "min(neptune_hole_summary.meters_recovered) as MetersRecovered, " +
                    "min(neptune_hole_summary.ocean_code) as Ocean " +
                    "from neptune_hole_summary inner join neptune_sample " +
                    "on neptune_hole_summary.hole_id = neptune_sample.hole_id " +
                    "group by HoleID " +
                    "order by Latitude, Longitude";
//            buffy = ADPIO.getCSVReaderFromQuery(query);

            // --- REST Code ---

            // -- SQL code ---
            Statement stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            rsmd = rs.getMetaData();

            // -- SQL code ----
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to execute query: " + ex.getMessage());
            return false;
        }

        Vector Holes = new Vector();

//        try {
//            String inLine = buffy.readLine();  // header line
//            //System.out.println(inLine);
//            while ((inLine = buffy.readLine()) != null) {
//                //System.out.println(inLine);
//                StringTokenizer ronny = new StringTokenizer(inLine, ",");
//                String hid = ronny.nextToken().trim();
//                String lat = ronny.nextToken().trim();
//                String lon = ronny.nextToken().trim();
//                String nos = ronny.nextToken().trim();
//                String dep = ronny.nextToken().trim();
//                String pen = ronny.nextToken().trim();
//                String rec = ronny.nextToken().trim();
//                String sea = ronny.nextToken().trim();
//                Holes.add(new NeptuneHoleInfo(hid, lat, lon, nos, dep, pen, rec, sea));
//            }
//        } catch (Exception ex) {
//            JOptionPane.showMessageDialog(null, "Problem processing query: " + ex.getMessage());
//            return false;
//        }




        try {
            if ( !rs.first() ) {
                JOptionPane.showMessageDialog(null,"No hole summary data retrieved");
                return false;
            }
            do {
                String hid = rs.getObject("HoleID").toString().trim();
                String lat = rs.getObject("Latitude").toString().trim();
                String lon = rs.getObject("Longitude").toString().trim();
                String nos = rs.getObject("SampleCount").toString().trim();
                String dep = rs.getObject("WaterDepth").toString().trim();
                String pen = rs.getObject("MetersPenetrated").toString().trim();
                String rec = rs.getObject("MetersRecovered").toString().trim();
                String sea = rs.getObject("Ocean").toString().trim();
                Holes.add( new NeptuneHoleInfo(hid,lat,lon,nos,dep,pen,rec,sea) );
            } while ( rs.next() );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Problem processing query: " + ex.getMessage() );
            return false;
        }



       try {
           conn.close();
       } catch (Exception ex) {
           JOptionPane.showMessageDialog(null,"Problem closing connection: " + ex.getMessage() );
       }



       Enumeration moo = Holes.elements();
       int irec = 0;
       while ( moo.hasMoreElements() ) {
           NeptuneHoleInfo hi = (NeptuneHoleInfo) moo.nextElement();
           irec++;
           System.out.println( "record: " + irec + "; " + hi.toString() );
       }



        int hfWidth = (int) (0.90 * appWidth);
        int hfHeight = (int) (0.90 * appHeight);
        NeptuneHoleFinder hf = new NeptuneHoleFinder(null, Holes, hfWidth, hfHeight);
        //hf.setSize( (int)(0.80*appWidth), (int)(0.80*appHeight) );
        hf.setLocation(appLocX + 20, appLocY + 20);
        NeptuneHoleInfo hinfo = hf.showDialog();
        hf.dispose();
        System.gc();
        if (hinfo == null) return false;

        /*
        try {
            if ( conn.isClosed() ) {
                try {
                    Class.forName("org.postgresql.Driver");
                    String userName = "PASSWORD_HERE";
                    String password = "PASSWORD_HERE";
                    conn = DriverManager.getConnection("jdbc:postgresql://cdb0.geol.iastate.edu/neptune",userName,password);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null,"Unable to connect to Neptune database: " + ex.getMessage() );
                    return false;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,"Unable to connect to Neptune database: " + ex.getMessage() );
            return false;
        }
        */

        /* commenting out 27-Sept-04: Now trying to map out distinct taxa & datums
       rs = null;
       rsmd = null;
       moreData = false;
       try {
           String query = "select * from neptune_sample where hole_id = '" + hinfo.holeID + "'" +
                          " order by sample_depth_mbsf";
           Statement stmt = conn.createStatement();
           rs = stmt.executeQuery(query);
           rsmd = rs.getMetaData();
           moreData = rs.first();
       } catch (Exception ex) {
           JOptionPane.showMessageDialog(null,"Unable to execute query: " + ex.getMessage() );
           return false;
       }
       if ( !moreData ) {
           JOptionPane.showMessageDialog(null,"No data retrieved" );
           return false;
       }
        */

        //trying dlgBusy again, 29-Dec-04
        if (dlgBusy == null) {
            Frame BadDad = null;
            dlgBusy = new JDialog(BadDad, "Retrieving Neptune data...", false);
            dlgBusy.setBounds((int) (0.15 * ss.width), (int) (0.45 * ss.height), (int) (0.80 * ss.width), (int) (0.25 * ss.height));
            dlgBusy.getContentPane().setLayout(new BorderLayout());
            //dlgBusy.getContentPane().add( new JLabel("Retrieving Neptune data...") );
            ta = new TextArea(50, 1000);
            dlgBusy.getContentPane().add(ta, BorderLayout.CENTER);

        }
        ta.setText("Neptune Data for Hole " + hinfo.holeID + "\n");
        ta.append("Group\tName\tLabel\tMinAge(Ma)\tMaxAge(Ma)\tMinDepth(mbsf)\tMaxDepth(mbsf)\n");
        dlgBusy.setVisible(true);

        // cursor change added 12/29/04 2:06 PM
        // undoing that, 12/29/04 4:31 PM
        //Cursor oldCursor = getCursor();
        //setCursor(Cursor.WAIT_CURSOR);

        // 07-March-05:  Switching to loop over fossil groups

        Hashtable tmpStratEvents = new Hashtable();
        int evtno = 0;
        String[] fg = {"F", "D", "R", "N"};

        for (int ifg = 0; ifg < 4; ifg++) {
            String FossilGroup = fg[ifg];

            // 27-Sept-04: getting sample depths & taxa . . .
            // 28-Feb-05:  Switching to QDF query
            // rs = null;
            // rsmd = null;
            // moreData = false;
            //System.out.println("Before sample query");
            buffy = null;
            try {
                String query = "select neptune_sample.sample_id as SampleID, " +
                        "neptune_sample.sample_depth_mbsf as SampleDepth, " +
                        "neptune_sample_taxa.taxon_id as TaxonID, " +
                        "taxSyn(neptune_sample_taxa.taxon_id) as SynTaxonID " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' " +
                        "order by SampleDepth";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After sample query");

            // 27-Sept-04: Extracting sample-taxa info . . .

            Vector SampleID = new Vector();
            Vector SampleDepth = new Vector();
            Vector SampleOrigTaxonID = new Vector();  // what is actually listed for sample
            Vector SampleValidTaxonID = new Vector(); // added 02-Mar-05; presumably valid id's...

            //System.out.println("Before processing sample query");
            try {
                String inLine = buffy.readLine();   // pop off header line
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    //String sid = rs.getObject("SampleID").toString().trim();
                    String sid = ronny.nextToken().trim();
                    SampleID.add(sid);
                    String sdp = ronny.nextToken().trim();
                    SampleDepth.add(sdp);
                    String tid = ronny.nextToken().trim();
                    SampleOrigTaxonID.add(tid);
                    String syd = ronny.nextToken().trim();
                    SampleValidTaxonID.add(syd);
                    //System.out.println( "sampno, sample, depth, taxon, synTaxon: " + (++isamp) + ", " + sid + ", " + sdp + ", " + tid + ", " + syd );
                }
            } catch (Exception ex) {
                //dlgBusy.hide();
                //dlgBusy = null;
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing sample query: " + ex.getMessage());
                return false;
            }
            //System.out.println("After processing sample query");

            //System.out.println( "nsamp: " + SampleID.size() );

            if (SampleID.size() == 0) continue;  // go on to next fossil group

            // 02-Mar-05:  Getting distinct valid taxa directly from query

            buffy = null;
            try {
                String query = "select distinct taxSyn(neptune_sample_taxa.taxon_id) " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Unable to execute distinct taxa query: " + ex.getMessage());
                return false;
            }

            Vector DistinctValidTaxa = new Vector();

            try {
                String inLine = buffy.readLine();   // pop off header line
                //System.out.println(inLine);
                int isamp = 0;
                while ((inLine = buffy.readLine()) != null) {
                    String dvt = inLine.trim();
                    //System.out.println(dvt);
                    DistinctValidTaxa.add(dvt);
                }
            } catch (Exception ex) {
                dlgBusy.setVisible(false);
                JOptionPane.showMessageDialog(null, "Problem processing distinct taxa query: " + ex.getMessage());
                return false;
            }

            if (DistinctValidTaxa.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            /*

 // 28-Sept-04: getting distinct taxa & synonyms three levels deep (three times
  //             past initial (zeroth) query into taxonomy table . . .
   rs = null;
   rsmd = null;
   moreData = false;
   try {
   String query = "select distinct neptune_sample.hole_id, " +
   "neptune_taxonomy.taxon_id as TaxonID0, " +
   "neptune_taxonomy.taxon_status as TaxonStatus0, " +
   "neptune_taxonomy_1.taxon_id as TaxonID1, " +
   "neptune_taxonomy_1.taxon_status as TaxonStatus1, " +
   "neptune_taxonomy_2.taxon_id as TaxonID2, " +
   "neptune_taxonomy_2.taxon_status as TaxonStatus2, " +
   "neptune_taxonomy_3.taxon_id as TaxonID3, " +
   "neptune_taxonomy_3.taxon_status as TaxonStatus3 " +
   "from ((((neptune_sample_taxa inner join neptune_sample " +
   "on neptune_sample_taxa.sample_id = neptune_sample.sample_id) " +
   "inner join neptune_taxonomy " +
   "on neptune_sample_taxa.taxon_id = neptune_taxonomy.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_1 " +
   "on neptune_taxonomy.taxon_synon_to = neptune_taxonomy_1.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_2 " +
   "on neptune_taxonomy_1.taxon_synon_to = neptune_taxonomy_2.taxon_id) " +
   "left join neptune_taxonomy as neptune_taxonomy_3 " +
   "on neptune_taxonomy_2.taxon_synon_to = neptune_taxonomy_3.taxon_id " +
   "where neptune_sample.hole_id = '" + hinfo.holeID + "'";
   Statement stmt = conn.createStatement();
   rs = stmt.executeQuery(query);
   rsmd = rs.getMetaData();
   moreData = rs.first();
   } catch (Exception ex) {
   //dlgBusy.hide();
    //dlgBusy = null;
     dlgBusy.setVisible(false);
     JOptionPane.showMessageDialog(null,"Unable to execute query: " + ex.getMessage() );
     return false;
     }
     if ( !moreData ) {
     //dlgBusy.hide();
      //dlgBusy = null;
       dlgBusy.setVisible(false);
       JOptionPane.showMessageDialog(null,"No taxon data retrieved" );
       return false;
       }

       Vector OrigTaxonID = new Vector();        // distinct taxa as listed in holes
       Vector OrigTaxonStatus = new Vector();    // status thereof
       Vector ValidTaxonID = new Vector();       // corresponding valid (hopefully) taxa, not nec. distinct
       Vector ValidTaxonStatus = new Vector();   // actual status assigned thereunto
       Vector DistinctValidTaxa = new Vector();  // just that (29-Sept-04)

       try {  // loop over set of original taxa, trying to rend down to valid taxa

       if ( !rs.first() ) {
       //dlgBusy.hide();
        //dlgBusy = null;
         dlgBusy.setVisible(false);
         JOptionPane.showMessageDialog(null,"No taxon data retrieved");
         return false;
         }

         do {

         String vtid = null;  // will hold ID of final "valid" taxon id
         String vstat = null;  // will hold actual status associated with final "valid" id

         Object tid = rs.getObject("TaxonID0");
         Object tst = rs.getObject("TaxonStatus0");
         if ( tid == null ) continue;   // shouldn't happen . . .
         vtid = tid.toString().trim();  // initialize "valid id" to original . . .
         if ( tst == null ) {
         vstat = " ";
         } else {
         vstat = tst.toString().trim();
         }
         OrigTaxonID.add( vtid );
         OrigTaxonStatus.add( vstat );

         int ilev = 0;
         while ( vstat.equals("S") && ilev < 3 ) {  // go to next level if current id is synonym
         ilev++;
         tid = rs.getObject("TaxonID" + ilev);
         tst = rs.getObject("TaxonStatus" + ilev);
         if ( tid == null || tst == null ) break;  // quit with what we had before
         vtid = tid.toString().trim();
         vstat = tst.toString().trim();
         }

         ValidTaxonID.add( vtid );
         ValidTaxonStatus.add( vstat );

         if ( !DistinctValidTaxa.contains(vtid) ) {  // added 29-Sept-04
         DistinctValidTaxa.add(vtid);
         } else {
         //System.out.println( "Repeated taxon id: " + vtid );
          }

          } while ( rs.next() );

          } catch (Exception ex) {
          //dlgBusy.hide();
           //dlgBusy = null;
            dlgBusy.setVisible(false);
            JOptionPane.showMessageDialog(null,"Problem processing taxon query: " + ex.getMessage() );
            return false;
            }
            */

            /*
   for ( int i=0; i<OrigTaxonID.size(); i++ ) {
   System.out.println( " " + i + ", " + OrigTaxonID.get(i) +
   ", " + OrigTaxonStatus.get(i) +
   ", " + ValidTaxonID.get(i) +
   ", " + ValidTaxonStatus.get(i) );
   }

   //System.out.println("Number of distinct valid taxa: " + DistinctValidTaxa.size() );

    // Map original taxon id's in each sample to corresponding "valid" taxon id's (29-Sept-04)

     Vector SampleOrigTaxonStatus = new Vector();   // status of original taxon id in sample; not using yet (29-Sept-04) but might . . .
     Vector SampleValidTaxonID = new Vector();      // "Valid" id's corresponding to original id's in samples
     Vector SampleValidTaxonStatus = new Vector();  // Actual status associated with above

     for ( int i=0; i<SampleOrigTaxonID.size(); i++ ) {
     int itax = OrigTaxonID.indexOf((String)SampleOrigTaxonID.get(i));  // index in list of distinct original taxa
     if ( itax < 0 ) {
     SampleOrigTaxonStatus.add(" ");
     SampleValidTaxonID.add(" ");
     SampleValidTaxonStatus.add(" ");
     } else {
     SampleOrigTaxonStatus.add( OrigTaxonStatus.get(itax) );
     SampleValidTaxonID.add( ValidTaxonID.get(itax) );
     SampleValidTaxonStatus.add( ValidTaxonStatus.get(itax) );
     }

     //System.out.println( "sampno, sample, depth, otid, ostat, vtid, vstat: " +
      //                       SampleID.get(i) + ", " +
       //                       SampleDepth.get(i) + ", " +
        //                       SampleOrigTaxonID.get(i) + ", " +
         //                       SampleOrigTaxonStatus.get(i) + ", " +
          //                       SampleValidTaxonID.get(i) + ", " +
           //                       SampleValidTaxonStatus.get(i) );


            }
            */

            //Hashtable tmpStratEvents = new Hashtable();
            //int nsamp = SampleDepth.size();
            //System.out.println("nsamp: " + nsamp);
            //System.out.println("SampleID.size() " + SampleID.size() );
            //int evtno = 0;

            // 10-Mar-05:  Getting datums for all distinct valid taxa (in fossil group) in one query:

            buffy = null;
            try {
                String query = "select neptune_datum_def.* " +
                        "from neptune_datum_def inner join " +
                        "(select distinct taxSyn(neptune_sample_taxa.taxon_id) as vtid " +
                        "from neptune_sample_taxa inner join neptune_sample " +
                        "on neptune_sample.sample_id = neptune_sample_taxa.sample_id " +
                        "where neptune_sample.hole_id = '" + hinfo.holeID + "' " +
                        "and neptune_sample.fossil_group = '" + FossilGroup + "' ) as bernie " +
                        "on neptune_datum_def.taxon_id = bernie.vtid " +
                        "where not(datum_age_min_ma is null and datum_age_max_ma is null)";
                buffy = ADPIO.getCSVReaderFromQuery(query);
            } catch (Exception ex) {
                buffy = null;
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Unable to execute datum query: " + ex.getMessage() );
                //return false;
            }

            if (buffy == null) continue;  // go on to next fossil group

            Vector Datums = new Vector();

            try {
                String inLine = buffy.readLine();  // header line
                //System.out.println(inLine);
                if (inLine == null) continue;      // no header line
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println(inLine);
                    Datums.add(new ADPNeptuneDatum(inLine));
                    //System.out.println(inLine);
                }
            } catch (Exception ex) {
                // maybe just try to ignore it and go on (09-Mar-05)
                //dlgBusy.setVisible(false);
                //JOptionPane.showMessageDialog(null,"Problems processing datum query: " + ex.getMessage() );
                //return false;
            }

            if (Datums.size() == 0) continue;   // go on to next fossil group (10-Mar-05)

            int nsamp = SampleDepth.size();

            // candidates for FAD and LAD datums . . .
            String[] FADThings = {"B", "FAD", "BT", "base", "Be"};
            String[] LADThings = {"T", "LAD", "TOP", "top", "Te"};

            // try to extract FAD/LAD datums for each distinct valid taxon (01-Oct-04)
            Enumeration etax = DistinctValidTaxa.elements();
            while (etax.hasMoreElements()) {

                String vtid = (String) etax.nextElement();  // id for current taxon . . .
                //System.out.println("vtid: " + vtid);

                //System.out.println( "got some for taxon " + vtid );

                // try to find minDepth & maxDepth for LAD . . .
                int isamp;
                String minDepth = null;
                String maxDepth = (String) SampleDepth.get(0);
                for (isamp = 0; isamp < nsamp; isamp++) {   // loop down from top until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(maxDepth)) {  // have moved down to next sample
                        minDepth = maxDepth;  // depth of next sample up
                        maxDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp==nsamp ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp < nsamp) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // the datum ID; added 12/15/04 2:48 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < LADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;
                                String dtype = datum.type;
                                //System.out.println( "LADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                // 10-Mar-05:  Adding check for taxon id since Datums now includes all vtid's
                                if (taxid.equals(vtid) & dtype.equals(LADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

                // now try to find minDepth & maxDepth for FAD . . .
                minDepth = (String) SampleDepth.get(nsamp - 1);
                maxDepth = null;
                for (isamp = (nsamp - 1); isamp >= 0; isamp--) {   // loop up from bottom until we encounter vtid
                    String curDepth = (String) SampleDepth.get(isamp);
                    if (!curDepth.equals(minDepth)) {  // have moved up to next sample
                        maxDepth = minDepth;  // depth of next sample down
                        minDepth = curDepth;
                    }
                    if (((String) SampleValidTaxonID.get(isamp)).equals(vtid)) break;
                }
                //if ( isamp<0 ) System.out.println("Taxon " + vtid + " disappeared.");
                if (isamp >= 0) {  // should be in all cases; if not just skip it . . .
                    //System.out.println("vtid, minDepth, maxDepth: " + vtid + ", " + minDepth + ", " + maxDepth );
                    // try to find corresponding datum from query above . . .
                    int datumID = 0;   // added 12/15/04 2:51 PM
                    String evtGroup = null;
                    String evtName = null;
                    String evtLabel = null;
                    String minAge = null;
                    String maxAge = null;
                    boolean found = false;
                    for (int i = 0; i < FADThings.length; i++) {
                        try {
                            Enumeration edat = Datums.elements();
                            while (!found && edat.hasMoreElements()) {
                                ADPNeptuneDatum datum = (ADPNeptuneDatum) edat.nextElement();
                                String taxid = datum.taxon_id;   // added 10-Mar-05
                                String dtype = datum.type;
                                //System.out.println( "FADThings[i], dtype: " + LADThings[i] + ", " + dtype );
                                if (taxid.equals(vtid) & dtype.equals(FADThings[i])) {
                                    //System.out.println( "A match!" );
                                    datumID = datum.id;
                                    evtGroup = datum.fossil_group;
                                    evtName = datum.type + " " + datum.name;
                                    evtLabel = datum.label;
                                    minAge = datum.age_min;
                                    maxAge = datum.age_max;
                                    found = true;
                                }
                            }
                        } catch (Exception ex) {
                            //System.out.println( ex.getMessage() );
                        }
                        if (found) {
                            //System.out.println( "breaking out!" );
                            break;
                        }
                    }
                    if (evtGroup != null) {  // if we managed to find a matching datum, we have a strat event . . .
                        String strMinAge = null;
                        ADPStratEvent se = new ADPStratEvent(evtGroup, evtName, evtLabel, minAge, maxAge, minDepth, maxDepth, datumID);
                        se.setDepthRange(CoreDepths);
                        evtno++;
                        //System.out.println( "Adding event " + evtno );
                        tmpStratEvents.put(new Integer(evtno), se);
                        ta.append(se.toString() + "\n");
                    }
                }

            }

        }

        if (tmpStratEvents.size() < 3) {
            //dlgBusy.hide();
            //dlgBusy = null;
            dlgBusy.setVisible(false);
            JOptionPane.showMessageDialog(null, "Too few datums found.");
            return false;
        }

        // apparently succeeded in reading data . . .
        StratEvents = OrderStratEvents(tmpStratEvents);
        //StratEvents = new Hashtable();
        ADPSymbolPalette.initPalette();
        Enumeration sekeys = StratEvents.keys();
        while (sekeys.hasMoreElements()) {
            Integer sekey = (Integer) sekeys.nextElement();
            ADPStratEvent se = (ADPStratEvent) StratEvents.get(sekey);
            se.setDepthRange(CoreDepths);
            ADPSymbolPalette.addEvent(se.EventGroup);
            //StratEvents.put( sekey, se );
        }
        //dlgBusy.setVisible(false);

        StratFileComment = "Data from Neptune Hole " + hinfo.holeID
                + ", lat: " + hinfo.latitude
                + ", long: " + hinfo.longitude;

        HoleID = hinfo.holeID;   // added 12/8/04 2:36 PM -- to allow saving of LOC for this hole

        //dlgBusy.hide();
        //dlgBusy = null;

        CoreDepths = null;   // forget core depths
        CoreFileComment = "[NONE]";

        // 10-Oct-05: trying to retrieve age model data
        // Turn HoleID to SiteHole, which is hole id without leg
        LOCPoints = null;
        int iu = hinfo.holeID.indexOf('_');
        String SiteHole = hinfo.holeID.substring(iu + 1).trim();  // this is the key in the age model table
        //System.out.println(SiteHole);

        // try to extract age model data . . .
        // 07-Mar-05:  Switching to QDF query
        // 14-Apr-05:  Get max revision number first...
        int rvno = -1;
        buffy = null;
        try {
            String query = "select max(revision_no) from neptune_age_model " +
                    "where site_hole = '" + SiteHole + "' ";
            buffy = ADPIO.getCSVReaderFromQuery(query);
            buffy.readLine();  // header line
            rvno = Integer.parseInt(buffy.readLine());
        } catch (Exception ex) {
            LOCPoints = null;   // presumably nothing there
        }

        // now get age model data if we got a revision number...
        if (rvno > -1) {
            buffy = null;
            try {
                //rs = null;
                //rsmd = null;
                //moreData = false;
                String query = "select age_ma, depth_mbsf from neptune_age_model " +
                        "where neptune_age_model.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno + " " +
                        "order by neptune_age_model.age_ma";
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //System.out.println(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                //moreData = rs.first();
                //if (moreData) LOCPoints = new Vector();
                LOCPoints = new Vector();
                String inLine = buffy.readLine();  // header line, presumably
                while ((inLine = buffy.readLine()) != null) {
                    //System.out.println("age, depth: " + rs.getObject("age_ma").toString() + ", " + rs.getObject("depth_mbsf").toString());
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    double age = Double.parseDouble(ronny.nextToken());
                    double depth = Double.parseDouble(ronny.nextToken());
                    LOCPoints.add(new ADPLOCPoint(age, depth));
                    //moreData = rs.next();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Problem extracting LOC data: " + ex.getMessage());
                LOCPoints = null;
            }
        }

        //initialize LOCPoints to default if no age model found, else try to get history info
        if (LOCPoints == null || LOCPoints.size() == 0) {
            InitLOC();
            LOCFileComment = "[No LOC found in database]";
        } else {
            LOCFileComment = "[No LOC history found in database]";
            try {
                //rs = null;
                //rsmd = null;
                buffy = null;
                String query = "select revision_no, age_quality, date_worked, interpreted_by " +
                        "from neptune_age_model_history " +
                        "where neptune_age_model_history.site_hole = '" + SiteHole + "' " +
                        "and revision_no = " + rvno;
                buffy = ADPIO.getCSVReaderFromQuery(query);
                //Statement stmt = conn.createStatement();
                //rs = stmt.executeQuery(query);
                //rsmd = rs.getMetaData();
                if (buffy != null) {
                    String inLine = buffy.readLine();  // header
                    inLine = buffy.readLine();
                    StringTokenizer ronny = new StringTokenizer(inLine, ",");
                    String rvsn = ronny.nextToken().trim();
                    String qual = ronny.nextToken().trim();
                    if (qual.equals("E")) {
                        qual = "Excellent";
                    } else if (qual.equals("V") || qual.equals("VG")) {
                        qual = "Very Good";
                    } else if (qual.equals("G") || qual.equals("v.2 G") || qual.equals("X")) {
                        qual = "Good";
                    } else if (qual.equals("M")) {
                        qual = "Medium";
                    } else if (qual.equals("P")) {
                        qual = "Poor";
                    } else if (qual.equals("U")) {
                        qual = "Unknown";
                    } else {
                        qual = "Unknown";
                    }
                    String date = ronny.nextToken().trim();
                    String auth = ronny.nextToken().trim();
                    LOCFileComment = "Revision: " + rvsn + ";   Quality: " + qual +
                            ";   Date worked: " + date + "; Interpreted by: " + auth;
                }
            } catch (Exception ex) {
                LOCFileComment = "[No LOC history found in database]";
            }
        }

        adpPP.PlotTitle = "Age-Depth Plot for Neptune Hole " + hinfo.holeID;
        adpPP.XAxisTitle = "Age (Ma)";
        adpPP.YAxisTitle = "Depth (mbsf)";

        //dlgBusy.setTitle("Neptune data retrieved");

        dlgBusy.setVisible(false);
        return true;

    }


    /**
     * orders strat events by MidAge, returning ordered Hashtable; added 12/20/04 3:17 PM
     */
    public Hashtable OrderStratEvents
    (Hashtable
             inStratEvents) {
        int nevt = inStratEvents.size();
        int[] iord = new int[nevt];       // will end up as sorting indices
        double[] age = new double[nevt];  // MidAges . . .
        for (int i = 0; i < nevt; i++) {
            iord[i] = i;
            ADPStratEvent se = (ADPStratEvent) inStratEvents.get(new Integer(i + 1));  // really shouldn' be using Hashtable
            age[i] = se.MidAge;
        }
        int nswap;
        do {
            nswap = 0;
            for (int i = 0; i < (nevt - 1); i++) {
                if (age[iord[i]] < age[iord[i + 1]]) {
                    int itmp = iord[i];
                    iord[i] = iord[i + 1];
                    iord[i + 1] = itmp;
                    nswap++;
                }
            }
        } while (nswap > 0);
        Hashtable outStratEvents = new Hashtable();
        for (int i = 0; i < nevt; i++) {
            ADPStratEvent se = (ADPStratEvent) inStratEvents.get(new Integer(iord[i] + 1));
            outStratEvents.put(new Integer(i + 1), se);
        }
        return outStratEvents;
    }


    private boolean writeLOCtoJanusAmp() {

        String name = null;
        if (jampUser == null) {
            name = JOptionPane.showInputDialog("Enter JanusAMP \n username: ");
            if (name == null || name.equals("")) return false;
            jampUser = name;
        }

        int password = -1;
        if (jampPassword == null) {
//            password = JOptionPane.showInputDialog("JanusAMP \n username: " + jampUser + " \n Enter Password:");

            JPasswordField pwd = new JPasswordField(10);
//            password = JOptionPane.showInputDialog(null, pwd, "Enter Password");
            password = JOptionPane.showConfirmDialog(null, pwd, "Enter Password", JOptionPane.OK_CANCEL_OPTION);
            if (password < 0 || name.equals("")) return false;
            jampPassword = String.valueOf(pwd.getPassword());
        }

//        String rating = JOptionPane.showInputDialog("JanusAMP \n username: " + jampUser + " \n password: ******** \n Please enter rating: ");
        String rating = (String) JOptionPane.showInputDialog(null, "JanusAMP \n username: " + jampUser + " \n password: ******** \n Please enter rating: ", "Rating selection", JOptionPane.QUESTION_MESSAGE, null, new String[]{"very poor", "poor", "average", "good", "very good"}, "poor");
        if (rating == null || jampUser.equals("")) return false;
        String rating2 = "";    // will have single quotes and backslashes escaped
        for (int i = 0; i < rating.length(); i++) {
            String biff = rating.substring(i, i + 1);
            if (biff.equals("'")) {           // a single quote
                rating2 = rating2 + "\\'";          // replace with escaped single quote
            } else if (biff.equals("\\")) {   // a backslash
                rating2 = rating2 + "\\\\";         // replace with an escaped backslash
            } else {
                rating2 = rating2 + biff;           // otherwise just the actual character
            }
        }

        String comment = JOptionPane.showInputDialog("JanusAMP \n username: " + jampUser + " \n password: ******** \n rating: " + rating2 + "\n Please enter comments:");
        if (comment == null) return false;
        String comment2 = "";   // ditto
        for (int i = 0; i < comment.length(); i++) {
            String biff = comment.substring(i, i + 1);
            if (biff.equals("'")) {           // a single quote
                comment2 = comment2 + "\\'";    // replace with escaped single quote
            } else if (biff.equals("\\")) {   // a backslash
                comment2 = comment2 + "\\\\";         // replace with an escaped backslash
            } else {
                comment2 = comment2 + biff;           // otherwise just the actual character
            }
        }


        Writer writer = new StringWriter();

        try {
            String[] siteNameArray = HoleID.split("_");
            String legValue = siteNameArray[0];
            String siteHoleValue = siteNameArray[1];
            String holeValue = siteHoleValue.substring(siteHoleValue.length() - 1);
            String siteValue = siteHoleValue.substring(0, siteHoleValue.length() - 1);


            assert (Integer.valueOf(legValue) instanceof Integer) : "Failure, leg is not an integer";
            assert (Integer.valueOf(siteValue) instanceof Integer) : "Failure, leg is not an integer";
            assert (holeValue instanceof String) : "Falure, hole value is not a string";

            String latlongXML = ADPIO.getJanusLSHLatLong(legValue, siteValue, holeValue);

            InputSource inStream = new InputSource();
            inStream.setCharacterStream(new StringReader(latlongXML));

            XMLInputFactory f = XMLInputFactory.newInstance();
            XMLStreamReader r = f.createXMLStreamReader(inStream.getCharacterStream());
            while (r.hasNext()) {
                //  could also use r.getNamespaceURI() to only look for certain namespaces to begin with
                if ("pos".equals(r.getLocalName())) { // not looking at a namespace level
                    System.out.println("Found a pos: " + r.getElementText());
                }
                r.next();
            }

            //  todo  update all the data in the following XML build to match the correct values for updloading

            XMLOutputFactory xof = XMLOutputFactory.newInstance();
            XMLStreamWriter xtw = null;
            xtw = xof.createXMLStreamWriter(writer);
            xtw.writeStartDocument("utf-8", "1.0");
            xtw.setPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            xtw.writeStartElement("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "RDF");
            xtw.writeNamespace("chronos", "http://www.chronos.org/loc-schema#");
            xtw.writeNamespace("skos", "http://www.w3.org/2004/02/skos/core#");
            xtw.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            xtw.writeNamespace("dc", "http://purl.org/dc/elements/1.1/");
            xtw.writeNamespace("hreview", "http://microformats.org/wiki/hreview");
            xtw.writeNamespace("gml", "http://www.opengis.net/gml");
            xtw.writeNamespace("html", "http://www.w3.org/TR/REC-html40");

            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "loc");

            xtw.writeStartElement("http://purl.org/dc/elements/1.1/", "title");
            xtw.writeCharacters("leg site hole");
            xtw.writeEndElement();

            xtw.writeStartElement("http://purl.org/dc/elements/1.1/", "description");
            xtw.writeCharacters("description data here");
            xtw.writeEndElement();

            xtw.writeStartElement("http://purl.org/dc/elements/1.1/", "creator");
            xtw.writeCharacters("author user");
            xtw.writeEndElement();

            xtw.writeStartElement("http://purl.org/dc/elements/1.1/", "date");
            xtw.writeCharacters(" the date of upload");
            xtw.writeEndElement();

            // chronos:review
            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "review");
            xtw.writeAttribute("type", "xhtml");
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "div");
            xtw.writeAttribute("class", "hreview");
            xtw.writeAttribute("id", "hreviewID");
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "h2");
            xtw.writeAttribute("class", "summary");
            xtw.writeCharacters("1");
            xtw.writeEndElement();
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "abbr");
            xtw.writeAttribute("class", "dtreviewed");
            xtw.writeAttribute("title", "the date again");
            xtw.writeCharacters("the date here");
            xtw.writeEndElement();
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "span");
            xtw.writeAttribute("class", "reviwer vcard");
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "span");
            xtw.writeAttribute("class", "fn");
            xtw.writeCharacters("robotr2");
            xtw.writeEndElement();
            xtw.writeEndElement();
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "span");
            xtw.writeAttribute("class", "type");
            xtw.writeAttribute("style", "display:none");
            xtw.writeCharacters("url");
            xtw.writeEndElement();
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "div");
            xtw.writeAttribute("class", "item");
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "a");
            xtw.writeAttribute("class", "fn url");
            xtw.writeAttribute("href", "http://www.chronos.org/loc/101/1234/A");
            xtw.writeCharacters("leg_siteHole");
            xtw.writeEndElement();
            xtw.writeEndElement();
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "blockquote");
            xtw.writeAttribute("class", "description");
            xtw.writeStartElement("http://www.w3.org/TR/REC-html40", "p");
            xtw.writeAttribute("class", "rating");
            xtw.writeAttribute("title", "0");
            xtw.writeCharacters("&amp;#x2606;&amp;#x2606;&amp;#x2606;&amp;#x2606;&amp;#x2606;");
            xtw.writeEndElement();
            xtw.writeEndElement();
            xtw.writeEndElement();     //  close div
            xtw.writeEndElement();      //  close review


            // todo  put in all the leg site hole values correctly where static test data is now
            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "janusHole");  //  start janusHole
            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "leg");
            xtw.writeCharacters(legValue);
            xtw.writeEndElement();

            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "site");
            xtw.writeCharacters(siteValue);
            xtw.writeEndElement();

            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "hole");
            xtw.writeCharacters(holeValue);
            xtw.writeEndElement();

            xtw.writeStartElement("http://www.opengis.net/gml", "Point");
            xtw.writeStartElement("http://www.opengis.net/gml", "pos");
            xtw.writeCharacters(" lat long values");
            xtw.writeEndElement();
            xtw.writeEndElement();
            xtw.writeEndElement();      //  close janusHole


            xtw.writeStartElement("http://www.chronos.org/loc-schema#", "controlPoints");  //  start controlPoints
            xtw.writeAttribute("tiemscale", "Berggren et al., 1995");
            xtw.writeAttribute("depthUnits", "mbsf");

            int nlp = LOCPoints.size();
            java.text.DecimalFormat df5 = new java.text.DecimalFormat("0.0####");
            for (int i = 0; i < nlp; i++) {
                ADPLOCPoint adp = (ADPLOCPoint) LOCPoints.get(i);
                double age = adp.Age;
                double depth = adp.Depth;
                xtw.writeStartElement("http://www.chronos.org/loc-schema#", "locPoint");
                xtw.writeStartElement("http://www.chronos.org/loc-schema#", "age");
                xtw.writeCharacters(Double.toString(age));
                xtw.writeEndElement();
                xtw.writeStartElement("http://www.chronos.org/loc-schema#", "depth");
                xtw.writeCharacters(Double.toString(depth));
                xtw.writeEndElement();
            }

            xtw.writeEndElement();      //  close controlPoints


            xtw.writeEndElement();
            xtw.writeEndDocument();
            xtw.flush();
            xtw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Exception occurred while running writer samples");
        }

        System.out.println(writer.toString());

        // add in apache httpclient code to call and input XML based data to janusAMP

        HttpClient httpclient = new HttpClient();

        String urlLogin = "http://chronoslab.chronos.org/janusAmp/j_spring_security_check";
        String strURL = "http://chronoslab.chronos.org/janusAmp/utility/getPostXML";

        PostMethod postSec = new PostMethod(urlLogin);

        postSec.setParameter("j_username", jampUser);
        postSec.setParameter("j_password", jampPassword);
//        try {
//            httpclient.executeMethod(postSec);
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }

        PostMethod post = new PostMethod(strURL);
        post.setParameter("dataPackage", "XML DATA GOES HERE");

//        try {
//            int result = httpclient.executeMethod(post);
//            System.out.println("Response status code: " + result);
//            System.out.println("Response body: ");
//            System.out.println(post.getResponseBodyAsString());
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } finally {
//            post.releaseConnection();
//        }

        return true;
    }

    /**
     * Attempts to write LOC to Neptune database.
     *
     * @return true if LOC successfully written, false if not
     */
    private boolean writeLOCtoNeptune
    () {

        String name = JOptionPane.showInputDialog("Please enter your name:");
        if (name == null || name.equals("")) return false;
        String name2 = "";    // will have single quotes and backslashes escaped
        for (int i = 0; i < name.length(); i++) {
            String biff = name.substring(i, i + 1);
            if (biff.equals("'")) {           // a single quote
                name2 = name2 + "\\'";          // replace with escaped single quote
            } else if (biff.equals("\\")) {   // a backslash
                name2 = name2 + "\\\\";         // replace with an escaped backslash
            } else {
                name2 = name2 + biff;           // otherwise just the actual character
            }
        }

        String comment = JOptionPane.showInputDialog("Please enter an age model comment:");
        if (comment == null) return false;
        String comment2 = "";   // ditto
        for (int i = 0; i < comment.length(); i++) {
            String biff = comment.substring(i, i + 1);
            if (biff.equals("'")) {           // a single quote
                comment2 = comment2 + "\\'";    // replace with escaped single quote
            } else if (biff.equals("\\")) {   // a backslash
                comment2 = comment2 + "\\\\";         // replace with an escaped backslash
            } else {
                comment2 = comment2 + biff;           // otherwise just the actual character
            }
        }

        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            String userName = "USER";
            String password = "PASSWORD";
            //conn = DriverManager.getConnection("jdbc:postgresql://cdb0.geol.iastate.edu/neptune",userName,password);
            //switching to test database, 27-Sept-04:
            //switching back to cdb0, 07-Oct-04:
            conn = DriverManager.getConnection("jdbc:postgresql://cdb0.geol.iastate.edu/neptune", userName, password);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to connect to Neptune database: " + ex.getMessage());
            return false;
        }

        try {
            Statement stmt = conn.createStatement();
            String cmd = "Insert into neptune_user_age_model_hdr " +
                    "(hole_id,interpreted_by,age_model_descript) " +
                    "values ('" + HoleID + "','" + name2 + "','" + comment2 + "')";
            //System.out.println( cmd );
            stmt.execute(cmd);
            stmt.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to insert into header table: " + ex.getMessage());
            return false;
        }

        String amid = null;
        try {
            String query = "select currval('age_model_id_seq')";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if (rs.first()) {  // hopefully the one just generated will be last . . .
                amid = rs.getObject(1).toString().trim();
                //System.out.println("age model id: " + amid);
            } else {
                JOptionPane.showMessageDialog(null, "Unable to retrieve age model id.");
                return false;
            }
            stmt.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Unable to retrieve age model id: " + ex.getMessage());
            return false;
        }

        try {
            Statement stmt = conn.createStatement();
            int nlp = LOCPoints.size();
            java.text.DecimalFormat df5 = new java.text.DecimalFormat("0.0####");
            for (int i = 0; i < nlp; i++) {
                ADPLOCPoint adp = (ADPLOCPoint) LOCPoints.get(i);
                double age = adp.Age;
                double depth = adp.Depth;
                int DatumID = 0;   // Datum ID in Neptune database
                int datumIndex = adp.KeyDatumIndex;  // index of strat event in memory
                if (datumIndex > 0) {
                    ADPStratEvent se = (ADPStratEvent) StratEvents.get(new Integer(datumIndex));
                    DatumID = se.DatumID;
                }
                comment = adp.Comment;
                comment2 = "";
                for (int j = 0; j < comment.length(); j++) {
                    String biff = comment.substring(j, j + 1);
                    if (biff.equals("'")) {           // a single quote
                        comment2 = comment2 + "\\'";    // replace with escaped single quote
                    } else if (biff.equals("\\")) {   // a backslash
                        comment2 = comment2 + "\\\\";         // replace with an escaped backslash
                    } else {
                        comment2 = comment2 + biff;           // otherwise just the actual character
                    }
                }

                String cmd = "";
                if (DatumID > 0 && comment2.length() > 0) {
                    cmd = "Insert into neptune_user_age_model " +
                            "(age_model_id,age_point_no,age_ma," +
                            "depth_mbsf,datum_id,age_point_descript) " +
                            "values (" + amid + "," + (i + 1) + "," +
                            df5.format(age) + "," + df5.format(depth) + "," +
                            DatumID + ",'" + comment2 + "')";
                } else if (DatumID == 0 && comment2.length() > 0) {
                    cmd = "Insert into neptune_user_age_model " +
                            "(age_model_id,age_point_no,age_ma," +
                            "depth_mbsf,age_point_descript) " +
                            "values (" + amid + "," + (i + 1) + "," +
                            df5.format(age) + "," + df5.format(depth) +
                            ",'" + comment2 + "')";
                } else if (DatumID > 0 && comment2.length() == 0) {
                    cmd = "Insert into neptune_user_age_model " +
                            "(age_model_id,age_point_no,age_ma," +
                            "depth_mbsf,datum_id) " +
                            "values (" + amid + "," + (i + 1) + "," +
                            df5.format(age) + "," + df5.format(depth) + "," +
                            DatumID + ")";
                } else {
                    cmd = "Insert into neptune_user_age_model " +
                            "(age_model_id,age_point_no,age_ma,depth_mbsf) " +
                            "values (" + amid + "," + (i + 1) + "," +
                            df5.format(age) + "," + df5.format(depth) + ")";
                }
                //System.out.println(cmd);
                stmt.execute(cmd);
            }
            stmt.close();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Insert into age model failed: " + ex.getMessage());
            return false;
        }

        try {
            conn.close();
        } catch (SQLException x) {
            // whatever
        }

        return true;

    }

    // DRF was here
    private boolean ReadJanusData
    () {
        // change 2/18/04:  Now will go ahead and read strat event data even if reading
        //                  of core depths data file cancelled.  In this case, will only
        //                  be able to interpret numerical strat event depth data . . .
        Hashtable tmpCoreDepths = new Hashtable();
        tmpCoreDepths = null;  // let the memory go
        Hashtable tmpStratEvents = new Hashtable();

        Vector tmpLOCPoints = new Vector();

//        if (ADPIO.readJanusStratData(tmpStratEvents) && ADPIO.readJanusLOCData(tmpLOCPoints)) {
        if (ADPIO.readJanusLOCData(tmpLOCPoints) && ADPIO.readJanusStratData(tmpStratEvents)) {
//        if (ADPIO.readJanusStratData(tmpStratEvents)) {
            //  Strat part
            StratEvents = new Hashtable();
            ADPSymbolPalette.initPalette();
            Enumeration sekeys = tmpStratEvents.keys();
            while (sekeys.hasMoreElements()) {
                Integer sekey = (Integer) sekeys.nextElement();
                ADPStratEvent se = (ADPStratEvent) tmpStratEvents.get(sekey);
                se.setDepthRange(CoreDepths);
                ADPSymbolPalette.addEvent(se.EventGroup);
                StratEvents.put(sekey, se);
            }
            tmpStratEvents = null;

//            //  LOC part
            LOCPoints = new Vector();
            Enumeration eloc = tmpLOCPoints.elements();
            while (eloc.hasMoreElements()) {
                System.out.println("transfering the LOC's over");
                LOCPoints.add(eloc.nextElement());
            }

            return true;
        } else {
            tmpStratEvents = null;
            return false;
        }
    }


    /**
     * Reads line-of-correlation data from file into LOCPoints vector
     *
     * @return true if LOC data read successfully, false otherwise
     */
    private boolean ReadJanusLOC() {
        Vector tmpLOCPoints = new Vector();
        if (ADPIO.readJanusLOCData(tmpLOCPoints)) {
            LOCPoints = new Vector();
//            Enumeration eloc = tmpLOCPoints.elements();
            Enumeration eloc = tmpLOCPoints.elements();
            while (eloc.hasMoreElements()) {
                LOCPoints.add(eloc.nextElement());
            }
            return true;
        } else {
            return false;
        }
    }


    /**
     * Reads core depth data into Hashtable CoreDepths (maybe) and strat event data
     * into Hashtable StratEvents.  Core-depth data values are used to interpret strat
     * event depths in core-section;cm format.  If reading of cored-depth data is
     * cancelled, only strat events with depths already in mbsf (meters below sea floor)
     * will be plotted.
     *
     * @return true if strat data successfully read, false if not
     */

    private boolean ReadData
    () {
        // change 2/18/04:  Now will go ahead and read strat event data even if reading
        //                  of core depths data file cancelled.  In this case, will only
        //                  be able to interpret numerical strat event depth data . . .
        Hashtable tmpCoreDepths = new Hashtable();
        // CoreDepths will be null if input file dialog box cancelled or there are
        // problems reading the data . . .
        if (ADPIO.readCoreData(tmpCoreDepths)) {
            CoreDepths = new Hashtable();
            Enumeration cdkeys = tmpCoreDepths.keys();
            while (cdkeys.hasMoreElements()) {
                Integer cdkey = (Integer) cdkeys.nextElement();
                ADPDepthRange cdr = (ADPDepthRange) tmpCoreDepths.get(cdkey);
                CoreDepths.put(cdkey, cdr);
            }
        } else {
            CoreDepths = null;
            CoreFileComment = "[NONE]";
            lblCoreFile.setText("  Core file info:  " + CoreFileComment);
        }
        tmpCoreDepths = null;  // let the memory go
        // StratEvents data will remain unchanged if input file dialog box cancelled
        // or if there are problems reading data . . .
        Hashtable tmpStratEvents = new Hashtable();
        if (ADPIO.readStratData(tmpStratEvents)) {
            StratEvents = new Hashtable();
            ADPSymbolPalette.initPalette();
            Enumeration sekeys = tmpStratEvents.keys();
            while (sekeys.hasMoreElements()) {
                Integer sekey = (Integer) sekeys.nextElement();
                ADPStratEvent se = (ADPStratEvent) tmpStratEvents.get(sekey);
                se.setDepthRange(CoreDepths);
                ADPSymbolPalette.addEvent(se.EventGroup);
                StratEvents.put(sekey, se);
            }
            tmpStratEvents = null;
            return true;
        } else {
            tmpStratEvents = null;
            return false;
        }
    }

    /**
     * Reads line-of-correlation data from file into LOCPoints vector
     *
     * @ return true if LOC data read successfully, false otherwise
     */
    private boolean ReadLOC
    () {
        Vector tmpLOCPoints = new Vector();
        if (ADPIO.readLOCData(tmpLOCPoints)) {
            LOCPoints = new Vector();
            Enumeration eloc = tmpLOCPoints.elements();
            while (eloc.hasMoreElements()) {
                LOCPoints.add(eloc.nextElement());
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Creates initial line-of-correlation connecting two extreme age-depth points
     */

    private void InitLOC
    () {
        double MinDepth = Double.NaN;
        double MaxDepth = Double.NaN;
        double MinAge = Double.NaN;
        double MaxAge = Double.NaN;
        Enumeration senum = StratEvents.elements();
        while (senum.hasMoreElements()) {
            ADPStratEvent se = (ADPStratEvent) senum.nextElement();
            double MidAge = se.MinAge;
            if (!Double.isNaN(se.MaxAge)) MidAge = 0.5 * (se.MinAge + se.MaxAge);
            double MidDepth = se.MinDepth;
            if (!Double.isNaN(se.MaxDepth)) MidDepth = 0.5 * (se.MinDepth + se.MaxDepth);
            if (!Double.isNaN(MidAge)) {
                if (Double.isNaN(MinAge) || MidAge < MinAge) MinAge = MidAge;
                if (Double.isNaN(MaxAge) || MidAge > MaxAge) MaxAge = MidAge;
            }
            if (!Double.isNaN(MidDepth)) {
                if (Double.isNaN(MinDepth) || MidDepth < MinDepth) MinDepth = MidDepth;
                if (Double.isNaN(MaxDepth) || MidDepth > MaxDepth) MaxDepth = MidDepth;
            }
        }
        LOCPoints = new Vector();
        LOCPoints.add(new ADPLOCPoint(MinAge, MinDepth));
        LOCPoints.add(new ADPLOCPoint(MaxAge, MaxDepth));
        LOCFileComment = "[NONE]";
        lblLOCFile.setText("  LOC file info:  " + LOCFileComment);
    }

    /**
     * Computes default axis parameters based on range of age, depth values
     * in data (both core-depth and strat event data)
     *
     * @return the default axis parameters
     */

    public static ADPAxisParameters getDefaultAxisParameters
    () {
        double MinDepth = Double.NaN;
        double MaxDepth = Double.NaN;
        double DepthIncMajor = Double.NaN;
        double DepthIncMinor = Double.NaN;
        double MinAge = Double.NaN;
        double MaxAge = Double.NaN;
        double AgeIncMajor = Double.NaN;
        double AgeIncMinor = Double.NaN;
        if (CoreDepths != null) {
            Enumeration ecd = CoreDepths.elements();
            while (ecd.hasMoreElements()) {
                ADPDepthRange DR = (ADPDepthRange) ecd.nextElement();
                if (!Double.isNaN(DR.MinDepth)) {
                    if (Double.isNaN(MinDepth) || DR.MinDepth < MinDepth) MinDepth = DR.MinDepth;
                    if (Double.isNaN(MaxDepth) || DR.MinDepth > MaxDepth) MaxDepth = DR.MinDepth;
                }
                if (!Double.isNaN(DR.MaxDepth)) {
                    if (Double.isNaN(MinDepth) || DR.MaxDepth < MinDepth) MinDepth = DR.MaxDepth;
                    if (Double.isNaN(MaxDepth) || DR.MaxDepth > MaxDepth) MaxDepth = DR.MaxDepth;
                }
            }
        }
        if (StratEvents != null) {
            Enumeration ecs = StratEvents.elements();
            while (ecs.hasMoreElements()) {
                ADPStratEvent SE = (ADPStratEvent) ecs.nextElement();
                if (!Double.isNaN(SE.MinAge)) {
                    if (Double.isNaN(MinAge) || SE.MinAge < MinAge) MinAge = SE.MinAge;
                    if (Double.isNaN(MaxAge) || SE.MinAge > MaxAge) MaxAge = SE.MinAge;
                }
                if (!Double.isNaN(SE.MaxAge)) {
                    if (Double.isNaN(MinAge) || SE.MaxAge < MinAge) MinAge = SE.MaxAge;
                    if (Double.isNaN(MaxAge) || SE.MaxAge > MaxAge) MaxAge = SE.MaxAge;
                }
                if (!Double.isNaN(SE.MinDepth)) {
                    if (Double.isNaN(MinDepth) || SE.MinDepth < MinDepth) MinDepth = SE.MinDepth;
                    if (Double.isNaN(MaxDepth) || SE.MinDepth > MaxDepth) MaxDepth = SE.MinDepth;
                }
                if (!Double.isNaN(SE.MaxDepth)) {
                    if (Double.isNaN(MinDepth) || SE.MaxDepth < MinDepth) MinDepth = SE.MaxDepth;
                    if (Double.isNaN(MaxDepth) || SE.MaxDepth > MaxDepth) MaxDepth = SE.MaxDepth;
                }
            }
        }
        if (LOCPoints != null) {
            Enumeration eloc = LOCPoints.elements();
            while (eloc.hasMoreElements()) {
                ADPLOCPoint adp = (ADPLOCPoint) eloc.nextElement();
                if (!Double.isNaN(adp.Age)) {
                    if (Double.isNaN(MinAge) || adp.Age < MinAge) MinAge = adp.Age;
                    if (Double.isNaN(MaxAge) || adp.Age > MaxAge) MaxAge = adp.Age;
                }
                if (!Double.isNaN(adp.Depth)) {
                    if (Double.isNaN(MinDepth) || adp.Depth < MinDepth) MinDepth = adp.Depth;
                    if (Double.isNaN(MaxDepth) || adp.Depth > MaxDepth) MaxDepth = adp.Depth;
                }
            }
        }
        //if ( MaxDepth <= 0.0 ) MaxDepth = 1000.0;
        //java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        ////System.out.println( "MinDepth, MaxDepth: " + df2.format(MinDepth) + ", " + df2.format(MaxDepth) );
        if (Double.isNaN(MinDepth) && Double.isNaN(MaxDepth)) {
            MinDepth = 0.0;
            MaxDepth = 1000.0;
            DepthIncMajor = 200;
            DepthIncMinor = 40;
        } else {
            DepthIncMajor = pretty((MaxDepth - MinDepth) / 5.1);
            DepthIncMinor = DepthIncMajor / 5.0;
            MinDepth = DepthIncMinor * (Math.floor(MinDepth / DepthIncMinor) - 1);
            MaxDepth = DepthIncMinor * (Math.ceil(MaxDepth / DepthIncMinor) + 1);
        }

        //if ( MaxAge <= 0.0 ) MaxAge = 20.0;
        //DepthIncM = Math.round( (MaxDepth-MinDepth)/5.0 );
        ////System.out.println( "MinAge, MaxAge: " + df2.format(MinAge) + ", " + df2.format(MaxAge) );
        if (Double.isNaN(MinAge) && Double.isNaN(MaxAge)) {
            MinAge = 0.0;
            MaxAge = 100.0;
            AgeIncMajor = 20.0;
            AgeIncMinor = 4.0;
        } else {
            AgeIncMajor = pretty((MaxAge - MinAge) / 5.1);
            AgeIncMinor = AgeIncMajor / 5.0;
            MinAge = AgeIncMinor * (Math.floor(MinAge / AgeIncMinor) - 1);
            MaxAge = AgeIncMinor * (Math.ceil(MaxAge / AgeIncMinor) + 1);
        }
        ////System.out.println( "Plot limits: " + df2.format(MinAge) + " Ma to " + df2.format(MaxAge) + " Ma, " + df2.format(MinDepth) + " mbsf to " + df2.format(MaxDepth) + " mbsf" );
        return (new ADPAxisParameters(MinDepth, MaxDepth, DepthIncMajor, DepthIncMinor,
                MinAge, MaxAge, AgeIncMajor, AgeIncMinor));
    }

    /**
     * Rounds x to the nearest "pretty" number (1, 2.5 or 5 times the appropriate power of 10)
     *
     * @return the pretty number
     */

    public static double pretty
    (
            double x) {  // returns "pretty" version of x: nearest 1-ish, 2.5-ish, or 5-ish thing
        int ix = (int) (Math.log(Math.abs(x)) / Math.log(10.0));
        double mag = Math.pow(10.0, (double) ix);  // 1, 10, 100, . . .
        double biff = x / mag;  // should be between 1 and 10
        double xout;
        if (biff < 2.0) {
            xout = 1.0;
        } else if (biff < 3.7) {
            xout = 2.5;
        } else {
            xout = 5.0;
        }
        //java.text.DecimalFormat df2 = new java.text.DecimalFormat("0.00");
        ////System.out.println( "x, ix, mag, biff, xout: " + df2.format(x) + ", " + ix + ", " +df2.format(mag) + ", " + df2.format(biff) + ", " + df2.format(xout) );
        return mag * xout;
    }

    /**
     * Makes it go
     *
     * @param args the command line arguments
     */
    public static void main
    (String[] args) {
        /*
        BufferedReader buffy = null;
        String inLine = "";
         
        HoleIDs = new Vector();
        HoleFiles = new Vector();
         
        HoleIDs.add( "[NONE]" );
        HoleID = "[NONE]";
        HoleFiles.add( new ADPHoleInfo("[NONE]","","","") );
         
        try {
            buffy = new BufferedReader( new FileReader("D:\\CHRONOS\\ADP\\HoleInfo.dat") );
            inLine = buffy.readLine();  // skip first two lines
            inLine = buffy.readLine();
            while ( (inLine = buffy.readLine()) != null ) {
                StringTokenizer biff = new StringTokenizer( inLine, "," );
                ADPHoleInfo adpHI = new ADPHoleInfo();
                adpHI.HoleID = biff.nextToken();
                adpHI.CoreFile= biff.nextToken();
                adpHI.StratFile = biff.nextToken();
                adpHI.LOCFile = biff.nextToken();
                HoleIDs.add( adpHI.HoleID );
                HoleFiles.add( adpHI );
            }
            buffy.close();
        }
        catch (Exception x) {
            ////System.out.println(x.getMessage());
        }
         */

        final ADPApp app = new ADPApp();
        app.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent e) {
                        System.exit(0);
                    }
                }
        );

    }
}

