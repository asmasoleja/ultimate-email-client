/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * A class which represents a message in the JTree
 * @author Adam
 */
public class MessageNode extends DefaultMutableTreeNode
{
    //The message held by this node
    private Message message;
    
    public MessageNode(Message message)
    {
        this.message = message;
    }
    
    //A message is always a leaf(?)
    @Override
    public boolean isLeaf()
    {
        return true;
    }
    
    /** 
     * Returns the children for this node
     */   
    @Override
    public int getChildCount()
    {
        return 1;
    }
    
    public Message getMessage()
    {
        return this.message;
    }
    
    public String toString()
    {
        String from = "";
        try 
        {
            Address[] addresses = message.getFrom();
            if (addresses != null)
            {
                for (int i = 0; i < addresses.length; ++i)
                {
                    from += addresses[i].toString() + " ";
                }
            }
            
            return message.getSubject() + "   " + from;
        } 
        catch (MessagingException ex) 
        {
            System.err.println("Can't show message from!");
            ex.printStackTrace();
        }
        
        return "???";
    }
}
