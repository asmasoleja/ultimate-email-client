package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.mail.MessagingException;

/**
 * A class which handles message transfer from the local database
 * @author Adam
 */
public class MessageManager 
{
    private static final int MESSAGE_LINE_LENGTH = 9;
    //The account this is for
    private Account account;
    //The database to use
    private Database database;
    //Property listeners
    private ArrayList<PropertyListener> listeners;
    
    public MessageManager(Account account, Database database)
    {
        this.account = account;
        this.database = database;
        listeners = new ArrayList<PropertyListener>();
    }
    
    public void addListener(PropertyListener listener)
    {
        listeners.add(listener);
    }
    
    private void publishPropertyEvent(String name, Object value)
    {
        for (PropertyListener listener : listeners)
        {
            listener.onPropertyEvent(this.getClass(), name, value);
        }
    }
    
    /**
     * 
     * @param messages UID, from, to, sent, rec
     * @param fm
     * @throws SQLException
     * @throws MessagingException 
     */
    public void addMessages(ArrayList<Object[]> dbData)
            throws SQLException, MessagingException
    {         
        MessageAdderJob job = new MessageAdderJob(account, database, dbData);
        for (PropertyListener listener : listeners) {
            job.addListener(listener);
        }
        job.start();
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
    
    public Object[][] getMessageTableData(ArrayList<Integer> ids) throws SQLException
    {
        Object[][] messageList = new Object[ids.size()][MESSAGE_LINE_LENGTH];
        for (int i = 0; i < ids.size(); i++)
        {
            int id = ids.get(i);
            ArrayList<Object[]> result = database.selectFromTableWhere("Messages", "*", "messageID=" + id);
            //Get the message from the server
            Object[] line = result.get(0);
            messageList[i] = line; 
        }
        
        return messageList;
    }
    
}
