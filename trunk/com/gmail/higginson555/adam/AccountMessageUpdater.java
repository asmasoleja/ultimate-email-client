package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.swing.JOptionPane;

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



    public boolean checkAllMessages()
    {
        boolean wasOK = true;
        try
            {
            //Go through each folder in the database for this account
            ArrayList<Object[]> result = database.selectFromTableWhere("Folders",
                    "folderID, urlName",
                    "accountUsername='" + account.getUsername() + "'");

            //No folders found
            if (result.isEmpty())
            {
                return true;
            }

            for (Object[] line : result)
            {
                int folderID = (Integer) line[0];
                String url = (String) line[1];
                boolean areMessagesOK = checkMessagesInFolder(folderID, url);
                if (!areMessagesOK)
                {
                    wasOK = false;
                    rebuildFolder(folderID);
                }
            }
        }
        catch (Exception ex)
        {
            wasOK = false;
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
        }

        return wasOK;
    }

    private boolean checkMessagesInFolder(int folderID, String url) throws MessagingException, SQLException
    {
        //Get folder
        Folder folder = store.getFolder(url);
        ArrayList<Object[]> result
                = database.selectFromTableWhere("Messages",
                "messageNo, messageUID", "folderID=" + Integer.toString(folderID));

        //No messages found in folder, just return
        if (result.isEmpty())
        {
            return true;
        }

        int[] messageNos = new int[result.size()];
        String[] UIDs = new String[result.size()];
        for (int i = 0; i < messageNos.length; i++)
        {
            Object[] line = result.get(i);

            int messageNo = (Integer) line[0];
            String UID = (String) line[1];
            messageNos[i] = messageNo;
            UIDs[i] = UID;
        }


        if (!folder.isOpen())
        {
            folder.open(Folder.READ_ONLY);
        }

        Message[] messages = folder.getMessages(messageNos);
        FetchProfile fp = new FetchProfile();
        fp.add("Message-Id");
        folder.fetch(messages, fp);

        for (int i = 0; i < messages.length; i++)
        {
            Message message = messages[i];
            //Get UID
            String[] UIDheader = message.getHeader("Message-Id");
            String UID = UIDheader[0];
            //If UID not equal, some message has changed in this folder
            if (!UID.equalsIgnoreCase(UIDs[i]))
            {
                System.out.println("Warning: found conflicting UIDs! Found: " + UID + " Current: " + UIDs[i]);
                return false;
            }
        }

        return true;
    }

    private void rebuildFolder(int folderID)
    {
        
    }

    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
