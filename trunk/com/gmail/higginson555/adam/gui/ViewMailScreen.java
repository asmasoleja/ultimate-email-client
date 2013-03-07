/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.TagParser;
import com.gmail.higginson555.adam.UserDatabase;
import com.gmail.higginson555.adam.view.View;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.jsoup.Jsoup;
/*import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.test.SimpleHtmlRendererContext;
import org.lobobrowser.html.test.SimpleUserAgentContext;/*

/**
 *
 * @author Adam
 */
public class ViewMailScreen extends JPanel
{

    //The attatchment number, used for saving attatchments without
    //a filename
    private static int attatchmentNo = 1;
    
    //The message object which holds the sent e-mail
    private Message message;
    //The ID of the message in the database
    private int messageID;
    //The level we're currently in on reading the message
    private int level;
    //The level of the attachment list we're at
    private int listLevel;
    //The ArrayList of all attachments of the message
    private ArrayList<Part> attachments;
    //The part of the message which holds the body
    private Part messageBody;
    //The string body representation
    private String body;
    //The reply to String, used for the Reply to message functionality
    private String replyTo;
    //The recipient string
    private String recipient;
    //Subject
    private String subject;

    
    
    
   /**
     * Creates new form ViewMailScreen
     * @param message The message object which represents the
     *                received e-mail
     */
    public ViewMailScreen(Message message, Account account, int messageID, boolean extractTags)
    {
        this.messageID = messageID;
        this.message = message;
        this.level = 0;
        
        this.attachments = new ArrayList<Part>();
        
        body = "";
        initComponents();
        
        final JWebBrowser browser = new JWebBrowser();
        browser.setBarsVisible(false);
        browserPanel.add(browser, BorderLayout.CENTER);
 
        
        
        /*UserAgentContext ucontext = new SimpleUserAgentContext();
        SimpleHtmlRendererContext rContext = new SimpleHtmlRendererContext(panel, ucontext);
        DocumentBuilderImpl dbi = new DocumentBuilderImpl(ucontext, rContext);*/
                

        //bodyTextPane.getDocument().putProperty("IgnoreCharsetDirective", Boolean.TRUE);
        try 
        {
            System.out.println("\nWriting message...\n");
            writeMessage(message);
            //panel.setHtml(body, body, rContext);
            browser.setHTMLContent(body);
            //Remove HTML from the string, for inserting tags into the database
            String withoutHTML = Jsoup.parse(body).text();
            //Get tags and insert into local user database
            System.out.println("Should extract tags?" + extractTags);
            if (extractTags)
            {
                System.out.println("Inserting tags!");
                ArrayList<String> tags = TagParser.getInstance().getTags(withoutHTML);
                TagParser.getInstance().insertTags(UserDatabase.getInstance(), tags, messageID);
            }
            System.out.println("\n---------------DONE-------------------\n");            
            //bodyTextPane.setText(body);
        } 
        catch (Exception ex) 
        {
            JOptionPane.showConfirmDialog(null, "Error! Could not show message\n" + ex.getMessage(), "Error!", JOptionPane.OK_OPTION);
        }

        this.setName(subject);
    }
    
