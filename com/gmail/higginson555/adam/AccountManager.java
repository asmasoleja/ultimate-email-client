package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class which manages all the accounts a user has.
 * It stores the account information in the local user database
 * @author Adam
 */
public class AccountManager 
{
    //The database used to connect to
    private Database database;
    
    public AccountManager(Database database)
    {
        this.database = database;
    }
    
    public void addAccount(Account account) throws SQLException
    {
        String[] fieldNames = {"username", "password"};
        Object[] fieldValues = { account.getUsername(), account.getPassword() };
        
        database.insertRecord("Account", fieldNames, fieldValues);        
    }
    
    public ArrayList<Account> getAllAccounts() throws SQLException
    {
        ArrayList<Account> accounts = new ArrayList<Account>();
        
        ArrayList<Object[]> tableSelect = database.selectAllFromTable("Account");
        Iterator<Object[]> tableIter = tableSelect.iterator();
       
        while (tableIter.hasNext())
        {
            Object[] currentLine = tableIter.next();
            String accountName = (String) currentLine[1];
            String accountPassword = (String) currentLine[2];
            
            Account account = new Account(accountName, accountPassword, null);
            accounts.add(account);
        }
        
        return accounts;       
    }
    
}
