package rover;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * Created by suegy on 10/08/15.
 */
public class RoverTableModel extends AbstractTableModel {

    private String[] columnNames = {
            "Rover", "X", "Y", "Task", "% Complete", "Carrying", "Power", "Max Speed", "Max Range", "Max Load" };

    private ArrayList<Object[]> rowData = new ArrayList<Object[]>();

    public String getColumnName(int col) {
        return columnNames[col].toString();
    }

    public int getRowCount() { return rowData.size(); }
    public int getColumnCount() { return columnNames.length; }

    public Object getValueAt(int row, int col) {
        return rowData.get(row)[col];
    }

    public boolean isCellEditable(int row, int col)
    { return false; }

    public void setValueAt(Object value, int row, int col) {
        rowData.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }

    public ArrayList<Object[]> getRowData() {
        return rowData;

    }
}