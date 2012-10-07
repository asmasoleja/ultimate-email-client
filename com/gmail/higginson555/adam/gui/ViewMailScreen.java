/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam
 */
public class ViewMailScreen extends javax.swing.JFrame {

    //The attatchment number, used for saving attatchments without
    //a filename
    private static int attatchmentNo = 1;
    
    //The message object which holds the sent e-mail
    private Message message;
    //The level we're currently in on reading the message
    private int level;
    
    
    /**
     * Creates new form ViewMailScreen
     * @param message The message object which represents the
     *                received e-mail
     */
    public ViewMailScreen(Message message) 
    {
        this.message = message;
        this.level = 0;
        initComponents();
        try 
        {
            writeMessage(message);
        } 
        catch (Exception ex) 
        {
            JOptionPane.showConfirmDialog(rootPane, "Error! Could not show message\n" + ex.getMessage(), "Error!", JOptionPane.OK_OPTION);
        }
    }
    
    private void writeMessage(Part part) throws Exception 
    {
        if (part instanceof Message)
            writeEnvelope((Message) part);
            
        String filename = part.getFileName();
        
        //We use isMimeType to determine the content type, avoiding
        //having to get the content data until we need it
        if (part.isMimeType("text/plain"))
        {
            bodyTextPane.setContentType("text/plain");
            bodyTextPane.setText((String)part.getContent());
        }
        if (part.isMimeType("text/html"))
        {
            bodyTextPane.setContentType("text/html");
            bodyTextPane.setText((String)part.getContent());
        }
        else if (part.isMimeType("multipart/*"))
        {
            Multipart multipart = (Multipart)part.getContent();
            level++; //Increment the level...
            int count = multipart.getCount();
            for (int i = 0; i < count; ++i)
                writeMessage(multipart.getBodyPart(i));
            level--; //We're out, so decrement the level
        }
        else if (part.isMimeType("message/rfc822"))
        {
            level++; //Again increment as we're going 'deeper' into the message
            writeMessage((Part)part.getContent());
            level--; //Decrement level
        }
        else
        {
            //TODO Save attatchments here
            if (level != 0 && part instanceof MimeBodyPart && !part.isMimeType("multipart/*"))
            {
                String disposition = part.getDisposition();
                //Some mailers don't include a content-disposition
                if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT))
                {
                    if (filename == null)
                        filename = "Attachment" + attatchmentNo++;
                    System.out.println("Saving attatchment to file: " + filename);
                    
                    try
                    {
                        File file = new File(filename);
                        if (file.exists())
                        {
                            //TODO What to do if file exists?
                            throw new IOException("file already exists!");
                           
                        }
                        ((MimeBodyPart)part).saveFile(file);
                    }
                    catch (IOException ex)
                    {
                        System.err.println("Failed to save attachment!" + ex);
                        JOptionPane.showConfirmDialog(rootPane, "Error! Could not save attatchment!\n" + ex.getMessage(), "Error!", JOptionPane.OK_OPTION);
                    }
                }
            }
        }
    }
    
    /*
     * Writes the details of the envelope to the screen
     */
    private void writeEnvelope(Message message) throws Exception
    {
        //The addresses of the message
        Address[] addresses;
        
        //From
        addresses = message.getFrom();
        String fromText = "";
        if (addresses != null)
        {
            for (int i = 0; i < addresses.length; i++)
                fromText += addresses[i].toString() + " ";
        }
        fromLabel.setText(fromText);
        
        //Reply to
        addresses = message.getReplyTo();
        String replyToText = "";
        if (addresses != null)
        {
            for (int i = 0; i < addresses.length; i++)
                replyToText += addresses[i].toString() + " ";
        }
        replyToLabel.setText(replyToText);
        
        //To
        addresses = message.getRecipients(Message.RecipientType.TO);
        String toText = "";
        if (addresses != null)
        {
            for (int i = 0; i < addresses.length; i++)
            {
                toText += addresses[i].toString() + " ";
                InternetAddress ia = (InternetAddress)addresses[i];
                if (ia.isGroup())
                {
                    InternetAddress[] allAddresses = ia.getGroup(false);
                    for (int j = 0; j < allAddresses.length; j++)
                        toText += "    Group: " + allAddresses[j].toString();
                }
            }
        }
        toLabel.setText(toText);
        
        subjectLabel.setText(message.getSubject());
        
        Date date = message.getSentDate();
        if (date != null)
            sentLabel.setText(date.toString());
        else
            sentLabel.setText("Unknown");
        
        //TODO FLAGS?
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fromLabelStatic = new javax.swing.JLabel();
        toLabelStatic = new javax.swing.JLabel();
        sentLabelStatic = new javax.swing.JLabel();
        fromLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        bodyTextPane = new javax.swing.JTextPane();
        subjectLabelStatic = new javax.swing.JLabel();
        subjectLabel = new javax.swing.JLabel();
        replyButton = new javax.swing.JButton();
        forwardButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        sentLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        replyToLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        fromLabelStatic.setText("From:");

        toLabelStatic.setText("To:");

        sentLabelStatic.setText("Sent:");

        String messageFrom = null;

        try
        {
            Address[] addresses;
            if ((addresses = message.getFrom()) != null)
            {
                for (int i = 0; i < addresses.length; i++)
                messageFrom += addresses[i].toString();
            }
        }
        catch (MessagingException ex)
        {
            JOptionPane.showConfirmDialog(rootPane, "Could not get from field!", "Error!", JOptionPane.OK_OPTION);
            ex.printStackTrace();
        }

        if (messageFrom != null)
        {
            fromLabel.setText(messageFrom);
        }
        else
        fromLabel.setText("???");

        toLabel.setText("To Label");

        bodyTextPane.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        bodyTextPane.setEnabled(false);
        jScrollPane1.setViewportView(bodyTextPane);

        subjectLabelStatic.setText("Subject:");

        String subject = null;

        try { subject = message.getSubject(); }
        catch (MessagingException ex)
        {
            JOptionPane.showConfirmDialog(rootPane, "Could not get subject field!", "Error!", JOptionPane.OK_OPTION);
            ex.printStackTrace();
        }

        if (subject != null)
        subjectLabel.setText(subject);
        else
        subjectLabel.setText("???");

        replyButton.setText("Reply");

        forwardButton.setText("Forward");

        deleteButton.setText("Delete");

        sentLabel.setText("Sent Label");

        jLabel1.setText("Reply To:");

        replyToLabel.setText("Reply To");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(sentLabelStatic)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sentLabel)
                        .addGap(99, 99, 99))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(subjectLabelStatic)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(subjectLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(replyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(forwardButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(toLabelStatic, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(fromLabelStatic))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fromLabel)
                                    .addComponent(replyToLabel)
                                    .addComponent(toLabel))))
                        .addGap(0, 571, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(replyButton)
                    .addComponent(forwardButton)
                    .addComponent(deleteButton))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sentLabelStatic)
                            .addComponent(sentLabel)))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fromLabelStatic)
                            .addComponent(fromLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(replyToLabel))))
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(toLabel)
                    .addComponent(toLabelStatic, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(subjectLabel)
                    .addComponent(subjectLabelStatic))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(ViewMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ViewMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ViewMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ViewMailScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                //new ViewMailScreen().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane bodyTextPane;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton forwardButton;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel fromLabelStatic;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton replyButton;
    private javax.swing.JLabel replyToLabel;
    private javax.swing.JLabel sentLabel;
    private javax.swing.JLabel sentLabelStatic;
    private javax.swing.JLabel subjectLabel;
    private javax.swing.JLabel subjectLabelStatic;
    private javax.swing.JLabel toLabel;
    private javax.swing.JLabel toLabelStatic;
    // End of variables declaration//GEN-END:variables
}
