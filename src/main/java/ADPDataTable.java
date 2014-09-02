/*
 * ADPDataTable.java  
 *
 * Created 12/13/04 2:41 PM
 *    -- copied from CDDataTable
 */

/**
 * General-purpose data table . . . extended by other classes for particular data types
 *
 * @version 17-Dec-04
 * @author gcb
 */

import javax.swing.table.*;
import java.util.*;

public class ADPDataTable extends AbstractTableModel {
    
    private String[] columnNames;
    private Object[][] data;
   
    /** 
     * Creates a new instance of CDDataTable
     *
     * @param vec Vector of data objects . . . must implement toVector
     */
    public ADPDataTable( Vector vec ) {
        ADPDataType obj0 = (ADPDataType) vec.get(0);
        columnNames = obj0.fieldNames();
        int nvar = columnNames.length;
        int ndat = vec.size();
        data = new Object[ndat][nvar];
        for ( int i=0; i<ndat; i++ ) {
            ADPDataType dat = (ADPDataType) vec.get(i);
            data[i] = dat.toVector().toArray();
        }
    }

    public ADPDataTable( Hashtable hash ) {
        Enumeration keys = hash.keys();
        Integer key0 = (Integer) keys.nextElement();
        ADPDataType obj0 = (ADPDataType) hash.get(key0);
        columnNames = obj0.fieldNames();
        int nvar = columnNames.length;
        int ndat = hash.size();
        data = new Object[ndat][nvar];
        keys = hash.keys();
        int i = 0;
        while ( keys.hasMoreElements() ) {
            Integer key = (Integer) keys.nextElement();
            ADPDataType dat = (ADPDataType) hash.get(key);
            data[i] = dat.toVector().toArray();
            i++;
        }
    }

    
    public int getColumnCount() {
        return columnNames.length;
    }

    public int getRowCount() {
        return data.length;
    }

    public String getColumnName(int col) {
        return columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return data[row][col];
    }

    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * @return whether cell is editable
     */
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
        return false;
    }

    /*
     * Sets value for given cell
     * @param value new value for cell (as Object)
     * @param row row number
     * @param col column number
     */
    public void setValueAt(Object value, int row, int col) {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }
       
}
