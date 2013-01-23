package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
    //All accounts currently loaded, mapping username to account
    private HashMap<String, Account> accounts;
    
    //The singleton
    private static AccountManager instance;
    
    /**
     * Get the instance of the AccountManager
     * @return The singleton AccountManager
     */
    public static synchronized AccountManager getSingleton()
    {
        if (instance == null)
        {
            instance = new AccountManager(UserDatabase.getInstance());
        }
        
        return instance;
    }
    
    private AccountManager(Database database)
    {
        this.database = database;
        this.accounts = new HashMap<String, Account>();
    }
    
    /**
     * 
     * @param accountName
     * @param accountPassword
     * @return The object representing the account created
     * @throws SQLException 
     */
    public Account addAccount(String accountName, String accountPassword) throws SQLException
    {
        Account account = new Account(accountName, accountPassword);
        String[] fieldNames = {"username", "password"};
        Object[] fieldValues = {accountName, accountPassword};
        
        database.insertRecord("Accounts", fieldNames, fieldValues);
        
        accounts.put(accountName, account);
        return account;
    }
    public void addAccount(Account account) throws SQLException
    {
        String[] fieldNames = {"username", "password", "accountType", "incoming", "outgoing", "outgoingPort"};
        Object[] fieldValues = {account.getUsername(), 
                                account.getPassword(), 
                                (String) account.getAccountType(), 
                                account.getIncoming(), 
                                account.getOutgoing(), 
                                account.getOutgoingPort()};        
        database.insertRecord("Accounts", fieldNames, fieldValues);
        
        accounts.put(account.getUsername(), account);
    }
    
    public ArrayList<Account> getAllAccounts() throws SQLException
    {
        ArrayList<Account> accountsList = new ArrayList<Account>();
        
        ArrayList<Object[]> tableSelect = database.selectAllFromTable("Accounts");
        Iterator<Object[]> tableIter = tableSelect.iterator();
       
        while (tableIter.hasNext())
        {
            Object[] currentLine = tableIter.next();
            String accountName = (String) currentLine[0];
            String accountPassword = (String) currentLine[1];
            String accountType = (String) currentLine[2];
            String incoming = (String) currentLine[3];
            String outgoing = (String) currentLine[4];
            int outgoingPort = (Integer) currentLine[5];
            
            Account account = new Account(accountName, accountPassword, accountType, incoming, outgoing, outgoingPort);
            accountsList.add(account);
        }
        
        return accountsList;       
    }
    
    /**
     * DEPRECEATED! Use getAccount instead!
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
    
    public Account getAccount(String username) throws SQLException
    {
        Account account = accounts.get(username);
        if (account == null)
        {
            String whereSQL = "username='" + username + "'";
            ArrayList<Object[]> result = database.selectFromTableWhere("Accounts", "password, accountType, incoming, outgoing, outgoingPort", whereSQL);
            String password = (String) result.get(0)[0];
            String accountType = (String) result.get(0)[1];
            String incoming = (String) result.get(0)[2];
            String outgoing = (String) result.get(0)[3];
            int outgoingPort = (Integer) result.get(0)[4];
            account = new Account(username, password, accountType, incoming, outgoing, outgoingPort);
            accounts.put(username, account);
        }
        
        return account;      
    }

    public Database getDatabase() {
        return database;
    }
    
    
    
}
