package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
        String[] fieldNames = {"messageUID", "subject", "messageFrom", 
            "messageTo", "dateSent", "dateReceived", "folderID"};
        
        database.insertRecords("Messages", fieldNames, dbData);
        

        
        //Get subject data for each message, and extract out key words
        Iterator<Object[]> dataIter = dbData.iterator();
        while (dataIter.hasNext())
        {
            Object[] currentLine = dataIter.next();
            //Get the id of the inserted message
            ArrayList<Object[]> result = database.selectFromTableWhere("Messages", "messageID", "messageUID=" + (String)currentLine[0]);
            int id = (Integer) result.get(0)[0];
            //Parse the key words from the subject
            String subject = (String)currentLine[1];
            ArrayList<String> keyWords = TagParser.getInstance().getTags(subject);
            Iterator<String> keyWordsIter = keyWords.iterator();
            while (keyWordsIter.hasNext())
            {
                String currentWord = keyWordsIter.next();
                String tagFieldNames[] = {"tagValue"};
                Object[] tagFieldValues = {keyWordsIter.next()};
                
                //TODO: Finish inserting tags into the DB!
                
            }
        }
        
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
