/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.gui;

import chrriis.common.UIUtils;
import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.AccountManager;
import com.gmail.higginson555.adam.AccountMessageDownloader;
import com.gmail.higginson555.adam.ClientThreadPool;
import com.gmail.higginson555.adam.UserDatabase;
import com.gmail.higginson555.adam.view.View;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author Adam
 */
public class AllViewsScreen extends javax.swing.JFrame implements PropertyListener
{
    //All the accounts currently in the system
    private ArrayList<Account> accounts;
    //Currently active message adder threads
    private int messageManagerThreads;
    //The set of accounts which could not connect!
    private HashSet<Account> failedAccounts;
    
    //Account downloader threads
    private ArrayList<AccountMessageDownloader> downloaders;
    

    /**
     * Creates new form AllViewsScreen
     */
    public AllViewsScreen() {
        failedAccounts = new HashSet<Account>();
        initComponents();
        try {
            accounts = AccountManager.getSingleton().getAllAccounts();
            downloaders = new ArrayList<AccountMessageDownloader>(accounts.size());
            if (!accounts.isEmpty())
            {
                for (Account account : accounts)
                {
                    try
                    {
                        AccountMessageDownloader amd = AccountMessageDownloader.getInstance(account);
                        downloaders.add(amd);
                        //ClientThreadPool.executorService.submit(amd);
                    }
                    catch (MessagingException ex)
                    {
                        JOptionPane.showMessageDialog(rootPane, "Could not connect to account: " + account.getUsername(), "Error", JOptionPane.ERROR_MESSAGE);
                        failedAccounts.add(account);
                        //ex.printStackTrace();
                    }
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(AllViewsScreen.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        } 
        //Build the tree, each account has a list of views
        buildTree();
        startDownloaders();
    }
    
    private void startDownloaders()
    {
        for (AccountMessageDownloader downloader : downloaders)
        {
            downloader.addListener(this);
            ClientThreadPool.executorService.submit(downloader);
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

        jMenu3 = new javax.swing.JMenu();
        viewsLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        viewTree = new javax.swing.JTree();
        tabbedPane = new javax.swing.JTabbedPane();
        openViewsLabel = new javax.swing.JLabel();
        warningLabel = new javax.swing.JLabel();
        closeTabButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        newMessageMenuItem = new javax.swing.JMenuItem();
        sqlServerSettingsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        accountMenu = new javax.swing.JMenu();
        newAccountMenuItem = new javax.swing.JMenuItem();
        viewTrustedAccountsItem = new javax.swing.JMenuItem();
        viewsMenu = new javax.swing.JMenu();
        addViewMenuItem = new javax.swing.JMenuItem();

        jMenu3.setText("jMenu3");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Ultimate E-mail Client");

        viewsLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
        viewsLabel.setText("Views");

        viewTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                viewTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(viewTree);

        openViewsLabel.setFont(new java.awt.Font("Tahoma", 1, 12));
        openViewsLabel.setText("Opened Views");

        warningLabel.setText("State: OK");

        closeTabButton.setText("Close Selected Tab");
        closeTabButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeTabButtonActionPerformed(evt);
            }
        });

        jMenu1.setText("File");

        newMessageMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        newMessageMenuItem.setText("New Message");
        newMessageMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newMessageMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(newMessageMenuItem);

        sqlServerSettingsMenuItem.setText("SQL Server Settings");
        sqlServerSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sqlServerSettingsMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(sqlServerSettingsMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        accountMenu.setText("Account");

        newAccountMenuItem.setText("New Account");
        newAccountMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newAccountMenuItemActionPerformed(evt);
            }
        });
        accountMenu.add(newAccountMenuItem);

