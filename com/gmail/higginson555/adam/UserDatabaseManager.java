package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.LoadingScreen;
import java.sql.SQLException;

/**
 * A class which provides access to the local user
 * database.
 * @author Adam
 */
public class UserDatabaseManager 
{
    //The database url, username and password
    private String databaseURL, username, password;
    
    public UserDatabaseManager()
    {
        databaseURL = "jdbc:mysql://localhost:3306";
        username = "root";
        password = "";
    }
    
    public Database getDatabaseInstance() throws SQLException, ClassNotFoundException
    {
        Database database = new Database(databaseURL, username, password);
        
        try
        {
            database.selectDatabase("User");
        }
        catch (SQLException ex)
        {
            LoadingScreen ls = new LoadingScreen("Please wait, creating database...");
            ls.setVisible(true);
            //Create the User database
            database.createDatabase("User");
            database.selectDatabase("User");
            //ACCOUNT TABLE
            String accountTableSQL = "accountID int NOT NULL AUTO_INCREMENT,"
                        + "username varchar(255) NOT NULL,"
                        + "password varchar(255) NOT NULL,"
                        + "PRIMARY KEY(accountID)";
            database.createTable("Accounts", accountTableSQL);
            //FOLDER TABLE
            String folderTableSQL = "folderID int NOT NULL AUTO_INCREMENT,"
                                  + "name varchar(255) NOT NULL,"
                                  + "accountID int NOT NULL,"
                                  + "parentFolder int,"
                                  + "lastMessage TIMESTAMP NULL DEFAULT NULL,"
                                  + "PRIMARY KEY(folderID),"
                                  + "FOREIGN KEY(accountID) REFERENCES accounts(accountID),"
                                  + "FOREIGN KEY(parentFolder) REFERENCES folders(folderID)";
            database.createTable("Folders", folderTableSQL);
            //MESSAGE TABLE
            String messageTableSQL = "messageID int NOT NULL AUTO_INCREMENT,"
                                   + "messageUID int NOT NULL,"
                                   + "subject varchar(255),"
                                   + "messageFrom varchar(255),"
                                   + "messageTo varchar(255),"
                                   + "dateSent TIMESTAMP,"
                                   + "dateReceived TIMESTAMP,"
                                   + "folderID int NOT NULL,"
                                   + "PRIMARY KEY(messageID),"
                                   + "FOREIGN KEY(folderID) REFERENCES folders(folderID)";
            database.createTable("Messages", messageTableSQL);
            //database = createNewDatabase(database);
            
            ls.dispose();
        }
        
        return database;
    }
    
    private Database createNewDatabase(Database currentDatabase) throws SQLException
    {

        currentDatabase.createDatabase("User");
        String accountTableSQL = "accountID int NOT NULL AUTO_INCREMENT,"
                                + "username varchar(255) NOT NULL,"
                                + "password varchar(255) NOT NULL,"
                                + "PRIMARY KEY(accountID)";
        currentDatabase.createTable("Account", accountTableSQL);
        
        return currentDatabase;
    }
}
