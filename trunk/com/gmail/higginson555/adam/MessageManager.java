package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.Date;
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
    
    public void addMessage(Message message, FolderManager fm) 
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
    }
    
}
