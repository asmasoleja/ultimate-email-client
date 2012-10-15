/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import com.gmail.higginson555.adam.ProtectedPassword;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message.RecipientType;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam
 */
public class ComposeMailScreen extends javax.swing.JFrame {

    private String username, password;
    //The properties to use for this message
    private Properties properties;
    //The reply message, can be null if we should just compose a blank
    //message.
    private Message replyMessage;
    /**
     * 
     * @param config The Properties holding a username, password etc.
     * @param properties Properties holding server settings and configuration
     */
    public ComposeMailScreen(Properties config, 
                             Properties serverProperties) 
    {
        this.username = config.getProperty("username");
        //The password which will be decrypted
        String passwordDec = config.getProperty("password");
        try 
        {
            passwordDec = ProtectedPassword.decrypt(passwordDec);
        } 
        catch (GeneralSecurityException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "GeneralSecurityException", JOptionPane.ERROR_MESSAGE);
        } 
        catch (IOException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "GeneralSecurityException", JOptionPane.ERROR_MESSAGE);
        }
        
        this.password = passwordDec;
        this.properties = serverProperties;
        initComponents();
        this.setLocationRelativeTo(null);
    }
    
    public ComposeMailScreen(Properties config, Properties serverProperties,
                             Message replyMessage)
    {
        this(config, serverProperties);
        this.replyMessage = replyMessage;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        toField = new javax.swing.JTextField();
        subjectField = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        messageArea = new javax.swing.JTextArea();
        sendButton = new javax.swing.JButton();
        discardButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Compose Mail");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("To:");

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel2.setText("Subject:");

        messageArea.setColumns(20);
        messageArea.setRows(5);
        jScrollPane1.setViewportView(messageArea);

        sendButton.setText("Send");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        discardButton.setText("Discard");
        discardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                discardButtonActionPerformed(evt);
            }
        });

        saveButton.setText("Save");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(13, 13, 13)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subjectField)
                            .addComponent(toField)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(sendButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(saveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discardButton)
                        .addGap(0, 476, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sendButton)
                    .addComponent(saveButton)
                    .addComponent(discardButton))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(toField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(subjectField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 370, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void discardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_discardButtonActionPerformed
        //Create a new option pane to check if it's ok to close
        int result = JOptionPane.showConfirmDialog(rootPane, 
                "Are you sure you wish to discard this message?", 
                "Discard", 
                JOptionPane.YES_NO_OPTION);
        
        if (result == 0)
            this.dispose();
    }//GEN-LAST:event_discardButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        try
        {
            //TODO What if not using gmail SMTP?
            //For TLS, which gmail requires
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            };
            
            Session session = Session.getInstance(this.properties, authenticator);       
            Message message = new MimeMessage(session);
            //Set FROM and TO fields
            message.setFrom(new InternetAddress(this.username));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(this.toField.getText(), false));
            message.setSubject(this.subjectField.getText());
            message.setText(this.messageArea.getText());
            
            System.out.println("Trying to send from: " + this.username);
            
            Transport.send(message);
            System.out.println("Sent message from: " + this.username + " to: "+ this.toField.getText());
            
            this.dispose();
        }
        catch (AddressException addressEx)
        {
            JOptionPane.showConfirmDialog(rootPane, "Error!", 
                    "Could not parse TO field\n" + addressEx.getMessage(), 
                    JOptionPane.OK_OPTION);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_sendButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ComposeMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ComposeMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ComposeMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ComposeMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                //new ComposeMailScreen().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton discardButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea messageArea;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton sendButton;
    private javax.swing.JTextField subjectField;
    private javax.swing.JTextField toField;
    // End of variables declaration//GEN-END:variables
}