        viewTrustedAccountsItem.setText("View Trusted Accounts");
        viewTrustedAccountsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewTrustedAccountsItemActionPerformed(evt);
            }
        });
        accountMenu.add(viewTrustedAccountsItem);

        jMenuBar1.add(accountMenu);

        viewsMenu.setText("Views");

        addViewMenuItem.setText("Add View");
        addViewMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addViewMenuItemActionPerformed(evt);
            }
        });
        viewsMenu.add(addViewMenuItem);

        jMenuBar1.add(viewsMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(warningLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 926, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(viewsLabel)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 433, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(openViewsLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 692, Short.MAX_VALUE)
                                .addComponent(closeTabButton))
                            .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 927, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(warningLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(viewsLabel)
                    .addComponent(openViewsLabel)
                    .addComponent(closeTabButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 627, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buildTree()
    {   
        try
        {
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Accounts");

            for (Account account : accounts)
            {
                if (!failedAccounts.contains(account))
                {
                    DefaultMutableTreeNode accNode = new DefaultMutableTreeNode(account);
                    ArrayList<View> views = View.getViewsForAccount(UserDatabase.getInstance(), account);
                    for (View view : views)
                    {
                        DefaultMutableTreeNode viewNode = new DefaultMutableTreeNode(view);
                        accNode.add(viewNode);
                    }
                    rootNode.add(accNode);
                }
            }
            
            
            TreeModel treeModel = new DefaultTreeModel(rootNode);
            viewTree.setModel(treeModel);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
    private void newAccountMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newAccountMenuItemActionPerformed
        AddAccountScreen addAccountScreen = new AddAccountScreen(this);
        addAccountScreen.setVisible(true);
    }//GEN-LAST:event_newAccountMenuItemActionPerformed

    private void addViewMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addViewMenuItemActionPerformed
        CreateViewScreen createViewScreen = new CreateViewScreen(this, accounts);
        createViewScreen.setVisible(true);
    }//GEN-LAST:event_addViewMenuItemActionPerformed

    private void viewTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_viewTreeMouseClicked
        //Double click
        if (evt.getClickCount() == 2)
        {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) viewTree.getLastSelectedPathComponent();
            if (selectedNode != null)
            {
                Object nodeObject = selectedNode.getUserObject();
                //View is selected, load data
                if (nodeObject instanceof View)
                {
                    View view = (View) nodeObject;
                    System.out.println("Creating view panel with: " + view.getQuery());
                    ViewPanel viewPanel = new ViewPanel(view);
                    viewPanel.addListener(this);
                    tabbedPane.add(viewPanel);
                }
            }
        }
        
    }//GEN-LAST:event_viewTreeMouseClicked

    private void closeTabButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeTabButtonActionPerformed
        if (tabbedPane.getSelectedIndex() >= 0)
        {
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }
    }//GEN-LAST:event_closeTabButtonActionPerformed

    private void newMessageMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMessageMenuItemActionPerformed
        //Get selected menu item
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) viewTree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
            Object nodeObject = selectedNode.getUserObject();
            while (selectedNode != null && nodeObject != null && !(nodeObject instanceof Account))
            {
                selectedNode = (DefaultMutableTreeNode) selectedNode.getParent();
                nodeObject = selectedNode.getUserObject();
            }

            if (nodeObject != null || nodeObject instanceof Account)
            {
                Account account = (Account) nodeObject;
                Properties properties = System.getProperties();
                properties.setProperty("mail.smtp.host", account.getOutgoing());
                properties.setProperty("mail.smtp.port", Integer.toString(account.getOutgoingPort()));
                System.out.println("Port: " + account.getOutgoingPort());
                properties.setProperty("mail.smtp.starttls.enable", "true");
                properties.setProperty("mail.smtp.auth", "true");
                properties.setProperty("username", account.getUsername());
                properties.setProperty("password", account.getPassword());
                ComposeMailScreen cms = new ComposeMailScreen(properties);
                cms.setVisible(true);
            }
        }
    }//GEN-LAST:event_newMessageMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        int result = JOptionPane.showConfirmDialog(rootPane, "Are you sure you wish to exit?", "Exit confirmation", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
        {
            this.dispose();
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void viewTrustedAccountsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewTrustedAccountsItemActionPerformed
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) viewTree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
            Object nodeObject = selectedNode.getUserObject();
            while (selectedNode != null && nodeObject != null && !(nodeObject instanceof Account))
            {
                selectedNode = (DefaultMutableTreeNode) selectedNode.getParent();
                nodeObject = selectedNode.getUserObject();
            }

            if (nodeObject != null || nodeObject instanceof Account)
            {
                TrustedAccountsScreen tas = new TrustedAccountsScreen((Account) nodeObject);
                tas.setVisible(true);
            }
        }
    }//GEN-LAST:event_viewTrustedAccountsItemActionPerformed

    private void sqlServerSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sqlServerSettingsMenuItemActionPerformed
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) viewTree.getLastSelectedPathComponent();
        if (selectedNode != null)
        {
            Object nodeObject = selectedNode.getUserObject();
            while (selectedNode != null && nodeObject != null && !(nodeObject instanceof Account))
            {
                selectedNode = (DefaultMutableTreeNode) selectedNode.getParent();
                nodeObject = selectedNode.getUserObject();
            }

            if (nodeObject != null || nodeObject instanceof Account)
            {
                SQLServerSettingsScreen screen = new SQLServerSettingsScreen((Account) nodeObject);
                screen.setVisible(true);
            }
        }
    }//GEN-LAST:event_sqlServerSettingsMenuItemActionPerformed

    @Override
    public void dispose()
    {
        ClientThreadPool.shouldStop = true;
        ClientThreadPool.executorService.shutdown();
        try {
            while (!ClientThreadPool.executorService.isTerminated())
            {
                    Thread.sleep(200);
            }
            UserDatabase.getInstance().close();
        } catch (SQLException ex) {
            Logger.getLogger(AllViewsScreen.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        } catch (Exception ex) {
            Logger.getLogger(AllViewsScreen.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        super.dispose();
        System.exit(0);
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        UIUtils.setPreferredLookAndFeel();
        NativeInterface.open();
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        /*try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AllViewsScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AllViewsScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AllViewsScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AllViewsScreen.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new AllViewsScreen().setVisible(true);
            }
        });
        
        NativeInterface.runEventPump();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu accountMenu;
    private javax.swing.JMenuItem addViewMenuItem;
    private javax.swing.JButton closeTabButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JMenuItem newAccountMenuItem;
    private javax.swing.JMenuItem newMessageMenuItem;
    private javax.swing.JLabel openViewsLabel;
    private javax.swing.JMenuItem sqlServerSettingsMenuItem;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTree viewTree;
    private javax.swing.JMenuItem viewTrustedAccountsItem;
    private javax.swing.JLabel viewsLabel;
    private javax.swing.JMenu viewsMenu;
    private javax.swing.JLabel warningLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void onPropertyEvent(Class source, String name, Object value) {
        //Comes from view class
        if (source.equals(View.class))
        {
            if (name.equalsIgnoreCase("DatabaseWrite"))
            {
                buildTree();
            }
        }
        else if (source.equals(AddAccountScreen.class))
        {
            if (name.equalsIgnoreCase("AddAccount"))
            {
                try {
                    Account account = (Account) value;
                    System.out.println("Added account: " + account);
                    accounts.add(account);
                    DefaultTreeModel model = (DefaultTreeModel) viewTree.getModel();
                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
                    DefaultMutableTreeNode child = new DefaultMutableTreeNode(account);
                    root.add(child);               
                    viewTree.revalidate();
                    
                    AccountMessageDownloader amd = AccountMessageDownloader.getInstance(account);
                    amd.addListener(this);
                    ClientThreadPool.executorService.submit(amd);
                } catch (SQLException ex) {
                    Logger.getLogger(AllViewsScreen.class.getName()).log(Level.SEVERE, null, ex);
                } catch (MessagingException ex) {
                    Logger.getLogger(AllViewsScreen.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        else if (name.equalsIgnoreCase("MessageInsertDone"))
        {
            warningLabel.setText("State: OK");
        }
        else if (name.equalsIgnoreCase("MessageManagerThreadStart"))
        {
            messageManagerThreads++;
            System.out.println("Thread started, text should change? Threads: " + messageManagerThreads + " class: " + source.getName());
            warningLabel.setText("State: Adding data to the local database, views may not work correctly!");
        }
        else if (name.equalsIgnoreCase("MessageManagerThreadFinished"))
        {
            messageManagerThreads--;
            System.out.println("------------Thread finished! Thread count is now: " + messageManagerThreads + " Source: " + source.getName() + "\n\n");
            if (messageManagerThreads == 0)
            {
                warningLabel.setText("State: OK");
            }
        }
        else if (name.equalsIgnoreCase("MessageOpened"))
        {
            Object[] data = (Object[]) value;
            Message msg = (Message) data[0];
            Account account = (Account) data[1];
            int messageID = (Integer) data[2];
            boolean shouldExtractTags = (Boolean) data[3];


            tabbedPane.add(new ViewMailScreen(msg, account, messageID, shouldExtractTags));
        }
        else if (name.equalsIgnoreCase("ViewNameChanged"))
        {
            buildTree();
        }
                
    }
}
