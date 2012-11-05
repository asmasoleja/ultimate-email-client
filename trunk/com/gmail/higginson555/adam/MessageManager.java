package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Stack;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * A class which handles message transfer from the local database
 * @author Adam
 */
public class MessageManager 
{
    //The database to use
    private Database database;
    
    public MessageManager(Database database)
    {
        this.database = database;
    }
    
    public void addMessages(Message[] messages, FolderManager fm)
            throws SQLException, MessagingException
    {
        
        if (messages.length == 0)
            return;
        //Get folder list, each folder is in another folder, or at the the top
        //when it's parent folder is NULL.
        //
        //As all messages will share the same folder, we only have to do this
        //once.
        ArrayList<String> folderStack = new ArrayList<String>();
        Folder folder = messages[0].getFolder().getParent();

        System.out.println("This folder = " + messages[0].getFolder().getName());
        System.out.println("parent folder = " + folder.getName());
        int folderID = -1;
        //Null when has no parents
        if (folder == null || folder.getName().isEmpty())
        {
            folderID = fm.getFolderID(messages[0].getFolder().getName());
        }
        else
        {
            //Build parent folder tree
            while (folder != null && !folder.getName().isEmpty())
            {
                folderStack.add(folder.getName());
                System.out.println("Adding: " + folder.getName() + "@ " + folderStack.lastIndexOf(folder.getName()));
                folder = folder.getParent();
            }
            
            Collections.reverse(folderStack);

            String[] parentFolderList = folderStack.toArray(new String[folderStack.size()]);
            for (int i = 0; i < parentFolderList.length; i++)
            {
                System.out.println("ParentFolderList[" + i + "] = " + parentFolderList[i]);
            }
            folderID = fm.getFolderID(messages[0].getFolder().getName(), parentFolderList);
        }
        
        ArrayList<Object[]> dbData = new ArrayList<Object[]>(messages.length);
        
        for (Message message : messages)
        {
            String subject = "";
            subject += message.getSubject();
            Address[] a = message.getFrom();
            String from = "";
            from += a[0].toString();

            String to = "";
            a = message.getAllRecipients();
            if (a != null)
            {
                for (int i = 0; i < a.length - 1; i++)
                {
                    to += a[i].toString() + ",";
                }
                //So we don't add a comma at the end
                to += a[a.length - 1];
            }
            
            Date dateSent = message.getSentDate();
            Date dateReceived = message.getReceivedDate();   
            
            Object[] line = {1, subject, from, to, dateSent, dateReceived, folderID};         
            dbData.add(line);
        }
        
        String[] fieldNames = {"messageUID", "subject", "messageFrom", 
            "messageTo", "dateSent", "dateReceived", "folderID"};
        
        database.insertRecords("Messages", fieldNames, dbData);
    }
    
    /*public void addMessage(Message message, FolderManager fm) 
            throws SQLException, MessagingException
    {
        String subject = message.getSubject();
        Address[] a = message.getFrom();
        String from = a[0].toString();
        
        a = message.getAllRecipients();
        String to = "";
        for (int i = 0; i < a.length - 1; i++)
        {
            to += a[i].toString() + ",";
        }
        //So we don't add a comma at the end
        to += a[a.length - 1];
        Date dateSent = message.getSentDate();
        Date dateReceived = message.getReceivedDate();
        Folder folder = message.getFolder();
        
        fm.
    }*/
    
}
