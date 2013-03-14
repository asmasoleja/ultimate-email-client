package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;
import com.sun.mail.imap.IMAPFolder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.mail.UIDFolder;
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
    //Listeners
    private ArrayList<PropertyListener> listeners;

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
        this.listeners = new ArrayList<PropertyListener>();
    }
    
    public void addListener(PropertyListener listener)
    {
        listeners.add(listener);
    }
    
    public void addListeners(ArrayList<PropertyListener> listeners)
    {
        listeners.addAll(listeners);
    }
    
    private void publishPropertyEvent(String name, Object value)
    {
        for (PropertyListener listener : listeners)
        {
            listener.onPropertyEvent(this.getClass(), name, value);
        }
    }
    
    
    private void resolveQueue()
    {
        //While queue isn't empty
        while (!ClientThreadPool.findMessageQueue.isEmpty())
        {
            publishPropertyEvent("MessageManagerThreadStart", null);
            //TODO find messages here
            FindMessageQueueItem queueItem = ClientThreadPool.findMessageQueue.remove();
            Object[] messageData = queueItem.getOldMessageData();
            PropertyListener listener = queueItem.getListener();
            String messageID = (String) messageData[1];
            try
            {
                database.updateRecord("Messages", "isValidMessage=0", "messageID='" + messageID + "'");
                //Try and get message from each of the stored locations
                ArrayList<Object[]> result = database.selectFromTableWhere("Messages",
                        "folderID, messageNo", 
                        "messageUID='" + messageID + "' AND isValidMessage=1");
                if (!result.isEmpty())
                {
                    Object[] correctLine = null;
                    Message message = null;
                    int correctFolderID = -1;
                    for (Object[] line : result)
                    {
                        //Just get the first result, don't bother with the rest
                        System.out.println("Result 0: " + result.get(0)[0]);
                        int folderID = (Integer) line[0];
                        long messageNo = (Long) line[1];
                        ArrayList<Object[]> folderResult = database.
                                selectFromTableWhere(
                                "Folders", "urlName", 
                                "folderID=" + Integer.toString(folderID) 
                                + " AND accountUsername='" + account.getUsername() + "'");
                        String folderName = (String) folderResult.get(0)[0];


                        IMAPFolder folder = (IMAPFolder) store.getFolder(folderName);
                        if (!folder.isOpen())
                        {
                            folder.open(Folder.READ_ONLY);
                        }
                        
                        message = folder.getMessageByUID(messageNo);

                        if (message != null)
                        {
                            System.out.println("Message subject: " + message.getSubject());
                            correctLine = line;
                            correctFolderID = folderID;
                            break;
                        }
                    }
                    
                    if (correctLine != null)
                    {
                        //System.out.println("Found correct line in DB Message-ID: " + messageID + " folderID: " + correctFolderID);
                        ClientThreadPool.foundMessages.put(messageID, message);
                        /*result = database.selectFromTableWhere("Messages", 
                                "*", 
                                "messageUID='" + messageID 
                                + "' AND folderID=" + Integer.toString(correctFolderID));*/
                        listener.onPropertyEvent(this.getClass(), "foundMessage", correctLine);
                        continue;
                    }
                }
                
                Folder[] folders = store.getDefaultFolder().list("*");
                SearchTerm searchTerm = new MessageIDTerm(messageID);
                for (Folder currentFolder : folders)
                {
                    if (currentFolder instanceof IMAPFolder 
                            && ((currentFolder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0))
                    {
                        IMAPFolder folder = (IMAPFolder) currentFolder;
                        boolean wasOpenedByUs = false;
                        if (!folder.isOpen())
                        {
                            wasOpenedByUs = true;
                            folder.open(Folder.READ_ONLY);
                        }
                        //TODO check if folder doesn't already exist

                        Message[] messages = folder.search(searchTerm);
                        //System.out.println("Messages length: " + messages.length);
                        if (messages.length != 0)
                        {
                            System.out.println("Found message in: " + folder.getFullName());
                            //Should only be one message for search!
                            Message foundMessage = messages[0];
                            long newSeqNo = folder.getUID(foundMessage);
                            ClientThreadPool.foundMessages.put(messageID, foundMessage);
                            int folderID = (Integer) database.selectFromTableWhere
                                    ("Folders", 
                                    "folderID", 
                                    "urlName='" + folder.getFullName() + "'")
                                    .get(0)[0];
                            database.updateRecord("Messages", 
                                    "isValidMessage=1, folderID=" + Integer.toString(folderID) + ", messageNo=" + Long.toString(newSeqNo), 
                                    "messageUID='" + messageID + "'");

                            result = database.selectFromTableWhere("Messages", "*", "messageUID='" + messageID + "' AND folderID=" + Integer.toString(folderID));
                            Object[] line = result.get(0);
                            listener.onPropertyEvent(this.getClass(), "foundMessage", line);
                            break;
                        }

                        if (wasOpenedByUs)
                        {
                            folder.close(false);
                        }
                    }
                }
            }
            catch (MessagingException ex)
            {
                JOptionPane.showMessageDialog(null, "Can't find message!", "MessagingException", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            catch (SQLException ex)
            {
                JOptionPane.showMessageDialog(null, "Connection to SQL Server Lost!", "SQLException", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            //Need to find message
            publishPropertyEvent("MessageManagerThreadFinished", null);
        }
    }
    
    private void fixFolder(int folderID, IMAPFolder folder) 
            throws SQLException, MessagingException
    {
        publishPropertyEvent("MessageManagerThreadFinished", null);
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
        fp.add("Tags");
        
        Message[] messages = null;
        try
        {
            messages = folder.getMessagesByUID(lastSeqNo, IMAPFolder.LASTUID);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        
        try
        {
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
            fp.add(UIDFolder.FetchProfileItem.UID);
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
            /*Iterator<String> UIDsIter = deletedUIDs.iterator();
            while (UIDsIter.hasNext())
            {
                //"Delete" record
                System.out.println("Deleting valid message");
                //database.updateRecord("Messages", "isValidMessage=0", "messageUID='" + UIDsIter.next() + "'");
            }    */       

            HashSet<String> newMessageSet = new HashSet<String>(newMessages);
            System.out.println("New message set size: " + newMessageSet.size());
            ArrayList<Object[]> dbData = new ArrayList<Object[]>(messages.length); 

            for (int i = 0; i < messages.length; i++)
            {
                //Get UID header
                String[] UIDheader = messages[i].getHeader("Message-Id");
                String UID = UIDheader[0];
                IMAPFolder imapFolder = (IMAPFolder) folder;
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
                    boolean isRead = messages[i].getFlags().contains(Flag.SEEN);
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

                    long messageNo = imapFolder.getUID(messages[i]);


                    String[] tags = messages[i].getHeader("Tags");

                    //Check to see if message already exists in database

                    Date dateSent = messages[i].getSentDate();
                    Date dateReceived = messages[i].getReceivedDate();

                    Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderID, account.getUsername(), messageNo, isRead, tags};
                    dbData.add(line);
                }
            }

            long uid = folder.getUIDNext();
            UserDatabase.getInstance().updateRecord("Folders", 
                    "lastSeqNo=" + Long.toString(uid), 
                    "folderID=" + Integer.toString(folderID));

            insertMessagesWithTags(dbData);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        publishPropertyEvent("MessageManagerThreadFinished", null);
    }
    
    private void insertMessagesWithTags(ArrayList<Object[]> dbData)
    {   
        System.out.println("Inserting data: " + dbData.size());
        String[] fieldNames = {"messageUID", "subject", "messageFrom", 
                               "messageTo", "dateSent", "dateReceived", "folderID", "accountUsername", "messageNo", "isRead"};
        
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
                            "messageID, folderID, messageFrom", "messageUID='" + (String)currentLine[0] + "'");
                    int id = (Integer) result.get(0)[0];
                    int folderID = (Integer) result.get(0)[1];
                    String from = (String) result.get(0)[2];
                    
                    if (TrustedAccount.isTrustedAccount(account, from))
                    {
                        String[] tags = (String[]) currentLine[currentLine.length - 1];
                        if (tags != null)
                        {
                            ArrayList<String> tagList = new ArrayList<String>(tags.length);
                            tagList.addAll(Arrays.asList(tags));
                            TagParser.getInstance().insertTags(UserDatabase.getInstance(), tagList, id);
                        }
                    }

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
        System.out.println("Started update thread for: " + account);
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
