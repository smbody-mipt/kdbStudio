package studio.kdb;

import javax.swing.table.AbstractTableModel;

public abstract class KTableModel extends AbstractTableModel {
    public abstract boolean isKey(int column);

    public abstract K.KBaseVector getColumn(int col);
    protected int[] sortIndex = null;
    protected int sorted = 0;
    protected int sortedByColumn = -1;

    public void asc(int col) {
        sortIndex = null;
        K.KBaseVector v = getColumn(col);
        sortIndex = v.gradeUp();
        sorted = 1;
        sortedByColumn = col;
    }

    public void desc(int col) {
        sortIndex = null;
        K.KBaseVector v = getColumn(col);
        sortIndex = v.gradeDown();
        sorted = -1;
        sortedByColumn = col;
    }

    public int getSortByColumn() {
        return sortedByColumn;
    }

    public boolean isSortedAsc() {
        return sorted == 1;
    }

    public boolean isSortedDesc() {
        return sorted == -1;
    }

    public void removeSort() {
        sortIndex = null;
        sorted = 0;
        sortedByColumn = -1;
    }

    public Class getColumnClass(int col) {
        return getColumn(col).getClass();
    }

    public Object getValueAt(int row,int col) {
        row = (sortIndex == null) ? row : sortIndex[row];
        K.KBaseVector v = getColumn(col);
        return v.at(row);
    }

    public int getRowCount() {
        return getColumn(0).getLength();
    }

}
