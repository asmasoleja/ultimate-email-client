package com.gmail.higginson555.adam;

import javax.mail.Folder;
import javax.mail.Message;
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
    //Whether we habe loaded up all the subfolders for this folder
    private boolean hasLoadedSubfolders = false;
    
    /**
     * Creates a FolderNode with the specified folder inside it
     * @param folder The folder used for this node
     */
    public FolderNode(Folder folder)
    {
        super(folder);
        this.folder = folder;
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
            if ((folder.getType() & Folder.HOLDS_FOLDERS) == 0)
            {
                return true;
            }
        }
        catch (MessagingException mesEx)
        {
            System.err.println("Error in checking is leaf!");
            JOptionPane.showMessageDialog(null, mesEx.toString(), 
                            "Messaing Exception!", JOptionPane.ERROR_MESSAGE);
        }
        
        return false;
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
                FolderNode newNode = new FolderNode(subfolders[i]);
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
        return folder.getName();
    }
}
