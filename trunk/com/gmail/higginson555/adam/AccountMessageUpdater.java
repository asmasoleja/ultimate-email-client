package com.gmail.higginson555.adam;

import com.sun.mail.imap.IMAPFolder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.SearchTerm;
import javax.swing.JOptionPane;
import org.apache.commons.collections.ListUtils;

/**
 * A class to check whether the e-mail messages of an account are in
 * the correct place
 * @author higgina0
 */
public class AccountMessageUpdater implements Runnable
{
    //The account this represents
    private Account account;
    //The database to use
    private Database database;
    //The message store
    private Store store;

    /**
     *
     * @param store The message store to use, should be connected
     * @param account The account to update messages for
     * @param database The database to get messages for
     */
    public AccountMessageUpdater(Store store, Account account, Database database)
    {
        this.account = account;
        this.database = database;
        this.store = store;
    }
    
    
    private void resolveQueue()
    {
        //While queue isn't empty
        while (!ClientThreadPool.findMessageQueue.isEmpty())
        {
            //TODO find messages here
            Object[] messageData = ClientThreadPool.findMessageQueue.remove();
            String messageID = (String) messageData[1];
            try
            {
                Folder[] folders = store.getDefaultFolder().list("*");
                SearchTerm searchTerm = new MessageIDTerm(messageID);
                for (Folder folder : folders)
                {
                    boolean wasOpenedByUs = false;
                    if (!folder.isOpen())
                    {
                        wasOpenedByUs = true;
                        folder.open(Folder.READ_ONLY);
                    }
                    String folderName = folder.getFullName();
                    //TODO check if folder doesn't already exist

                    Message[] message = folder.search(searchTerm);
                    if (message.length != 0)
                    {
                        
                    }
                    
                    if (wasOpenedByUs)
                    {
                        folder.close(false);
                    }
                }
                int folderID = (Integer) messageData[7];
            }
            catch (MessagingException ex)
            {
                JOptionPane.showMessageDialog(null, "Can't find message!", "MessagingException", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            
            //Need to find message
        }
    }
    
    private void fixFolder(int folderID, IMAPFolder folder) 
            throws SQLException, MessagingException
    {
        if (!folder.isOpen())
        {
            folder.open(Folder.READ_ONLY);
        }
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                "lastSeqNo", "folderID=" + Integer.toString(folderID));
        long lastSeqNo = (Long) result.get(0)[0];
        
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("Message-ID");
        //fp.add(FetchProfile.Item.CONTENT_INFO);
        fp.add("X-mailer");
        
        Message[] messages = null;
        try
        {
            messages = folder.getMessagesByUID(lastSeqNo, IMAPFolder.LASTUID);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        //Scan through folder for deleted messages
        //Our data
        result = database.selectFromTableWhere("Messages", 
          "messageUID", "accountUsername='" + account.getUsername() + "' AND isValidMessage=1");

        ArrayList<String> allUIDs = new ArrayList<String>(result.size());
        HashSet<String> deletedUIDs = new HashSet<String>(result.size());
        for (Object[] line : result)
        {
            String UID = (String) line[0];
            deletedUIDs.add(UID);
            allUIDs.add(UID);
        }

        //Get all messages now
        messages = folder.getMessages();
        FetchProfile idFP = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("Message-Id");
        folder.fetch(messages, idFP);
        //Need to find the difference in two lists 
        //(things in first not in 2nd) i.e deleted messages
        ArrayList<String> serverUIDs = new ArrayList<String>(messages.length);
        for (int i = 0; i < messages.length; i++)
        {
            Message message = messages[i];
            String[] UIDarray = message.getHeader("Message-Id");
            String UID = UIDarray[0];
            serverUIDs.add(UID);
            //Remove the UID if it's already there
            deletedUIDs.remove(UID);
        }
        
        //Things in second list not in first
        Collection<String> newMessages = ListUtils.subtract(serverUIDs, allUIDs);

        //UIDs should now just contain messages that wern't found on server
        Iterator<String> UIDsIter = deletedUIDs.iterator();
        while (UIDsIter.hasNext())
        {
            //"Delete" record
            database.updateRecord("Messages", "isValidMessage=0", "messageUID='" + UIDsIter.next() + "'");
        }           
        
        HashSet<String> newMessageSet = new HashSet<String>(newMessages);
        System.out.println("New message set size: " + newMessageSet.size());
        ArrayList<Object[]> dbData = new ArrayList<Object[]>(messages.length); 

        for (int i = 0; i < messages.length; i++)
        {
            //Get UID header
            String[] UIDheader = messages[i].getHeader("Message-Id");
            String UID = UIDheader[0];
            
            if (newMessageSet.contains(UID))
            {
                String subject = "";
                String from = "";
                subject += messages[i].getSubject();
                Address[] addresses = messages[i].getFrom();
                if (addresses.length != 0) {
                    from += addresses[0].toString();
                }
                String to = "";
                //This bit here is slow for some reason?
                /*addresses = messages[i].getAllRecipients();
                if (addresses != null)
                {
                    for (int j = 0; j < addresses.length - 1; j++)
                    {
                        to += addresses[j].toString() + ",";
                    }
                    //So we don't add a comma at the end
                    to += addresses[addresses.length - 1];
                }*/            

                int messageNo = messages[i].getMessageNumber();


                //Check to see if message already exists in database

                Date dateSent = messages[i].getSentDate();
                Date dateReceived = messages[i].getReceivedDate();

                Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderID, account.getUsername(), messageNo};
                dbData.add(line);
            }
        }
        
        long uid = folder.getUIDNext();
        UserDatabase.getInstance().updateRecord("Folders", 
                "lastSeqNo=" + Long.toString(uid), 
                "folderID=" + Integer.toString(folderID));
        
        insertMessagesWithTags(dbData);
        
        
    }
    
    private void insertMessagesWithTags(ArrayList<Object[]> dbData)
    {   
        System.out.println("Inserting data: " + dbData.size());
        String[] fieldNames = {"messageUID", "subject", "messageFrom", 
                               "messageTo", "dateSent", "dateReceived", "folderID", "accountUsername", "messageNo"};
        
        try
        {
            database.insertRecords("Messages", fieldNames, dbData);



            //Get subject data for each message, and extract out key words, while we should keep going
            Iterator<Object[]> dataIter = dbData.iterator();
            while (dataIter.hasNext())
            {
                if (!ClientThreadPool.shouldStop)
                {
                    Object[] currentLine = dataIter.next();
                    //Get the id of the inserted message
                    ArrayList<Object[]> result = database.selectFromTableWhere("Messages", 
                            "messageID, folderID", "messageUID='" + (String)currentLine[0] + "'");
                    int id = (Integer) result.get(0)[0];
                    int folderID = (Integer) result.get(0)[1];

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
                    database.insertRecords("MessagesToTags", messagesToTagsFieldNames, newData);

                    //Update last set message
                    //System.out.println("Setting new data!");
                }
                else
                {
                    return;
                }
            }
        } //try
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
    @Override
    public void run() 
    {
        System.out.println("Started update thread");
        try
        {
            while (true)
            {
                if (ClientThreadPool.shouldStop)
                {
                    return;
                }
                
                Thread.sleep(500);

                resolveQueue();
                //Check all folders to see if any changes
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                        "*", "accountUsername='" + account.getUsername() + "'");
                
                for (Object[] folderLine : result)
                {
                    long lastSeqNo;
                    if (folderLine[7] == null)
                    {
                        continue;
                    }
                    
                    lastSeqNo = (Long) folderLine[7];
                    //Get folder on server
                    String longName = (String) folderLine[5];
                    IMAPFolder folder = (IMAPFolder) store.getFolder(longName);
                    long newSeqNo = folder.getUIDNext();
                    
                    if (newSeqNo != lastSeqNo)
                    {
                        System.out.println("Found difference in validities for folder: "
                                + longName 
                                + " old: " + newSeqNo
                                + " new: " + lastSeqNo);
                        
                        int folderID = (Integer) folderLine[0];
                        fixFolder(folderID, folder);
                        System.out.println("Sorted...");
                    }                                          
                }
            } //while true
        } 
        catch (SQLException ex)
        {
            JOptionPane.showMessageDialog(null, "Could not connect to sql server!", "SQL Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (MessagingException ex)
        {
            JOptionPane.showMessageDialog(null, "Could not connect to IMAP server! " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        catch (InterruptedException ex)
        {
            JOptionPane.showMessageDialog(null, "Thread Exception! " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
                
                
    }
}
