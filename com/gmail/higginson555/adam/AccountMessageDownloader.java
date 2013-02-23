/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;
import com.gmail.higginson555.adam.view.EmailFilterer;
import com.sun.mail.imap.DefaultFolder;
import com.sun.mail.imap.IMAPFolder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Address;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * A class which connects and gets all the messages for a particular account,
 * and then adds them to the database.
 * @author Adam
 */
public class AccountMessageDownloader extends Thread
{
    private static HashMap<Account, AccountMessageDownloader> instances = new HashMap<Account, AccountMessageDownloader>();
    private Account account;
    private Store store;
    private ArrayList<PropertyListener> listeners;
    
    //In case we were in the middle of updating last time
    private int lastFolderID;
    private int lastMessageID;
    
    public static synchronized AccountMessageDownloader getInstance(Account account) 
            throws SQLException, MessagingException
    {
        if (instances.containsKey(account))
        {
            return instances.get(account);
        }
        else
        {
            AccountMessageDownloader newInstance = new AccountMessageDownloader(account);
            instances.put(account, newInstance);
            return newInstance;
        }
    }
    
    private AccountMessageDownloader(Account account) throws SQLException, MessagingException
    {
        this.account = account;
        connectToServer();
        ArrayList<Object[]> result = UserDatabase.getInstance().
                selectFromTableWhere("AccountMessageDownloaders", "accountDownloaderID",
                "accountUsername='" + account.getUsername() + "'");
        
        //Insert empty data to db 
        if (result.isEmpty())
        {
            String[] fields = {"accountUsername"};
            Object[] data = {account.getUsername()};
            UserDatabase.getInstance().insertRecord("AccountMessageDownloaders", fields, data);
            this.lastFolderID = -1;
            this.lastMessageID = -1;
        }
        
        this.listeners = new ArrayList<PropertyListener>();
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
        try {
            getMessages();
        } catch (MessagingException ex) {
            Logger.getLogger(AccountMessageDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(AccountMessageDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(AccountMessageDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Gets the messages and begins adding them to the database.
     * Note, adding messages to the database is done on a seperate thread.
     */
    private void getMessages() throws MessagingException, SQLException, MalformedURLException
    {
        ArrayList<Object[]> result = UserDatabase.getInstance().selectFromTableWhere
                ("AccountMessageDownloaders", 
                "isDone", "accountUsername='" + account.getUsername() + "'");
        
        boolean isDone = (Boolean) result.get(0)[0];
        System.out.println("IS DONEa: " + isDone);
        if (!isDone)
        {

            System.out.println("After if!");
            //Try to insert all folders into the database, as well as messages!
            Folder folder = null;
            int parent;
            System.out.println("Getting default folder! Is store connected: " + store.isConnected());
            folder = store.getDefaultFolder();
            parent = -1;
            System.out.println("About to insert folders");
            insertFolderIntoDatabase(parent, folder);
            //Done?
            UserDatabase.getInstance().updateRecord("AccountMessageDownloaders", 
                    "isDone=1", "accountUsername='" + account.getUsername() + "'");
        }
        //Start the account updater thread
        AccountMessageUpdater updater = new AccountMessageUpdater(store, account, UserDatabase.getInstance());
        ClientThreadPool.executorService.submit(updater);
    }      
    
    public Message getMessageWithID(int folderID, int messageUID) throws MessagingException, SQLException, MalformedURLException
    {
        //Get folder url
        ArrayList<Object[]> result = UserDatabase.getInstance().selectFromTableWhere("Folders", "urlname", "folderID=" + Integer.toString(folderID));
        
        if (result.isEmpty()) {
            throw new SQLException("Folder not found in database! FolderID given: " + folderID);
        }
        
        String folderName = (String) result.get(0)[0];
        //Access folder
        Folder foundFolder = store.getFolder(folderName);
        
        if (!foundFolder.isOpen()) {
            foundFolder.open(Folder.READ_ONLY);
        }
        
        Message foundMessage = foundFolder.getMessage(messageUID);
        
        return foundMessage;
    }
    
    
    /*
     * Recursive function to insert folders into the database.
     * parentID - The ID of the current folder's parent, -1 if no parent
     * folder - The current folder
     */
    private void insertFolderIntoDatabase(int parentID, Folder folder) throws MessagingException,
                                                    SQLException,
                                                    MalformedURLException
    {
        if (ClientThreadPool.shouldStop)
        {
            return;
        }
        
        System.out.println("Inserting folder!");
        
        //Insert this node into the database, with the parentID
        Database user = UserDatabase.getInstance();
        ArrayList<Object[]> result;
        result = user.selectFromTableWhere("Folders", "folderID", 
                "urlName='" + folder.getFullName() + "'");
            
        //If result it empty, folder does not already exist in database, insert it
        if (result.isEmpty())
        {
            //ParentID is -1 when no parent
            if (parentID == -1)
            {
                if (folder instanceof IMAPFolder && !(folder instanceof DefaultFolder))
                {
                    System.out.println("Folder class: " + folder.getClass().getName());
                    IMAPFolder imapFolder = (IMAPFolder) folder;
                    String[] fieldNames = {"name", 
                                           "accountUsername", 
                                           "urlName", 
                                           "uidValidity"};
                    Object[] fieldValues = {imapFolder.getName(), 
                                            account.getUsername(), 
                                            imapFolder.getFullName(), 
                                            imapFolder.getUIDValidity()};
                    user.insertRecord("Folders", fieldNames, fieldValues);
                }
                else
                {
                    String[] fieldNames = {"name",
                                           "accountUsername",
                                           "urlName"};
                    Object[] fieldValues = {folder.getName(),
                                            account.getUsername(),
                                            folder.getFullName()};
                    user.insertRecord("Folders", fieldNames, fieldValues);
                }
            }
            else //parent found
            {
                if (folder instanceof IMAPFolder && ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0))
                {
                    IMAPFolder imapFolder = (IMAPFolder) folder;
                    String[] fieldNames = {"name", 
                                           "accountUsername", 
                                           "parentFolder", 
                                           "urlname", 
                                           "uidValidity"};
                    Object[] fieldValues = {imapFolder.getName(),
                                            account.getUsername(), 
                                            parentID, 
                                            imapFolder.getFullName(), 
                                            imapFolder.getUIDValidity()};
                    user.insertRecord("Folders", fieldNames, fieldValues);
                }
                else
                {
                    String[] fieldNames = {"name", 
                                           "accountUsername", 
                                           "parentFolder", 
                                           "urlname"};
                    Object[] fieldValues = {folder.getName(),
                                            account.getUsername(), 
                                            parentID, 
                                            folder.getFullName()};
                    user.insertRecord("Folders", fieldNames, fieldValues);
                }
            }
        }
                
        System.out.println("Now on folder: " + folder.getFullName());
        
        //Find this folder ID
        result = user.selectFromTableWhere("Folders", "folderID", 
                "urlName='" + folder.getFullName() + "'");
        
        //Should always be full, if not an error has occurred
        //And should only be one result
        int id = (Integer) result.get(0)[0];
        
        //Need to recurse down tree if more folders
        if (folder.getType() == Folder.HOLDS_FOLDERS)
        {
            //Recurse for each child
            Folder[] children = folder.list("*");
            for (Folder child : children)
            {
                insertFolderIntoDatabase(id, child);
            }  
        }
        
        insertMessagesInFolder(id, folder);
    }
    
    private void insertMessagesInFolder(int folderID, Folder folder) 
            throws MessagingException, 
                   SQLException
    {
        if ((folder.getType() != Folder.HOLDS_FOLDERS) && !folder.isOpen())
        {
            folder.open(Folder.READ_ONLY);
        }
        else {
            return;
        }
        
        //Get all messages in this folder
        Message[] allMessages;
        if (lastMessageID == -1)
        {
            allMessages = folder.getMessages();
        }
        else    
        {
            ArrayList<Object[]> result = UserDatabase.getInstance().selectFromTableWhere("Messages", "messageNo", "messageID=" + Integer.toString(lastMessageID));
            int messageNo = (Integer) result.get(0)[0];
            int lastMessage = folder.getMessageCount();
            //TODO, what if message not found?!
            allMessages = folder.getMessages(messageNo, lastMessage);
        }
        
        if (allMessages.length == 0)
        {
            return;
        }

        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add("Message-ID");
        //fp.add(FetchProfile.Item.CONTENT_INFO);
        fp.add("X-mailer");

        folder.fetch(allMessages, fp);

        System.out.append("Inserting messages");
        int maxMessageNo = -1;
        ArrayList<Object[]> dbData = new ArrayList<Object[]>(allMessages.length); 
        for (int i = allMessages.length - 1; i >= 0; i--)
        {
            String subject = "";
            String from = "";
            subject += allMessages[i].getSubject();
            Address[] addresses = allMessages[i].getFrom();
            if (addresses.length != 0) {
                from += addresses[0].toString();
            }
            String to = "";
            //This bit here is slow for some reason?
            /*addresses = allMessages[i].getAllRecipients();
            if (addresses != null)
            {
                for (int j = 0; j < addresses.length - 1; j++)
                {
                    to += addresses[j].toString() + ",";
                }
                //So we don't add a comma at the end
                to += addresses[addresses.length - 1];
            }*/
            
            //Get UID header
            String[] UIDheader = allMessages[i].getHeader("Message-Id");
            String UID = UIDheader[0];
            System.out.println("Found UID: " + UID);
            
            
            int messageNo = allMessages[i].getMessageNumber();
            

            //Check to see if message already exists in database

            Date dateSent = allMessages[i].getSentDate();
            Date dateReceived = allMessages[i].getReceivedDate();
            
            Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderID, account.getUsername(), messageNo};
            dbData.add(line);
        }
        
        IMAPFolder imapFolder = (IMAPFolder) folder;
        long uid = imapFolder.getUIDNext();
        UserDatabase.getInstance().updateRecord("Folders", 
                "lastSeqNo=" + Long.toString(uid), 
                "folderID=" + Integer.toString(folderID));
        

        Comparator<Object[]> messageDataComp = new Comparator<Object[]>()
        {

            @Override
            public int compare(Object[] o1, Object[] o2) 
            {
                Date d1 = (Date) o1[5];
                Date d2 = (Date) o2[5];

                return d2.compareTo(d1);
            }

        };

        Collections.sort(dbData, messageDataComp);

        FolderManager fm = new FolderManager(UserDatabase.getInstance());
        Date lastSetDate = fm.getLastDate(folderID);
        //System.out.println("\nLast set date: " + lastSetDate);

        Iterator<Object[]> dataIter = dbData.iterator();
        ArrayList<Object[]> dbDataToAdd = new ArrayList<Object[]>();
        boolean isUpdatingDB = true;
        while (dataIter.hasNext())
        {
            Object[] currentLine = dataIter.next();
            Date foundDate = (Date) currentLine[5];
            //System.out.println("Found date: " + foundDate);

            //If we've found a message which was sent after the last set
            //date, we need to update the database with the latest message data
            //if (lastSetDate == null || foundDate.after(lastSetDate))
            //{
                dbDataToAdd.add(currentLine);
                isUpdatingDB = true;
            //}
            /*else
            {
                break;
            }*/
        }
        
        System.out.println(isUpdatingDB + " updating DB?");


        if (isUpdatingDB)
        {
            System.out.println("\n----UPDATING DATABASE WITH NEW MESSAGES!----\n");
            //fm.setLastDate(folderID, (Date) dbDataToAdd.get(0)[5]);
            publishPropertyEvent("MessageManagerThreadStart", null);
            /*MessageManager mm = new MessageManager(account, UserDatabase.getInstance());
            for (PropertyListener listener : listeners)
            {
                mm.addListener(listener);
            }
            mm.addMessages(dbDataToAdd);*/
            insertMessageWithTags(dbDataToAdd);
            //Now insert all headers
            if (!ClientThreadPool.shouldStop)
            {
                //ClientThreadPool.executorService.submit(new MessageBodyTagDownloader(folder, folderID));
            }
            else
            {
                return;
            }
            //System.out.println("\n\n\n\n -------------- RETURNED, THREAD WORKED?! ------------------\n\n\n\n");
        }
        
        folder.close(false);
    }
    
    private void insertMessageWithTags(ArrayList<Object[]> dbData)
    {
        Database database = UserDatabase.getInstance();
        
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
                        result = database.selectFromTableWhere("MessagesToTags", 
                                "messageID", 
                                "messageID=" + Integer.toString(id) 
                                + " AND tagID=" + Integer.toString(tagIDs[i]));
                        if (result.isEmpty())
                        {
                            Object[] line = {id, tagIDs[i]};
                            System.out.println("Adding message: " + id + " and tag: " + tagIDs[i]);
                            newData.add(line);
                        }
                    }

                    String[] messagesToTagsFieldNames = {"messageID", "tagID"};
                    database.insertRecords("MessagesToTags", messagesToTagsFieldNames, newData);
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
    
    private void connectToServer() throws MessagingException
    {
        //Decrypt password
        String decPass = "";
        try {
            decPass += ProtectedPassword.decrypt(account.getPassword());
        } catch (GeneralSecurityException ex) {
            Logger.getLogger(EmailFilterer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(EmailFilterer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Properties properties = System.getProperties();
        properties.setProperty("mail.store.protocol", account.getAccountType());
        
        Session session = Session.getInstance(properties);
        store = session.getStore(account.getAccountType().toLowerCase());
        store.connect(account.getIncoming(), account.getUsername(), decPass);
    }        
}
