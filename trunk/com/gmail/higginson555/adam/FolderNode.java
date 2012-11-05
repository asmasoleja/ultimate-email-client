package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A class which represents a single Folder within a user's email store.
 * 
 * @author Adam
 */
public class FolderNode extends DefaultMutableTreeNode
{
    //The folder held by this node
    private Folder folder;
    //Whether we have loaded up all the subfolders for this folder
    private boolean hasLoadedSubfolders = false;
    //The account this folder belongs to
    private Account account;
    //The folder manager to use
    private FolderManager folderManager;
    
    /**
     * Creates a FolderNode with the specified folder inside it
     * @param folder The folder used for this node
     */
    public FolderNode(Folder folder, FolderManager folderManager, Account account)
    {
        super(folder);
        System.out.println("\n\n----------------CREATING NEW FOLDER NODE " + folder.getName().toUpperCase() + "--------------\n\n");
        this.folder = folder;
        this.folderManager = folderManager;
        this.account = account;
        try 
        {
            Folder parent = null;
            try 
            {
                parent = folder.getParent();
                if (folder.getParent() == null || folder.getParent().getName().isEmpty())
                {
                    folderManager.addFolder(folder.getName(), account);
                }
                else //Folder has parent, work out parent tree!
                {
                    ArrayList<String> folderStack = new ArrayList<String>();
                    while (parent != null && !parent.getName().isEmpty())
                    {
                        folderStack.add(parent.getName());
                        System.out.println("Adding: " + parent.getName() + "@ " + folderStack.lastIndexOf(parent.getName()));
                        parent = parent.getParent();
                    }

                    Collections.reverse(folderStack);
                    String[] parentFolderList = folderStack.toArray(new String[folderStack.size()]);
                    folderManager.addFolder(folder.getName(), parentFolderList, account);
                }
                
            } catch (MessagingException ex) 
            {
                Logger.getLogger(FolderNode.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        } 
        catch (SQLException ex) {
            Logger.getLogger(FolderNode.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    /**
     * A FolderNode is a leaf only if it has no subfolders, or no messages
     * @return true if this folder contains no subfolders and messages, false otherwise
     */
    @Override
    public boolean isLeaf()
    {
        try
        {
            if (folder.getType() == Folder.HOLDS_FOLDERS)
            {
                return false;
            }
        }
        catch (MessagingException mesEx)
        {
            System.err.println("Error in checking is leaf!");
            JOptionPane.showMessageDialog(null, mesEx.toString(), 
                            "Messaing Exception!", JOptionPane.ERROR_MESSAGE);
        }
        
        return true;
    }
    
    /**
     * Get the folder held by this node
     * @return The folder held by this node
     */
    public Folder getFolder()
    {
        return this.folder;
    }
    
    /**
     * Gets the number of children for this node.
     * Loads up this folder's subfolders on the first call of this method
     * @return The number of children contained by this node
     */
    @Override
    public int getChildCount()
    {
        if (!hasLoadedSubfolders)
        {
            loadSubfolders();
        }
        return super.getChildCount();
    }
    
    /*
     * Loads the subfolders of this folder. If no subfolders are found,
     * nothing is done and the method returns.
     */
    private void loadSubfolders()
    {
        //We can't load a leaf's subfolders as there aren't any, so just
        //say we've already loaded them
        if (this.isLeaf())
        {
            hasLoadedSubfolders = true;
            return;
        }
        
        try
        {
            Folder[] subfolders = folder.list();
            //If no subfolders found in this folder
            /*if (subfolders.length == 0)
            {
                //Try and get any messages held by this folder
                if (!folder.isOpen())
                    folder.open(Folder.READ_ONLY);
                Message[] messages = folder.getMessages();
                for (int i = 0; i < messages.length; i++)
                {
                    MessageNode newMessage = new MessageNode(messages[i]);
                    insert(newMessage, i);
                }
            }
            else
            {*/
            //Add folder nodes for each subfolder
            for (int i = 0; i < subfolders.length; ++i)
            {
                FolderNode newNode = new FolderNode(subfolders[i], folderManager, account);
                insert(newNode, i);
            }
            //}
        }
        catch (MessagingException mesEx)
        {
            System.err.println("Error in getting submessages/folders");
            JOptionPane.showMessageDialog(null, mesEx.toString(), 
                    "Messaing Exception!", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public String toString()
    {
        if (this.isLeaf())
        {
            try 
            {
                return folder.getName() + " (" + Integer.toString(folder.getMessageCount()) + ")";
            } 
            catch (MessagingException ex) 
            {
                ex.printStackTrace();
            }    
        }
        
        return folder.getName();
    }
}
