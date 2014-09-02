/*
 * ADPLOCDataTable.java  
 *
 * Created 12/15/04 12:37 PM
 *    -- Copied from ADPStratEventTable
 * 12/22/04 3:58 PM:  adding TableModelListener . . . 
 */

/**
 * Table holding LOC data
 *
 * @version 22-Dec-2004
 * @author gcb
 */

//import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class ADPLOCDataTable extends ADPDataTable implements TableModelListener {
       
    /** Creates a new instance of ADPStratTable */
    public ADPLOCDataTable( Vector LOCPoints ) {
        super( LOCPoints );
        addTableModelListener(this);
    }
    
    public boolean isCellEditable( int row, int col ) {
        if ( col < 2 ) {
            return false;
        } else {
            return true;
        }
    }

    public void tableChanged(TableModelEvent e) {
        int row = e.getFirstRow();
        if ( e.getType() != TableModelEvent.UPDATE ) return;
        String datumLabel = getValueAt(row,2).toString();
        String comment = getValueAt(row,3).toString();
        ADPLOCPoint lp = (ADPLOCPoint) ADPApp.LOCPoints.get(row);
        lp.setKeyDatum(datumLabel);
        lp.setComment(comment);
        ADPApp.LOCPoints.set(row,lp);
    }
            
}
