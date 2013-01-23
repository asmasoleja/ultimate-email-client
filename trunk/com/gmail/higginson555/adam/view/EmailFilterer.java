package com.gmail.higginson555.adam.view;

import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.ProtectedPassword;
import com.gmail.higginson555.adam.UserDatabase;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * A class which filters all e-mails by a given view.
 * Has a collection of all messages for a given account
 * @author Adam
 */
public class EmailFilterer 
{
    //A mapping of an account to an e-mail filterer
    private static HashMap<Account, EmailFilterer> filterers;
    
    public static synchronized EmailFilterer getSingleton(Account account)
    {
        if (filterers.containsKey(account))
        {
            return filterers.get(account);
        }
        
        EmailFilterer filterer = new EmailFilterer();
        filterers.put(account, filterer);
        return filterer;
    }
    
    //The account for the e-mail filterer
    private Account account;
    
    private EmailFilterer(Account account)
    {
        connectToServer();
    }
    
    private void connectToServer()
    {

    }
    
}
