/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Adam
 */
public class EmailTableCellRenderer extends DefaultTableCellRenderer
{
    private ArrayList<Object[]> tableData;

    public EmailTableCellRenderer()
    {
        this.tableData = new ArrayList<Object[]>();
    }
    /**
     * Creates the EmailTableCellRenderer
     * @param tableData The full data for each e-mail in the table,
     *                  i.e all the data held about an email in a table
     *                  for each e-mail displayed by this table
     */
    public EmailTableCellRenderer(ArrayList<Object[]> tableData)
    {
        this.tableData = tableData;
    }
    
    public void addTableDataEntry(Object[] entry)
    {
        tableData.add(entry);
    }
    
    public void updateTableDataEntry(int row, Object[] newData)
    {
        tableData.remove(row);
        tableData.add(row, newData);
    }

    public void setTableData(ArrayList<Object[]> tableData)
    {
        this.tableData = tableData;
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 boolean hasFocus,
                                                 int row,
                                                 int column)
    {
        Component c = super.getTableCellRendererComponent(table, value, 
                                    isSelected, hasFocus, row, column);

        if (tableData != null && !tableData.isEmpty() && tableData.get(row) != null)
        {
            boolean isRead = (Boolean) tableData.get(row)[12];
            if (!isRead)
            {
                c.setFont(c.getFont().deriveFont(Font.BOLD));
            }
            
            boolean isValidMessage = (Boolean) tableData.get(row)[11];
            if (!isValidMessage)
            {
                c.setForeground(Color.red);
            }
        }
        
        //TODO
        //System.out.println("Table data: " + tableData.get(row)[11]);
        //boolean isValidMessage = (Boolean) tableData.get(row)[11];
        /*if (!isValidMessage)
        {
            c.setForeground(Color.red);
        }*/
        
        return c;       
    }
    
}
