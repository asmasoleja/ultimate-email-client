/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import com.gmail.higginson555.adam.AccountManager;
import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.FolderManager;
import com.gmail.higginson555.adam.FolderNode;
import com.gmail.higginson555.adam.MessageManager;
import com.gmail.higginson555.adam.ProtectedPassword;
import com.gmail.higginson555.adam.StoreNode;
import com.gmail.higginson555.adam.UserDatabaseManager;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.search.SearchTerm;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Adam
 */
public class HomeScreen extends javax.swing.JFrame 
{
    
    //The amount of messages to show by default
    private static final int MESSAGES_DEFAULT_SIZE = 100;
    
    //The password of the user
    private String password;
    //The properties to use for sending messages
    private Properties properties;
    //The properties for the config
    private Properties config;
    //An array list of every message in the currently selected folder
    private ArrayList<Message> messagesInFolder;
    //The store
    private Store store = null;
    //The inbox folder
    private Folder inbox = null;
    //Session for this mail client
    private Session session;
    //The user database to use
    private Database userDatabase;

    /**
     * Creates new form HomeScreen
     */
    public HomeScreen(Properties config) 
    {
        this.config = config;
        this.messagesInFolder = new ArrayList<Message>(MESSAGES_DEFAULT_SIZE);
        
        UserDatabaseManager dbManager = new UserDatabaseManager();
        try 
        {
            this.userDatabase = dbManager.getDatabaseInstance();
        } 
        catch (SQLException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "SQL Exception!", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(HomeScreen.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
            
        } 
        catch (ClassNotFoundException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "Class Not Found Exception!", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(HomeScreen.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        try 
        {
            this.password = ProtectedPassword.decrypt(config.getProperty("password"));
        } 
        catch (GeneralSecurityException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "General Security Exception!", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } 
        catch (IOException ex) 
        {
            JOptionPane.showMessageDialog(rootPane, ex.toString(), "IO Exception!", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
        
        //Initialise GUI
        initComponents();
        
        //Set email table to bold unread messages!
        emailTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) 
            {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Font font = comp.getFont();
                //Get the row and check whether it has been read
                Boolean isRead = (Boolean)table.getModel().getValueAt(row, 3);
                if (!isRead)
                {
                    comp.setFont(font.deriveFont(Font.BOLD));
                }
                else
                {
                    comp.setFont(font.deriveFont(Font.PLAIN));
                }
                
                return comp;
            }
        
        });
                    
        this.setLocationRelativeTo(null);

        connectToServer();
    }
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        String deleteLabel = "Delete";
        folderPopup = new javax.swing.JPopupMenu();
        composeButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        emailJTree = new javax.swing.JTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        emailTable = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        deleteSelectedButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        settingsMenu = new javax.swing.JMenu();
        optionsMenuItem = new javax.swing.JMenuItem();

        folderPopup.add(deleteLabel);
        folderPopup.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                folderPopupPopupMenuWillBecomeInvisible(evt);
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ultimate E-mail Client");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("homeFrame");

        composeButton.setText("Compose");
        composeButton.setToolTipText("Compose a new message");
        composeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                composeButtonActionPerformed(evt);
            }
        });

        refreshButton.setText("Refresh");
        refreshButton.setToolTipText("Refresh to check for new e-mails");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        emailJTree.setToolTipText("The folders of a particular account");
        emailJTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailJTreeMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(emailJTree);

        emailTable.setModel(new EmailTableModel()
        );
        emailTable.setToolTipText("Shows the messages from the selected folder");
        emailTable.setShowHorizontalLines(false);
        emailTable.setShowVerticalLines(false);
        emailTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(emailTable);

        jTextField1.setText("Search...");

        deleteSelectedButton.setText("Delete Selected");
        deleteSelectedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteSelectedButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        settingsMenu.setText("Settings");

        optionsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        optionsMenuItem.setText("Options");
        optionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(optionsMenuItem);

        jMenuBar1.add(settingsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(composeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(deleteSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(composeButton)
                    .addComponent(refreshButton)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteSelectedButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 587, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
     * Activated when compose button is pressed. Creates a new ComposeMailScreen,
     * to allow the user to compose a new mail message
     */
    private void composeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_composeButtonActionPerformed

        ComposeMailScreen mailScreen = new ComposeMailScreen(config, session);
        mailScreen.setVisible((true));
    }//GEN-LAST:event_composeButtonActionPerformed

    /*
     * TODO opening options
     */
    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        
        AddAccountScreen options = new AddAccountScreen(config, false);
        options.setAlwaysOnTop(true);
        options.setVisible(true);
    }//GEN-LAST:event_optionsMenuItemActionPerformed

    /*
     * Tries to connect to the server and opens a store
     */
    private void connectToServer()
    {
        try
        {            
            //Set properties
            properties = System.getProperties();
            /*this.properties.put("mail.smtp.auth", "true");
            this.properties.put("mail.smtp.starttls.enable", "true");
            this.properties.put("mail.smtp.host", config.getProperty("outgoing_server"));
            this.properties.put("mail.smtp.port", config.getProperty("smtp_port"));*/
            properties.setProperty("mail.store.protocol", "imaps");
            
            session = Session.getInstance(properties, null);
            
            //config.load(new FileInputStream("config.properties"));
            
            //Read info from property file
            String usernameProp = config.getProperty("username");
            String password = config.getProperty("password");
            String serverType = config.getProperty("server_type");
            String incoming = config.getProperty("incoming_server");
            String port = config.getProperty("smtp_port");
            
            //If these haven't been filled in, we can't connect to the server!
            if (usernameProp == null || serverType == null || incoming == null || password == null || port == null)
            {
                JOptionPane.showMessageDialog(rootPane, "Make sure option fields are filled!", 
                                              "Error!", JOptionPane.OK_OPTION);
                
                return;
            }

            System.out.println("Username: " + usernameProp + " Server type: " + serverType + " incoming " + incoming);
            //store = session.getStore(serverType.toLowerCase());
            store = session.getStore("imaps");
            
            //Build the JTree folder structure
            buildJTree();         
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }    
    }
    
    private void closeConnections() throws Exception
    {
        if (inbox != null) inbox.close(false);
        if (store != null) store.close();
    }
    
    //Attempts to build the tree, but only after the store object has been
    //built by connectToServer()
    private void buildJTree()
    {
        AccountManager am = new AccountManager(userDatabase);
        StoreNode storeNode = new StoreNode(store, config, password, am);
        DefaultTreeModel treeModel = new DefaultTreeModel(storeNode);
        emailJTree.setModel(treeModel);
        emailJTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);   
    }
    
    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        
        //First try and close existing connections
        try
        {
            closeConnections();
        }
        catch (Exception ex)
        {
            JOptionPane.showConfirmDialog(rootPane, "Error! Could not close connections!\n" + ex.toString(), "Error!", JOptionPane.OK_OPTION);
        }
        connectToServer();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void emailJTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emailJTreeMouseClicked
        
        //System.out.println("Button pressed, Button: " + evt.getButton());
        if (evt.getButton() == MouseEvent.BUTTON3)
        {
            emailJTree.setSelectionRow(emailJTree.getClosestRowForLocation(evt.getX(), evt.getY()));
            folderPopup.show(emailJTree, evt.getX(), evt.getY());
        }
        
        if (evt.getClickCount() == 2)
        {
            System.out.println("\n\n--------------Double click on JTree, D/Ling messages-------------------\n\n");
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)emailJTree.getLastSelectedPathComponent();
        
            if (selectedNode instanceof FolderNode)
            {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                FolderNode folderNode = (FolderNode)selectedNode;
                //Get all messages held in this folder
                Folder folder = folderNode.getFolder();        
                int folderID = folderNode.getFolderID();
                    
                try 
                {
                    if (!folder.isOpen()) {
                        folder.open(Folder.READ_WRITE);
                    }
                    //Get all messages in this folder
                    Message[] allMessages = folder.getMessages();
                                        
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    fp.add(FetchProfile.Item.FLAGS);
                    fp.add(UIDFolder.FetchProfileItem.UID);
                    //fp.add(FetchProfile.Item.CONTENT_INFO);
                    fp.add("X-mailer");
                    
                    System.out.println("Fetching all messages...");
                    long startTime = System.currentTimeMillis();
                    folder.fetch(allMessages, fp);
                    long endTime = System.currentTimeMillis();
                    System.out.println("Done! Took: " + (endTime - startTime) + "ms");
                    
                    EmailTableModel emailModel = (EmailTableModel) emailTable.getModel();
                    emailModel.setRowCount(allMessages.length);
                    //The data the table will hold
                    Object[][] newData = new Object[allMessages.length][4];
                    
                    //Make sure to empty out current messages in ArrayList
                    messagesInFolder.clear();
                    
                    ArrayList<Object[]> dbData = new ArrayList<Object[]>(allMessages.length); 
                    for (int i = allMessages.length - 1; i >= 0; i--)
                    {
                        startTime = System.currentTimeMillis();
                        String subject = "";
                        String from = "";
                        subject = allMessages[i].getSubject();
                        Address[] addresses = allMessages[i].getFrom();
                        from = addresses[0].toString();
                        String to = "";
                        //This bit here is slow for some reason?
                        /*addresses = allMessages[i].getAllRecipients();
                        if (addresses != null)
                        {
                            for (int j = 0; j < addresses.length - 1; j++)
                            {
                                to += addresses[j].toString() + ",";
                            }
                            //So we don't add a comma at the end
                            to += addresses[addresses.length - 1];
                        }*/
                        String UID = "";
                        if (config.getProperty("server_type").equals("IMAP"))
                        {
                            IMAPFolder imapFolder = (IMAPFolder) folder;
                            UID = Long.toString(imapFolder.getUID(allMessages[i]));
                        }
                        else if (config.getProperty("server_type").equals("POP3"))
                        {
                            POP3Folder pop3Folder = (POP3Folder) folder;
                            UID = pop3Folder.getUID(allMessages[i]);
                        }
                        String date = "";
                        Boolean isRead = true;
                        date = allMessages[i].getSentDate().toString();
                        isRead = allMessages[i].isSet(Flags.Flag.SEEN);        
                        
                        Date dateSent = allMessages[i].getSentDate();
                        Date dateReceived = allMessages[i].getReceivedDate();
                        Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderNode.getFolderID()};
                        dbData.add(line);
                        
                        newData[allMessages.length - 1 - i][0] = subject;
                        newData[allMessages.length - 1 - i][1] = from;
                        newData[allMessages.length - 1 - i][2] = date;
                        newData[allMessages.length - 1 - i][3] = isRead;
                        messagesInFolder.add(allMessages[i]); 
                        endTime = System.currentTimeMillis();
                        
                        System.out.println("To add a single line of data took: " + (endTime - startTime));
                    }
                    
                    //Need to sort by date. Probably more efficient ways of doing
                    //this...
                    Comparator<Message> messageComp = new Comparator<Message>() 
                    {

                        @Override
                        public int compare(Message o1, Message o2) {
                            try 
                            {
                                Date d1 = o1.getReceivedDate();
                                Date d2 = o2.getReceivedDate();
                                return d2.compareTo(d1);
                            } catch (MessagingException ex) 
                            {
                                Logger.getLogger(HomeScreen.class.getName()).log(Level.SEVERE, null, ex);
                                System.exit(-1);
                            }                          
                            return 0;
                        }
                    };
                    
                    Comparator<Object[]> tableDataComp = new Comparator<Object[]>()
                    {
                        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
                        @Override
                        public int compare(Object[] o1, Object[] o2)
                        {
                            try 
                            {
                                Date d1 = sdf.parse((String) o1[2]);
                                Date d2 = sdf.parse((String) o2[2]);
                                return d2.compareTo(d1);
                            } catch (ParseException ex) {
                                
                                Logger.getLogger(HomeScreen.class.getName()).log(Level.SEVERE, null, ex);
                                System.exit(-1);
                            }
                            return 0;
                        }
                    };
                    
                    System.out.println("Sorting data...");
                    Collections.sort(messagesInFolder, messageComp);
                    Arrays.sort(newData, tableDataComp);
                    System.out.println("Done!");
                    
                    Comparator<Object[]> messageDataComp = new Comparator<Object[]>()
                    {

                        @Override
                        public int compare(Object[] o1, Object[] o2) 
                        {
                            Date d1 = (Date) o1[5];
                            Date d2 = (Date) o2[5];
                            
                            return d2.compareTo(d1);
                        }
                        
                    };
                    
                    Collections.sort(dbData, messageDataComp);
                    
                    
                    
                    //System.out.println("Setting last received date to: " + dateRec);
                    FolderManager fm = new FolderManager(userDatabase);
                    Date lastSetDate = fm.getLastDate(folderID);
                    System.out.println("\nLast set date: " + lastSetDate);
                    
                    Iterator<Object[]> dataIter = dbData.iterator();
                    ArrayList<Object[]> dbDataToAdd = new ArrayList<Object[]>();
                    boolean isUpdatingDB = false;
                    while (dataIter.hasNext())
                    {
                        Object[] currentLine = dataIter.next();
                        Date foundDate = (Date) currentLine[5];
                        System.out.println("Found date: " + foundDate);
                        
                        //If we've found a message which was sent after the last set
                        //date, we need to update the database with the latest message data
                        if (lastSetDate == null || foundDate.after(lastSetDate))
                        {
                            dbDataToAdd.add(currentLine);
                            isUpdatingDB = true;
                        }
                        else
                        {
                            break;
                        }
                    }
                    
                    
                    if (isUpdatingDB)
                    {
                        System.out.println("\n----UPDATING DATABASE WITH NEW MESSAGES!----\n");
                        fm.setLastDate(folderID, (Date) dbDataToAdd.get(0)[5]);
                        MessageManager mm = new MessageManager(userDatabase);
                        mm.addMessages(dbDataToAdd);
                        System.out.println("\n\n\n\n -------------- RETURNED, THREAD WORKED?! ------------------\n\n\n\n");
                    }
                    //Set table data          
                    emailModel.setData(newData);
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                }
                catch (SQLException ex)
                {
                    JOptionPane.showMessageDialog(rootPane, ex.toString(), "SQL Exception!", JOptionPane.ERROR_MESSAGE);
                }
                catch (MessagingException ex) 
                {
                    JOptionPane.showMessageDialog(rootPane, ex.toString(), "Messaging Exception!", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }//GEN-LAST:event_emailJTreeMouseClicked

    private void emailTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emailTableMouseClicked
        if (evt.getClickCount() == 2 || evt.getClickCount() == 3)
        {            
            //Make sure to set message as read!
            int row = emailTable.rowAtPoint(evt.getPoint());
            Message selectedMsg = messagesInFolder.get(row);
            try 
            {
                selectedMsg.setFlag(Flags.Flag.SEEN, true);
                emailTable.getModel().setValueAt(Boolean.TRUE, row, 3);
            } 
            catch (MessagingException ex) {
                JOptionPane.showMessageDialog(rootPane, ex.toString(), "MessagingException", JOptionPane.ERROR_MESSAGE);
            }
            ViewMailScreen viewMail = new ViewMailScreen(selectedMsg, config, session);
            viewMail.setVisible(true);
            //TODO get message at this row
        }
    }//GEN-LAST:event_emailTableMouseClicked

    private void folderPopupPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_folderPopupPopupMenuWillBecomeInvisible
        System.out.println("Will become invisible!");
        if (folderPopup.getSelectionModel().isSelected())
        {
            int index = folderPopup.getSelectionModel().getSelectedIndex();
            System.out.println("Index: " + index);
            if (index == 0)
            {
                int result = JOptionPane.showConfirmDialog(rootPane, "Are you sure you wish to delete this folder?", "Confirm", JOptionPane.YES_NO_OPTION);
                
                if (result == JOptionPane.YES_OPTION)
                {
                    System.out.println("Deleting folder...");
                }
            }
        }
    }//GEN-LAST:event_folderPopupPopupMenuWillBecomeInvisible

    private void deleteSelectedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteSelectedButtonActionPerformed
        int[] selectedRows = emailTable.getSelectedRows();
        
        int result = JOptionPane.showConfirmDialog(rootPane, 
                "Are you sure you want to delete " 
                + Integer.toString(selectedRows.length) 
                + " messages?", "Confirm", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION)
        {
            try
            {
                for (int i = selectedRows.length - 1; i >= 0; i--)
                {
                    Message msg = messagesInFolder.remove(selectedRows[i]);
                    msg.setFlag(Flags.Flag.DELETED, true);
                    System.out.println("Selected row: " + selectedRows[i]);
                }
                
                
                
                
                EmailTableModel model = (EmailTableModel)emailTable.getModel();
                model.deleteRows(selectedRows);
            }
            catch (MessagingException ex)
            {
                JOptionPane.showMessageDialog(rootPane, "Could not delete message!\n" 
                        + ex.toString(), "MessagingException", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_deleteSelectedButtonActionPerformed

    @Override
    public void dispose()
    {
        try
        {
            closeConnections();
        }
        catch (Exception exClose) {exClose.printStackTrace();}
        
        super.dispose();
    }
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
            java.util.logging.Logger.getLogger(HomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(HomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(HomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(HomeScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
               // new HomeScreen().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton composeButton;
    private javax.swing.JButton deleteSelectedButton;
    private javax.swing.JTree emailJTree;
    private javax.swing.JTable emailTable;
    private javax.swing.JPopupMenu folderPopup;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JButton refreshButton;
    private javax.swing.JMenu settingsMenu;
    // End of variables declaration//GEN-END:variables
}
