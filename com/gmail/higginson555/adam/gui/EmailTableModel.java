/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Adam
 */
public class EmailTableModel extends AbstractTableModel
{
    private String[] columnNames = {"Subject", "From", "Date", "Read"};
    private Object[][] data = {{"", "", "", Boolean.FALSE}};

    public void setRowCount(int count)
    {
        data = new Object[count][4];
        fireTableDataChanged();
    }

    @Override
    public int getColumnCount()
    {
        return columnNames.length;
    }

    @Override
    public int getRowCount()
    {
        return data.length;
    }

    @Override
    public String getColumnName(int col)
    {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col)
    {
        return data[row][col];
    }

    @Override
    public Class getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }

    @Override
    public boolean isCellEditable(int row, int col)
    {
        return false;
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }

    public void insertRow(Object[] rowData, int row)
    {
        System.arraycopy(rowData, 0, data[row], 0, getColumnCount());
        fireTableRowsInserted(row, row);
        System.out.println("Inserted row @ " + row + " with data: " + data[row][0]);
    }

    public void deleteRows(int[] rows)
    {
        ArrayList<Object[]> allRows = new ArrayList<Object[]>();

        for (int i = 0; i < getRowCount(); i++)
        {
            Object[] row = new Object[getColumnCount()];
            System.arraycopy(data[i], 0, row, 0, getColumnCount());

            allRows.add(row);               
        }

        for (int i = rows.length - 1; i >= 0; i--)
        {
            allRows.remove(rows[i]);
        }

        Object[][] newData = new Object[allRows.size()][getColumnCount()];
        for (int i = 0; i < allRows.size(); i++)
        {
            Object[] row = allRows.get(i);
            System.arraycopy(row, 0, newData[i], 0, getColumnCount());
        }

        data = newData;
        fireTableDataChanged();


    }

    public void deleteRow(int row)
    {
        Object[][] newData = new Object[getRowCount() - 1][getColumnCount()];

        System.out.println("Row: " + row);
        for (int i = 0; i < row; i++)
        {
            System.out.println("i != row");
            for (int j = 0; j < getColumnCount(); j++)
            {
                //System.out.println("j: " + data[i][j]);
                newData[i][j] = data[i][j];
            }
        }

        data = newData;
        fireTableDataChanged();
    }

    public void setData(Object[][] newData)
    {
        data = newData;
        fireTableDataChanged();
    }
}
