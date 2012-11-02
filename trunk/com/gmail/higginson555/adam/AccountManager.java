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
    
    public void addAccount(String accountName, String accountPassword) throws SQLException
    {
        String[] fieldNames = {"username", "password"};
        Object[] fieldValues = {accountName, accountPassword};
        
        database.insertRecord("Accounts", fieldNames, fieldValues);
    }
    public void addAccount(Account account) throws SQLException
    {
        String[] fieldNames = {"username", "password"};
        Object[] fieldValues = { account.getUsername(), account.getPassword() };
        
        database.insertRecord("Accounts", fieldNames, fieldValues);        
    }
    
    public ArrayList<Account> getAllAccounts() throws SQLException
    {
        ArrayList<Account> accounts = new ArrayList<Account>();
        
        ArrayList<Object[]> tableSelect = database.selectAllFromTable("Account");
        Iterator<Object[]> tableIter = tableSelect.iterator();
       
        while (tableIter.hasNext())
        {
            Object[] currentLine = tableIter.next();
            Integer accountID = (Integer) currentLine[0];
            String accountName = (String) currentLine[1];
            String accountPassword = (String) currentLine[2];
            
            Account account = new Account(accountID, accountName, accountPassword, null);
            accounts.add(account);
        }
        
        return accounts;       
    }
    
    /**
     * Gets the account ID of a given account name
     * @param accountName The account name to find the id for.
     * @return the account ID of the given account name, or -1 if it cannot be found
     * @throws SQLException 
     */
    public int getAccountID(String accountName) throws SQLException
    {
        String whereSQL = "username='" + accountName + "'";
        ArrayList<Object[]> result = database.selectFromTableWhere("Accounts", "accountID", whereSQL);
        Iterator<Object[]> resultIter = result.iterator();
        int accountID = -1;
        while (resultIter.hasNext())
        {
            Object[] currentLine = resultIter.next();
            accountID = (Integer) currentLine[0];
        }
        
        return accountID;
    }

    public Database getDatabase() {
        return database;
    }
    
    
    
}
