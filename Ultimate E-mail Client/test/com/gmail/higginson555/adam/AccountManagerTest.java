/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Adam
 */
public class AccountManagerTest {
    
    private Database database;
    public AccountManagerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws SQLException 
    {
        //database.dropDatabase("User");
        database = UserDatabase.getInstance();
    }
    
    @After
    public void tearDown() throws SQLException 
    {
        //database.close();
    }

    /**
     * Test of getSingleton method, of class AccountManager.
     */
    @Test
    public void testGetSingleton() {
        System.out.println("getSingleton");
        AccountManager result = AccountManager.getSingleton();
        assertNotNull("Returned a null singleton variable!", result);
    }

    /**
     * Test of addAccount method, of class AccountManager.
     */
    @Test
    public void testAddAccount_Account() throws Exception {
        System.out.println("addAccount");
        Account account = new Account("TestAccount", "TestPassword", "IMAP", "imapServer", "smtpServer", 587);
        AccountManager instance = AccountManager.getSingleton();
        instance.addAccount(account);
        
        ArrayList<Object[]> dbResult = database.selectFromTableWhere("Accounts", "username", "username='TestAccount'");
        if (dbResult.isEmpty())
        {
            fail("No account added to database!");
        }
        else
        {
            //Clean up
            database.deleteRecord("Accounts", "username='TestAccount'");
        }
    }

    /**
     * Test of getAllAccounts method, of class AccountManager.
     */
    @Test
    public void testGetAllAccounts() throws Exception {
        System.out.println("getAllAccounts");
        AccountManager instance = AccountManager.getSingleton();
        Account account1 = new Account("TestAccount1", "TestPassword", "IMAP", "imapServer", "smtpServer", 587);
        Account account2 = new Account("TestAccount2", "TestPassword", "IMAP", "imapServer", "smtpServer", 587);
        Account account3 = new Account("TestAccount3", "TestPassword", "IMAP", "imapServer4", "smtpServer", 5897);
        instance.addAccount(account1);
        instance.addAccount(account2);
        instance.addAccount(account3);
        int expResult = 3;
        ArrayList<Account> result = instance.getAllAccounts();
        if (result.size() != expResult)
        {
            fail("Not all accounts were added!");
        }
        else
        {
            //Clean up
            database.deleteRecord("Accounts", "username='TestAccount1'");
            database.deleteRecord("Accounts", "username='TestAccount2'");
            database.deleteRecord("Accounts", "username='TestAccount3'");
        }
    }

    /**
     * Test of removeAccount method, of class AccountManager.
     */
    @Test
    public void testRemoveAccount() throws Exception {
        System.out.println("removeAccount");
        AccountManager instance = AccountManager.getSingleton();
        Account account1 = new Account("TestAccount1", "TestPassword", "IMAP", "imapServer", "smtpServer", 587);
        instance.addAccount(account1);
        ArrayList<Object[]> dbResult = database.selectFromTableWhere("Accounts", "username", "username='TestAccount1'");
        if (dbResult.isEmpty())
        {
            fail("Account not added!");
        }
        
        instance.removeAccount(account1);
        dbResult = database.selectFromTableWhere("Accounts", "username", "username='TestAccount1'");
        if (!dbResult.isEmpty())
        {
            fail("Account not deleted!");
        }
        
    }

    /**
     * Test of getAccount method, of class AccountManager.
     */
    @Test
    public void testGetAccount() throws Exception {
        System.out.println("getAccount");
        Account account1 = new Account("TestAccount1", "TestPassword", "IMAP", "imapServer", "smtpServer", 587);
        String username = "TestAccount1";
        AccountManager instance = AccountManager.getSingleton();
        instance.addAccount(account1);
        Account result = instance.getAccount("TestAccount1");
        assertEquals(result.getUsername(), username);
        
        database.deleteRecord("Accounts", "username='TestAccount1'");
    }
}
