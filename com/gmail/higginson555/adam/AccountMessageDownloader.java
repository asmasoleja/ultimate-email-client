/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;
import com.gmail.higginson555.adam.view.EmailFilterer;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder;
import javax.mail.URLName;

/**
 * A class which connects and gets all the messages for a particular account,
 * and then adds them to the database.
 * @author Adam
 */
public class AccountMessageDownloader 
{
    private static HashMap<Account, AccountMessageDownloader> instances = new HashMap<Account, AccountMessageDownloader>();
    private Account account;
    private Store store;
    private ArrayList<PropertyListener> listeners;
    
    public static synchronized AccountMessageDownloader getInstance(Account account)
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
    
    private AccountMessageDownloader(Account account)
    {
        this.account = account;
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
    
    /**
     * Gets the messages and begins adding them to the database.
     * Note, adding messages to the database is done on a seperate thread.
     */
    public void getMessages() throws MessagingException, SQLException, MalformedURLException
    {
        connectToServer();
        
        //Try to insert all folders into the database, as well as messages!
        Folder root = store.getDefaultFolder();
        insertFolderIntoDatabase(-1, root);
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
        //Insert this node into the database, with the parentID
        Database user = UserDatabase.getInstance();
        ArrayList<Object[]> result;
        //ParentID -1 when no parent
        if (parentID == -1)
        {
            result = user.selectFromTableWhere("Folders", "folderID", 
                    "name='" + folder.getName() 
                    + "' AND parentFolder IS NULL "
                    + "AND accountUsername='" + account.getUsername() + "'");
        }
        else
        {
            result = user.selectFromTableWhere("Folders", "folderID", 
                "name='" + folder.getName() + 
                "' AND parentFolder=" + Integer.toString(parentID) + 
                " AND accountUsername='" + account.getUsername() + "'");
        }
            
        //If result it empty, folder does not already exist in database, insert it
        if (result.isEmpty())
        {
            //ParentID is -1 when no parent
            if (parentID == -1)
            {
                
                String[] fieldNames = {"name", "accountUsername", "urlname"};
                Object[] fieldValues = {folder.getName(), account.getUsername(), folder.getFullName()};
                user.insertRecord("Folders", fieldNames, fieldValues);
            }
            else //parent found
            {
                String[] fieldNames = {"name", "accountUsername", "parentFolder", "urlname"};
                Object[] fieldValues = {folder.getName(), account.getUsername(), parentID, folder.getFullName()};
                user.insertRecord("Folders", fieldNames, fieldValues);
            }
        }
                
        System.out.println("Now on folder: " + folder.getFullName());
        
        //Find this folder ID
        if (parentID == -1)
        {
            result = user.selectFromTableWhere("Folders", "folderID", 
                "name='" + folder.getName() + 
                "' AND parentFolder IS NULL" +
                " AND accountUsername='" + account.getUsername() + "'");
        }
        else
        {
            result = user.selectFromTableWhere("Folders", "folderID", 
                "name='" + folder.getName() + 
                "' AND parentFolder=" + Integer.toString(parentID) + 
                " AND accountUsername='" + account.getUsername() + "'");
        }
        
        //Should always be full, if not an error has occurred
        //And should only be one result
        int id = (Integer) result.get(0)[0];
        
        //Need to recurse down tree if more folders
        if (folder.getType() == Folder.HOLDS_FOLDERS)
        {
            //Recurse for each child
            Folder[] children = folder.list();
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
        Message[] allMessages = folder.getMessages();
        
        if (allMessages.length == 0)
        {
            return;
        }

        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(UIDFolder.FetchProfileItem.UID);
        //fp.add(FetchProfile.Item.CONTENT_INFO);
        fp.add("X-mailer");

        folder.fetch(allMessages, fp);

        ArrayList<Object[]> dbData = new ArrayList<Object[]>(allMessages.length); 
        for (int i = allMessages.length - 1; i >= 0; i--)
        {
            String subject = "";
            String from = "";
            subject += allMessages[i].getSubject();
            Address[] addresses = allMessages[i].getFrom();
            from += addresses[0].toString();
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
            String UID = "";
            if (account.getAccountType().equalsIgnoreCase("IMAP") || account.getAccountType().equalsIgnoreCase("IMAPS"))
            {
                IMAPFolder imapFolder = (IMAPFolder) folder;
                UID += allMessages[i].getMessageNumber();
                //UID = Long.toString(imapFolder.getUID(allMessages[i]));
            }
            else if (account.getAccountType().equalsIgnoreCase("POP3"))
            {
                POP3Folder pop3Folder = (POP3Folder) folder;
                UID += allMessages[i].getMessageNumber();
                //UID = pop3Folder.getUID(allMessages[i]);
            }    

            Date dateSent = allMessages[i].getSentDate();
            Date dateReceived = allMessages[i].getReceivedDate();
            Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderID, account.getUsername()};
            dbData.add(line);
        }

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
        boolean isUpdatingDB = false;
        while (dataIter.hasNext())
        {
            Object[] currentLine = dataIter.next();
            Date foundDate = (Date) currentLine[5];
            //System.out.println("Found date: " + foundDate);

            //If we've found a message which was sent after the last set
            //date, we need to update the database with the latest message data
            if (lastSetDate == null || foundDate.after(lastSetDate))
            {
                dbDataToAdd.add(currentLine);
                isUpdatingDB = true;
            }
            else
            {
                break;
            }
        }


        if (isUpdatingDB)
        {
            System.out.println("\n----UPDATING DATABASE WITH NEW MESSAGES!----\n");
            fm.setLastDate(folderID, (Date) dbDataToAdd.get(0)[5]);
            publishPropertyEvent("MessageManagerThreadStart", null);
            MessageManager mm = new MessageManager(account, UserDatabase.getInstance());
            for (PropertyListener listener : listeners)
            {
                mm.addListener(listener);
            }
            mm.addMessages(dbDataToAdd);
            System.out.println("\n\n\n\n -------------- RETURNED, THREAD WORKED?! ------------------\n\n\n\n");
        }
        
        folder.close(false);
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
