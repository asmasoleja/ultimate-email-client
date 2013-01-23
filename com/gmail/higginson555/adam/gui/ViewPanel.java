/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import com.gmail.higginson555.adam.view.EmailFilterer;
import com.gmail.higginson555.adam.view.View;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam
 */
public class ViewPanel extends javax.swing.JPanel {

    //The view this panel represents
    private View view;
    
    /**
     * Creates new form ViewPanel
     */
    public ViewPanel(View view) {
        this.view = view;
        this.setName(view.getViewName());
        initComponents();
        EmailFilterer filterer = EmailFilterer.getInstance(view.getAccount());
        try
        {
            ArrayList<Object[]> filterData = filterer.getTableData(view);
            Object[][] newTableData = new Object[filterData.size()][4]; 
            EmailTableModel model = (EmailTableModel) messageTable.getModel();
            int row = 0;
            for (Object[] line : filterData)
            {
                Object[] tableLine = {line[2], line[3], line[6], Boolean.TRUE};
                newTableData[row] = tableLine;
                row++;
            }
            
            model.setData(newTableData);
        } 
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "SQLException", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewNameLabel = new javax.swing.JLabel();
        messagesLabel = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageTable = new javax.swing.JTable();

        viewNameLabel.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        viewNameLabel.setText(view.getViewName());

        messagesLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        messagesLabel.setText("Messages");

        messageTable.setModel(new EmailTableModel());
        jScrollPane1.setViewportView(messageTable);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 717, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewNameLabel)
                            .addComponent(messagesLabel))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(viewNameLabel)
                .addGap(4, 4, 4)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(4, 4, 4)
                .addComponent(messagesLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 497, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable messageTable;
    private javax.swing.JLabel messagesLabel;
    private javax.swing.JLabel viewNameLabel;
    // End of variables declaration//GEN-END:variables
}
