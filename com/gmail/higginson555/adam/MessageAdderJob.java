package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds messages to a database on a seperate thread
 * @author Adam
 */
public class MessageAdderJob extends Thread
{
    //The account these messages belong to
    private Account account;
    //The database to use
    private Database database;
    //The list of stuff to add to the db
    private ArrayList<Object[]> dbData;
    
    private ArrayList<PropertyListener> listeners;

    public MessageAdderJob(Account account, Database database, ArrayList<Object[]> dbData) {
        this.account = account;           
        listeners = new ArrayList<PropertyListener>();
        try {
            //Create a new database connection
            this.database = new Database(database);
            this.database.selectDatabase(database.getSelectedDB());
            this.dbData = dbData;
        } catch (SQLException ex) {
            Logger.getLogger(MessageAdderJob.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MessageAdderJob.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    @Override
    public void run()
    {
        String[] fieldNames = {"messageUID", "subject", "messageFrom", 
                               "messageTo", "dateSent", "dateReceived", "folderID", "accountUsername"};
        
        try
        {
            database.insertRecords("Messages", fieldNames, dbData);



            //Get subject data for each message, and extract out key words
            Iterator<Object[]> dataIter = dbData.iterator();
            while (dataIter.hasNext())
            {
                Object[] currentLine = dataIter.next();
                //Get the id of the inserted message
                ArrayList<Object[]> result = database.selectFromTableWhere("Messages", 
                        "messageID", "messageUID=" + (String)currentLine[0] 
                        + " AND folderID=" + (Integer.toString( (Integer)currentLine[6]) ));
                int id = (Integer) result.get(0)[0];
                //System.out.println("Found id: " + id);
                //Parse the key words from the subject
                String subject = (String)currentLine[1];
                ArrayList<String> keyWords = TagParser.getInstance().getTags(subject);
                //If no key words were found, continue
                if (keyWords == null) {
                    continue;
                }

                Iterator<String> keyWordsIter = keyWords.iterator();

                ArrayList<Object[]> tagDBLines = new ArrayList<Object[]>(keyWords.size());
                //Create array of key words for this message
                while (keyWordsIter.hasNext())
                {
                    String keyWord = keyWordsIter.next();
                    //Only add if it doesn't already exist!
                    if (database.selectFromTableWhere("Tags", "tagID", "tagValue='" + keyWord + "'").isEmpty())
                    {
                        Object[] tagFieldValues = {keyWord};
                        tagDBLines.add(tagFieldValues);     
                        //System.out.println("Added: " + keyWord + " as it doesn't already exist in the table!");
                    }
                    
                }



                String[] tagFieldNames = {"tagValue"};
                database.insertRecords("Tags", tagFieldNames, tagDBLines);

                //get the ids of each key word
                int[] tagIDs = new int[keyWords.size()];
                for (int i = 0; i < keyWords.size(); i++)
                {
                    result = database.selectFromTableWhere("Tags", "tagID" , "tagValue='" + keyWords.get(i) + "'");

                    tagIDs[i] = (Integer) result.get(0)[0];

                }

                //Now update message to tags table
                ArrayList<Object[]> newData = new ArrayList<Object[]>(tagIDs.length);
                for (int i = 0; i < tagIDs.length; i++)
                {
                    Object[] line = {id, tagIDs[i]};
                    newData.add(line);
                }

                String[] messagesToTagsFieldNames = {"messageID", "tagID"};
                database.insertRecords("messagestotags", messagesToTagsFieldNames, newData);
            }
        } //try
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        try {
            database.close();
        } catch (SQLException ex) {
            Logger.getLogger(MessageAdderJob.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("\n\n-------THREAD FINISHED------------");
        publishPropertyEvent("MessageManagerThreadFinished", null);
    }
    

    
    
}