    /*
     * Writes message to the message area
     */
    private void writeMessage(Part part) throws Exception 
    {
        //Write the envelope if the message
        if (part instanceof Message) {
            writeEnvelope((Message) part);
        }
            
        String filename = part.getFileName();
        
        System.out.println("Content type: " + part.getContentType());
        
        //We use isMimeType to determine the content type, avoiding
        //having to get the content data until we need it
        if (part.isMimeType("text/plain") || part.isMimeType("TEXT/PLAIN"))
        {

            //bodyTextPane.setContentType("text/plain");
            System.out.println("Found plain");
            //System.out.println("\nPlain Section:\n" + part.getContent());
            //body += (String)part.getContent();
        }
        else if (part.isMimeType("text/html") || part.isMimeType("TEXT/HTML"))
        {
            //bodyTextPane.setContentType("text/html");
            System.out.println("Found html");
            body += (String)part.getContent();
            this.messageBody = part;
        }
        else if (part.isMimeType("multipart/*"))
        {
            System.out.println("Found multipart/*");
            Multipart multipart = (Multipart)part.getContent();
            level++; //Increment the level...
            int count = multipart.getCount();
            for (int i = 0; i < count; ++i)
                writeMessage(multipart.getBodyPart(i));
            level--; //We're out, so decrement the level
        }
        else if (part.isMimeType("message/rfc822"))
        {
            System.out.println("Found message/rfc822");
            level++; //Again increment as we're going 'deeper' into the message
            writeMessage((Part)part.getContent());
            level--; //Decrement level
        }
        else
        {
            System.out.println("Into else!");
            //TODO Try and show the attatchment?
            if (level != 0 && part instanceof MimeBodyPart && !part.isMimeType("multipart/*"))
            {
                System.out.println("Into attatchment section!");
                String disposition = part.getDisposition();
                //Some mailers don't include a content-disposition
                if (disposition == null || disposition.equalsIgnoreCase(Part.ATTACHMENT))
                {
                    if (filename == null)
                    {
                        filename = "Attachment" + attatchmentNo++;
                        return; //TODO Make this so it doesn't return?
                    }
                    System.out.println("Saving attatchment to file: " + filename);
                    
                    
                    DefaultListModel listModel = (DefaultListModel) attachmentList.getModel();
                    listModel.insertElementAt(filename, listLevel);
                    //Insert attachment into arraylist
                    attachments.add(part);
                    
                    
                   // try
                   // {
 
                        /*File file = new File(filename);
                        if (file.exists())
                        {
                            //TODO What to do if file exists?
                            //throw new IOException("file already exists!");
                           
                        }
                        ((MimeBodyPart)part).saveFile(file);*/
                    //}
                    /*catch (IOException ex)
                    {
                        System.err.println("Failed to save attachment!" + ex);
                        JOptionPane.showConfirmDialog(rootPane, "Error! Could not save attatchment!\n" + ex.getMessage(), "Error!", JOptionPane.OK_OPTION);
                    }*/
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
        replyTo = addresses[0].toString();
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
        recipient = toText;
        toLabel.setText(toText);
        
        subjectLabel.setText(message.getSubject());
        subject = message.getSubject();
        
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
        subjectLabelStatic = new javax.swing.JLabel();
        subjectLabel = new javax.swing.JLabel();
        replyButton = new javax.swing.JButton();
        forwardButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        sentLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        replyToLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        attachmentList = new javax.swing.JList();
        browserPanel = new javax.swing.JPanel();

        fromLabelStatic.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        fromLabelStatic.setText("From:");

        toLabelStatic.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        toLabelStatic.setText("To:");

        sentLabelStatic.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
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
            JOptionPane.showConfirmDialog(null, "Could not get from field!", "Error!", JOptionPane.OK_OPTION);
            ex.printStackTrace();
        }

        if (messageFrom != null)
        {
            fromLabel.setText(messageFrom);
        }
        else
        fromLabel.setText("???");

        toLabel.setText("To Label");

        subjectLabelStatic.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        subjectLabelStatic.setText("Subject:");

        String subject = null;

        try { subject = message.getSubject(); }
        catch (MessagingException ex)
        {
            JOptionPane.showConfirmDialog(null, "Could not get subject field!", "Error!", JOptionPane.OK_OPTION);
            ex.printStackTrace();
        }

        if (subject != null)
        subjectLabel.setText(subject);
        else
        subjectLabel.setText("???");

        replyButton.setText("Reply");
        replyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replyButtonActionPerformed(evt);
            }
        });

        forwardButton.setText("Forward");

        deleteButton.setText("Delete");

        sentLabel.setText("Sent Label");

        jLabel1.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        jLabel1.setText("Reply To:");

        replyToLabel.setText("Reply To");

        jLabel2.setFont(new java.awt.Font("DejaVu Sans", 1, 13)); // NOI18N
        jLabel2.setText("Attachments:");

        attachmentList.setModel(new DefaultListModel());
        attachmentList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                attachmentListMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(attachmentList);

        javax.swing.GroupLayout browserPanelLayout = new javax.swing.GroupLayout(browserPanel);
        browserPanel.setLayout(browserPanelLayout);
        browserPanelLayout.setHorizontalGroup(
            browserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 577, Short.MAX_VALUE)
        );
        browserPanelLayout.setVerticalGroup(
            browserPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 435, Short.MAX_VALUE)
        );

        browserPanel.setLayout(new BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(subjectLabelStatic)
                                .addGap(12, 12, 12)
                                .addComponent(subjectLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(replyButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(forwardButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(deleteButton)))
                        .addGap(392, 411, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(browserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(fromLabelStatic)
                                    .addComponent(toLabelStatic, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(fromLabel)
                                    .addComponent(replyToLabel)
                                    .addComponent(toLabel))
                                .addGap(0, 16, Short.MAX_VALUE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(sentLabelStatic))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 281, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sentLabel))))
                        .addContainerGap())))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(fromLabel)
                            .addComponent(fromLabelStatic))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(replyToLabel)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(toLabel)
                            .addComponent(toLabelStatic))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(subjectLabel)
                            .addComponent(subjectLabelStatic)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(sentLabel)
                            .addComponent(sentLabelStatic))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void attachmentListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_attachmentListMouseClicked
        //Double click
        if (evt.getClickCount() == 2)
        {
            int index = attachmentList.getSelectedIndex();
            if (index < 0) {
                return;
            }
            
            String filename = (String)attachmentList.getSelectedValue();
            MimeBodyPart attachment = (MimeBodyPart)attachments.get(index);
            
            final JFileChooser fileChooser = new JFileChooser();
            
            fileChooser.setSelectedFile(new File(filename));
            
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                File file = fileChooser.getSelectedFile();
                try 
                {
                    attachment.saveFile(file);
                } 
                catch (IOException ex) 
                {
                    JOptionPane.showMessageDialog(null, ex.toString(), "IOException", JOptionPane.ERROR_MESSAGE);
                } 
                catch (MessagingException ex) 
                {
                    JOptionPane.showMessageDialog(null, ex.toString(), "Messaging Exception", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_attachmentListMouseClicked

    private void replyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replyButtonActionPerformed

        //ComposeMailScreen replyScreen = new ComposeMailScreen(config, session, messageBody, replyTo, recipient, subject);
        //replyScreen.setVisible(true);
    }//GEN-LAST:event_replyButtonActionPerformed

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
    private javax.swing.JList attachmentList;
    private javax.swing.JPanel browserPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton forwardButton;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel fromLabelStatic;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
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
