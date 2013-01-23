/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.view.EmailFilterer;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import java.awt.Cursor;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

/**
 * A class which connects and gets all the messages for a particular account,
 * and then adds them to the database.
 * @author Adam
 */
public class AccountMessageDownloader 
{
    private Account account;
    private Store store;
    
    public AccountMessageDownloader(Account account)
    {
        this.account = account;
    }
    
    /**
     * Gets the messages and begins adding them to the database.
     * Note, adding messages to the database is done on a seperate thread.
     */
    public void getMessages()
    {
        connectToServer();
        try 
        {
            
            Folder[] folders = store.getDefaultFolder().list();
            for (Folder folder : folders)
            {
                int folderID = -1;
                if (!folder.isOpen())
                {
                    folder.open(Folder.READ_WRITE);
                }
                    
                //Add this folder to the database, working out its proper parent path!
                folderID = addFolder(folder);

                //Get all messages in this folder
                Message[] allMessages = folder.getMessages();

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
                    subject = allMessages[i].getSubject();
                    Address[] addresses = allMessages[i].getFrom();
                    from = addresses[0].toString();
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
                        UID = Long.toString(imapFolder.getUID(allMessages[i]));
                    }
                    else if (account.getAccountType().equalsIgnoreCase("POP3"))
                    {
                        POP3Folder pop3Folder = (POP3Folder) folder;
                        UID = pop3Folder.getUID(allMessages[i]);
                    }
                    String date = "";
                    Boolean isRead = true;
                    date = allMessages[i].getSentDate().toString();
                    isRead = allMessages[i].isSet(Flags.Flag.SEEN);        

                    Date dateSent = allMessages[i].getSentDate();
                    Date dateReceived = allMessages[i].getReceivedDate();
                    Object[] line = {UID, subject, from, to, dateSent, dateReceived, folderID};
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
                System.out.println("\nLast set date: " + lastSetDate);

                Iterator<Object[]> dataIter = dbData.iterator();
                ArrayList<Object[]> dbDataToAdd = new ArrayList<Object[]>();
                boolean isUpdatingDB = false;
                while (dataIter.hasNext())
                {
                    Object[] currentLine = dataIter.next();
                    Date foundDate = (Date) currentLine[5];
                    System.out.println("Found date: " + foundDate);

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
                    MessageManager mm = new MessageManager(UserDatabase.getInstance());
                    mm.addMessages(dbDataToAdd);
                    System.out.println("\n\n\n\n -------------- RETURNED, THREAD WORKED?! ------------------\n\n\n\n");
                }
            }
        }
        catch (MessagingException ex) {
            Logger.getLogger(AccountMessageDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }
        
    }
    
    private void connectToServer()
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
        try 
        {
            store = session.getStore(account.getAccountType().toLowerCase());
            store.connect(account.getIncoming(), account.getUsername(), decPass);
        } catch (Exception ex) {
            Logger.getLogger(EmailFilterer.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
    }
    
    private int addFolder(Folder folder)
    {
        int folderID = -1;
                    
        FolderManager folderManager = new FolderManager(UserDatabase.getInstance());

        //Find out folder ids, and add them to database
        Folder parent = null;
        try 
        {
            parent = folder.getParent();
            if (folder.getParent() == null || folder.getParent().getName().isEmpty())
            {
                folderID = folderManager.addFolder(folder.getName(), account);
            }
            else //Folder has parent, work out parent tree!
            {
                ArrayList<String> folderStack = new ArrayList<String>();
                while (parent != null && !parent.getName().isEmpty())
                {
                    folderStack.add(parent.getName());
                    System.out.println("Adding: " + parent.getName() + "@ " + folderStack.lastIndexOf(parent.getName()));
                    parent = parent.getParent();
                }

                Collections.reverse(folderStack);
                String[] parentFolderList = folderStack.toArray(new String[folderStack.size()]);
                folderID = folderManager.addFolder(folder.getName(), parentFolderList, account);
            }
        } 
        catch (MessagingException ex) 
        {
            Logger.getLogger(FolderNode.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            System.exit(-1);
        }

        return folderID;
    }
    
}
