/*
 * ADPDataSheet.java
 *
 * Created 12/13/04 3:11 PM
 *   -- Copied from CDDataSheet
 */

/**
 * A data sheet (JTable in a JFrame) for ADP
 *
 * @version 13-Dec-2004
 * @author gcb
 */
import java.awt.*;
//import java.awt.event.*;
import javax.swing.*;
import java.util.*;

import javax.swing.table.*;

public class ADPDataSheet extends JFrame {
    
    /** The JTable containing the data */
    private JTable jtd;
    /** The name of this data sheet */
    private String moi;  // added 19-Apr-04
    
    /** Creates a new instance of ADPDataSheet */
    public ADPDataSheet(String sheetName, TableModel leTable) {
        super( sheetName );
        moi = sheetName;
        Container c = getContentPane();
        c.setLayout( new BorderLayout() );
        jtd = new JTable( leTable );
        jtd.setColumnSelectionAllowed( true );
        jtd.setRowSelectionAllowed( false );
        c.add( new JScrollPane(jtd), BorderLayout.CENTER );
        setSize(500,200);
    }
    
    /**
     * @return the name of the data sheet
     */
    public String getName() {
        return moi;
    }
    
    /**
     * @return array of variable (column) names
     */
    public String[] getVariableNames() {
        String[] colName = new String[jtd.getColumnCount()];
        for (int j=0; j<jtd.getColumnCount(); j++) {
            colName[j] = jtd.getColumnName(j);
        }
        return colName;
    }
          
    /** 
     * @return the vector of selected column heads (null if none)
     */
    public Vector getSelectedColumnHeads() {
        Vector selColHeads = null;
        if ( jtd.getSelectedColumnCount() == 0 ) return selColHeads;
        int[] selCols = jtd.getSelectedColumns();
        selColHeads = new Vector();
        for (int j=0; j<selCols.length; j++) {
            selColHeads.addElement( jtd.getColumnName(selCols[j]) );
        }
        return selColHeads;
    }
    
    /**
     * @returns Vector of Vectors of selected columns of data
     */
    public Vector getSelectedData() {
        Vector colData = null;
        if ( jtd.getSelectedColumnCount() == 0 ) return colData;
        int[] selCols = jtd.getSelectedColumns();
        colData = new Vector();
        for (int j=0; j<selCols.length; j++ ) {
            Vector col = new Vector();
            for (int i=0; i<jtd.getRowCount(); i++) {
                col.addElement( jtd.getValueAt(i,selCols[j]).toString() );
            }
            colData.addElement( col );
        }
        return colData;
    }
    
}
