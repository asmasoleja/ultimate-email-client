package com.gmail.higginson555.adam.view;

import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.UserDatabase;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A class which filters all e-mails by a given view.
 * Has a collection of all messages for a given account
 * @author Adam
 */
public class EmailFilterer 
{
    //A mapping of an account to an e-mail filterer
    private static HashMap<Account, EmailFilterer> filterers = new HashMap<Account, EmailFilterer>();
    
    /**
     * Get an instance of an EmailFilterer for a specific account.
     * @param account
     * @return 
     */
    public static synchronized EmailFilterer getInstance(Account account)
    {
        if (filterers.containsKey(account))
        {
            return filterers.get(account);
        }
        
        EmailFilterer filterer = new EmailFilterer(account);
        filterers.put(account, filterer);
        return filterer;
    }
    
    //The account for the e-mail filterer
    private Account account;
    
    private EmailFilterer(Account account)
    {
        this.account = account;
    }
    
    /**
     * Get the data back in a format suitable for the 4 column
     * JTable. Connects to the database each time this is called,
     * so make take a lot of time!
     * @param view The view used to filter data
     * @return An ArrayList, where each element corresponds to
     *         a row in the table.
     */
    public ArrayList<Object[]> getTableData(View view) throws SQLException
    {
        System.out.println("Getting table data!");
        Database user = UserDatabase.getInstance();
        ArrayList<String> keyWords = view.getKeyWords();
        System.out.println("Key words no: " + keyWords.size());
        
        //All ids already found
        HashSet<Integer> foundIDs = new HashSet<Integer>();
        //The return list
        ArrayList<Object[]> returnList = new ArrayList<Object[]>();
        //For each key word, get the ids
        for (String keyWord : keyWords)
        {
            System.out.println("Key word: " + keyWord);
            //Get id of key word from tag table
            ArrayList<Object[]> result = user.selectFromTableWhere("Tags", "tagID", "tagValue = '" + keyWord + "'");
            
            if (!result.isEmpty())
            {
                String tagID = Integer.toString((Integer) result.get(0)[0]);
                //Tag exists, so message for that tag should also exist (if not deleted by mistake)
                //Select message ids from message to tags
                result = user.selectFromTableWhere("MessagesToTags", "messageID", "tagID = " + tagID);
                //Go through, selecting messages where id = this
                for (Object[] line : result)
                {
                    int messageID = (Integer) line[0];
                    System.out.println("Message iD found: " + messageID);
                    //Only continue if message not already added
                    if (!foundIDs.contains(messageID))
                    {
                        result = user.selectFromTableWhere("Messages", "*", "messageID = " + Integer.toString(messageID));
                        //Get the account username of the message
                        String accountUsername = (String) result.get(0)[8];
                        //Check if it is the same username!
                        if (accountUsername.equalsIgnoreCase(account.getUsername()))
                        {
                            returnList.add(result.get(0));
                            foundIDs.add(messageID);
                        }
                    }
                }   
            }        
        }
        
        return returnList;
    }
    
    
}
