package com.gmail.higginson555.adam;

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
            //Create the User database
            database.createDatabase("User");
            database.selectDatabase("User");
            String accountTableSQL = "accountID int NOT NULL AUTO_INCREMENT,"
                        + "username varchar(255) NOT NULL,"
                        + "password varchar(255) NOT NULL,"
                        + "PRIMARY KEY(accountID)";
            database.createTable("Account", accountTableSQL);
            //database = createNewDatabase(database);
            
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
