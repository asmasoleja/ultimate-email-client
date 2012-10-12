/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import com.gmail.higginson555.adam.FolderNode;
import com.gmail.higginson555.adam.MessageNode;
import com.gmail.higginson555.adam.ProtectedPassword;
import com.gmail.higginson555.adam.StoreNode;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
 
import javax.mail.*;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
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
    //Model for email table
    private EmailTableModel model = null;

    /**
     * Creates new form HomeScreen
     */
    public HomeScreen(Properties config) 
    {
        this.config = config;
        this.messagesInFolder = new ArrayList<Message>(MESSAGES_DEFAULT_SIZE);
        
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
        
        String usernameProp = config.getProperty("username");
        String serverType = config.getProperty("server_type");
        String incoming = config.getProperty("incoming_server");
        String outgoing = config.getProperty("outgoing_server");
        String smtpPort = config.getProperty("smtp_port");
        
        System.out.println(incoming);
        CountDownLatch countdown = null;
        //If these haven't been filled in, we can't connect to the server!
        if (usernameProp == null || serverType == null || incoming == null 
                || smtpPort == null || outgoing == null)
        {
            //Open up options pane
            countdown = new CountDownLatch(1);
            OptionsScreen options = new OptionsScreen(config, false);
            options.setVisible(true);
            options.setAlwaysOnTop(true);
        }
                       
        this.setLocationRelativeTo(null);
        
        //Try connecting, waiting for the options screen to exit if there is one
        if (countdown != null)
        {
            try 
            {
                System.out.println("AWAITING!");
                countdown.await();
            } catch (InterruptedException ex) 
            {
                JOptionPane.showMessageDialog(rootPane, ex.toString(), "Interrupted Exception!", JOptionPane.ERROR_MESSAGE);
            }
        }
        connectToServer();
    }
    
    class EmailTableModel extends AbstractTableModel
    {
        private String[] columnNames = {"Subject", "From", "Date", "Read"};
        private Object[][] data = {{"", "", "", ""}};
        
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
        
        public void setData(Object[][] newData)
        {
            data = newData;
            fireTableDataChanged();
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

        composeButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        emailJTree = new javax.swing.JTree();
        jScrollPane1 = new javax.swing.JScrollPane();
        emailTable = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        dlProgressBar = new javax.swing.JProgressBar();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        settingsMenu = new javax.swing.JMenu();
        optionsMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ultimate E-mail Client");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("homeFrame");

        composeButton.setText("Compose");
        composeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                composeButtonActionPerformed(evt);
            }
        });

        refreshButton.setText("Refresh");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        emailJTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailJTreeMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(emailJTree);

        emailTable.setModel(new EmailTableModel()
        );
        emailTable.setShowHorizontalLines(false);
        emailTable.setShowVerticalLines(false);
        emailTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                emailTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(emailTable);

        jTextField1.setText("Search...");

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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(composeButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 817, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(dlProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(composeButton)
                    .addComponent(refreshButton)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane3)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 562, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dlProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /*
     * Activated when compose button is pressed. Creates a new ComposeMailScreen,
     * to allow the user to compose a new mail message
     */
    private void composeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_composeButtonActionPerformed

        ComposeMailScreen mailScreen = new ComposeMailScreen(config.getProperty("username"), password, this.properties);       
        mailScreen.setVisible((true));
    }//GEN-LAST:event_composeButtonActionPerformed

    /*
     * TODO opening options
     */
    private void optionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsMenuItemActionPerformed
        
        //OptionsScreen options = new OptionsScreen(config);
        //options.setVisible(true);
    }//GEN-LAST:event_optionsMenuItemActionPerformed

    /*
     * Tries to connect to the server and opens a store
     */
    private void connectToServer()
    {
        try
        {
            Authenticator authenticator = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getProperty("username"), password);
                }
            };
            
            //Set properties
            this.properties = System.getProperties();
            this.properties.put("mail.smtp.auth", "true");
            this.properties.put("mail.smtp.starttls.enable", "true");
            this.properties.put("mail.smtp.host", config.getProperty("outgoing_server"));
            this.properties.put("mail.smtp.port", config.getProperty("smtp_port"));
            
            Session session = Session.getInstance(properties, authenticator);
            
            //config.load(new FileInputStream("config.properties"));
            
            //Read info from property file
            String usernameProp = config.getProperty("username");
            String serverType = config.getProperty("server_type");
            String incoming = config.getProperty("incoming_server");
            
            //If these haven't been filled in, we can't connect to the server!
            if (usernameProp == null || serverType == null || incoming == null)
            {
                JOptionPane.showMessageDialog(rootPane, "Make sure option fields are filled!", 
                                              "Error!", JOptionPane.OK_OPTION);
                
                return;
            }

            System.out.println("Username: " + usernameProp + " Server type: " + serverType + " incoming " + incoming);
            store = session.getStore(serverType.toLowerCase());
            
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
                
        StoreNode storeNode = new StoreNode(store, config, password);
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
            JOptionPane.showConfirmDialog(rootPane, "Error! Could not close connections!", "Error!", JOptionPane.OK_OPTION);
        }
        connectToServer();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void emailJTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_emailJTreeMouseClicked
        if (evt.getClickCount() == 2 || evt.getClickCount() == 3)
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)emailJTree.getLastSelectedPathComponent();
        
            if (selectedNode.isLeaf())
                System.out.println("Is leaf!");
            if (selectedNode instanceof FolderNode)
            {
                System.out.println("In if!");
                FolderNode folderNode = (FolderNode)selectedNode;
                //Get all messages held in this folder
                Folder folder = folderNode.getFolder();
                    
                try 
                {
                    System.out.println(folder.getType());
                    System.out.println("Hold folders: " + Folder.HOLDS_FOLDERS);
                    if (folder.getType() == Folder.HOLDS_FOLDERS)
                    {
                        System.out.println("Holds folders! " + folder.getType());
                        return;
                    }
                    if (!folder.isOpen())
                        folder.open(Folder.READ_ONLY);
                    //Get all messages in this folder
                    Message[] allMessages = folder.getMessages();
                    
                    dlProgressBar.setMaximum(allMessages.length);
                    dlProgressBar.setValue(0);
                    
                    FetchProfile fp = new FetchProfile();
                    fp.add(FetchProfile.Item.ENVELOPE);
                    fp.add(FetchProfile.Item.FLAGS);
                    fp.add(FetchProfile.Item.CONTENT_INFO);
                    //fp.add("X-mailer");
                    
                    folder.fetch(allMessages, fp);
                    
                    EmailTableModel emailModel = (EmailTableModel) emailTable.getModel();
                    emailModel.setRowCount(allMessages.length);
                    //First row is subject
                    //TODO insert back to front, front at the moment is oldest value
                    Object[][] newData = new Object[allMessages.length][4];
                    
                    long startTime = System.nanoTime();
                    int progress = 0;
                    
                    //Make sure to empty out current messages in ArrayList
                    messagesInFolder.clear();
                    
                    for (int i = allMessages.length - 1; i >= 0; i--)
                    {
                        long startMessage = System.nanoTime();
                        String subject = allMessages[i].getSubject();
                        Address[] addresses = allMessages[i].getFrom();
                        String from = addresses[0].toString();
                        String date = allMessages[i].getSentDate().toString();
                        
                        newData[allMessages.length - 1 - i][0] = subject;
                        newData[allMessages.length - 1 - i][1] = from;
                        newData[allMessages.length - 1 - i][2] = date;
                        newData[allMessages.length - 1 - i][3] = Boolean.TRUE;
                        long time = System.nanoTime() - startMessage;
                        System.out.println("Time: " + (double)(time / 1000000000.0));
                        //System.out.println("Set: " + (allMessages.length - 1 - i));
                        
                        messagesInFolder.add(allMessages[i]);
                        
                        progress++;
                        dlProgressBar.setValue(progress);
                        
                    }
                    long estimatedTime = System.nanoTime() - startTime;
                    System.out.println("Estimated time: " + (double)(estimatedTime / 1000000000.0));
                    
                    emailModel.setData(newData);
                    
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
            int row = emailTable.rowAtPoint(evt.getPoint());
            ViewMailScreen viewMail = new ViewMailScreen(messagesInFolder.get(row));
            viewMail.setVisible(true);
            //TODO get message at this row
        }
    }//GEN-LAST:event_emailTableMouseClicked

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
    private javax.swing.JProgressBar dlProgressBar;
    private javax.swing.JTree emailJTree;
    private javax.swing.JTable emailTable;
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
