/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import com.gmail.higginson555.adam.AccountMessageDownloader;
import com.gmail.higginson555.adam.view.EmailFilterer;
import com.gmail.higginson555.adam.view.View;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam
 */
public class ViewPanel extends javax.swing.JPanel {

    //The view this panel represents
    private View view;
    //All the data this view holds after being filtered
    private ArrayList<Object[]> filterData;
    
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
            filterData = filterer.getTableData(view);
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
        messageTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                messageTableMouseClicked(evt);
            }
        });
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

    private void messageTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_messageTableMouseClicked
        //Double click
        if (evt.getClickCount() == 2)
        {
            //Get selected row
            int index = messageTable.getSelectedRow();
            //Get the data from this selected row
            Object[] line = filterData.get(index);
            //Get the message UID and folder ID
            int folderID = (Integer) line[7];
            int messageUID = (Integer) line[1];
            try 
            {
                //Find message with this data
                Message foundMessage = AccountMessageDownloader.getInstance(view.getAccount()).getMessageWithID(folderID, messageUID);
                System.out.println("Found message titled: " + foundMessage.getSubject());
                ViewMailScreen vms = new ViewMailScreen(foundMessage, view.getAccount());
                vms.setVisible(true);
            } 
            catch (Exception ex)
            {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, ex.toString(), ex.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_messageTableMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JTable messageTable;
    private javax.swing.JLabel messagesLabel;
    private javax.swing.JLabel viewNameLabel;
    // End of variables declaration//GEN-END:variables
}
