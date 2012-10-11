package com.gmail.higginson555.adam;

import java.util.Properties;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A class which represents a Store object from the JavaMail API.
 * A store object links to many folders within one particular store,
 * representing a user's found e-mail folders. It is a subclass of
 * Swing's DefaultMutableTreeNode, used for drawing it in a JTree(??).
 * 
 * Based off the demo code supplied with JavaMail
 * 
 * @author Adam
 */
public class StoreNode extends DefaultMutableTreeNode 
{
    //The store object
    private Store store;
    //The default folder
    private Folder folder;
    //The config file holding info about username and password
    private Properties config;
    //Username and password of the user. Used for connecting to the store
    private String password;
    
    public StoreNode(Store store, Properties config, String password)
    {
        super(store);
        this.store = store;
        this.config = config;
        //this.username = username;
        this.password = password;
    }
    
    /**
     * Overrides the DefaultMutableTreeNode's implementation of
     * isLeaf(). A store can't be a leaf because it will always 
     * contain something else
     * @return 
     */
    @Override
    public boolean isLeaf()
    {
        return false;
    }
    
    /**
     * Get the number of children for this node.
     * We load up all the folders in the default folder on first
     * call of this method.
     * @return 
     */
    @Override
    public int getChildCount()
    {
        if (folder == null)
        {
            getFolders();
        }
        return super.getChildCount();
    }
    
    private void getFolders()
    {
        try
        {
            if (!store.isConnected())
            {
                //store.connect();
                String username = config.getProperty("username");
                String incoming = config.getProperty("incoming_server");
                store.connect(incoming, username, password);
            }
            
            //Get default folder
            folder = store.getDefaultFolder();
            //List all subscribed folders to it
            Folder[] subscribedFolders = folder.list();         
            //Create a FolderNode for every folder found
            for (int i = 0; i < subscribedFolders.length; ++i)
            {
                FolderNode newNode = new FolderNode(subscribedFolders[i]);
                //Insert into tree
                insert(newNode, i);
            }
            
            
        }
        catch (MessagingException mesEx)
        {
            System.out.println("In store node messaging exception!");
            JOptionPane.showMessageDialog(null, mesEx.toString(), 
                            "Messaing Exception!", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public String toString()
    {
        return store.getURLName().getUsername();
    }
    
}
