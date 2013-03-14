package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.LoadingScreen;
import com.gmail.higginson555.adam.view.RelationshipType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

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

    public UserDatabaseManager(String databaseURL, String username, String password) {
        this.databaseURL = databaseURL;
        this.username = username;
        this.password = password;
    }
    
    
    
    public Database getDatabaseInstance() throws SQLException, ClassNotFoundException
    {
        Database database = new Database(databaseURL, username, password);
        
        //try
        //{
        //    database.selectDatabase("User");
        //}
        //catch (SQLException ex)
        //{
        try
        {
            //LoadingScreen ls = new LoadingScreen("Please wait, creating database...");
            //ls.setVisible(true);
            //Create the User database
            //database.createDatabase("User");
            //database.selectDatabase("User");
            database.selectDatabase("S10_higgina0");
            //ACCOUNT TABLE
            String accountTableSQL = 
                          "username varchar(255) NOT NULL,"
                        + "password varchar(255) NOT NULL,"
                        + "accountType varchar(5) NOT NULL,"
                        + "incoming varchar(50) NOT NULL,"
                        + "outgoing varchar(50) NOT NULL,"
                        + "outgoingPort int NOT NULL,"
                        + "PRIMARY KEY(username)";
            database.createTable("Accounts", accountTableSQL);
            //FOLDER TABLE
            String folderTableSQL = "folderID int NOT NULL AUTO_INCREMENT,"
                                  + "name varchar(255) NOT NULL,"
                                  + "accountUsername varchar(255) NOT NULL,"
                                  + "parentFolder int,"
                                  + "lastMessage TIMESTAMP NULL DEFAULT NULL,"
                                  + "urlName varchar(70),"
                                  + "uidValidity BIGINT(19),"
                                  + "lastSeqNo BIGINT(19),"
                                  + "PRIMARY KEY(folderID),"
                                  + "FOREIGN KEY(accountUsername) REFERENCES accounts(username),"
                                  + "FOREIGN KEY(parentFolder) REFERENCES folders(folderID)";
            database.createTable("Folders", folderTableSQL);
            //MESSAGE TABLE
            String messageTableSQL = "messageID int NOT NULL AUTO_INCREMENT,"
                                   + "messageUID varchar(255) NOT NULL,"
                                   + "subject varchar(255),"
                                   + "messageFrom varchar(255),"
                                   + "messageTo varchar(255),"
                                   + "dateSent TIMESTAMP,"
                                   + "dateReceived TIMESTAMP,"
                                   + "folderID int NOT NULL,"
                                   + "accountUsername varchar(255) NOT NULL,"
                                   + "areTagsExtracted BOOLEAN DEFAULT 0,"
                                   + "messageNo BIGINT,"
                                   + "isValidMessage BOOLEAN DEFAULT 1,"
                                   + "isRead BOOLEAN DEFAULT 0,"
                                   + "PRIMARY KEY(messageID),"
                                   + "FOREIGN KEY(folderID) REFERENCES folders(folderID),"
                                   + "FOREIGN KEY(accountUsername) REFERENCES accounts(username)";
            database.createTable("Messages", messageTableSQL);
            //Tags table
            String tagsTableSQL = "tagID int NOT NULL AUTO_INCREMENT,"
                             + "tagValue varchar(255),"
                             + "PRIMARY KEY(tagID)";
            database.createTable("Tags", tagsTableSQL);
            //MessagesToTags table
            String messagesToTagsSQL = "messageToTagID int NOT NULL AUTO_INCREMENT,"
                                     + "messageID int NOT NULL,"
                                     + "tagID int NOT NULL,"
                                     + "PRIMARY KEY(messageToTagID),"
                                     + "FOREIGN KEY(messageID) REFERENCES messages(messageID),"
                                     + "FOREIGN KEY(tagID) REFERENCES tags(tagID)";
            database.createTable("MessagesToTags", messagesToTagsSQL);
            //RelationshipTypes table
            String relationshipTypesTableSQL = "relationshipID int NOT NULL AUTO_INCREMENT,"
                                             + "relationshipName varchar(30),"
                                             + "PRIMARY KEY(relationshipID)";
            database.createTable("RelationshipTypes", relationshipTypesTableSQL);
            ArrayList<RelationshipType> relationshipTypes = RelationshipType.getDefaultRelationships();
            String[] fieldNames = {"relationshipName"};
            ArrayList<Object[]> fieldValues = new ArrayList<Object[]>(relationshipTypes.size());
            Iterator<RelationshipType> relationshipTypeIter = relationshipTypes.iterator();
   
            //Convert to data structure needed for database
            while (relationshipTypeIter.hasNext())
            {
                Object[] line = {relationshipTypeIter.next().toString()};
                fieldValues.add(line);
            }
            database.insertRecords("RelationshipTypes", fieldNames, fieldValues);
            
            String viewTableSQL = "viewID int NOT NULL AUTO_INCREMENT,"
                                + "viewName varchar(50),"
                                + "accountUsername varchar(255),"
                                + "query varchar(1000) DEFAULT NULL,"
                                + "PRIMARY KEY(viewID),"
                                + "FOREIGN KEY(accountUsername) REFERENCES accounts(username)";
            
            database.createTable("Views", viewTableSQL);
            
            String viewTagsSQL = "viewTagID int NOT NULL AUTO_INCREMENT,"
                               + "viewTagValue varchar(50),"
                               + "PRIMARY KEY(viewTagID)";
            database.createTable("ViewTags", viewTagsSQL);
            
            String viewToTagsSQL = "viewID int NOT NULL AUTO_INCREMENT,"
                               + "viewTagID int NOT NULL,"
                               + "PRIMARY KEY(viewTagID, viewID),"
                               + "FOREIGN KEY(viewTagID) REFERENCES viewtags(viewTagID),"
                               + "FOREIGN KEY(viewID) REFERENCES views(viewID)";
            database.createTable("ViewToViewTags", viewToTagsSQL);
            
            String accountMessageDownloaderSQL = "accountDownloaderID int UNIQUE NOT NULL AUTO_INCREMENT,"
                                                + "accountUsername varchar(255) NOT NULL,"
                                                + "isDone BOOLEAN DEFAULT 0,"
                                                + "PRIMARY KEY(accountDownloaderID),"
                                                + "FOREIGN KEY(accountUsername) REFERENCES accounts(username)";
            database.createTable("AccountMessageDownloaders", accountMessageDownloaderSQL);
            
            String trustedAccountsSQL = "trustedAccountID int UNIQUE NOT NULL AUTO_INCREMENT,"
                                        + "accountUsername varchar(255) NOT NULL,"
                                        + "trustedAccount varchar(255) NOT NULL,"
                                        + "PRIMARY KEY(trustedAccountID),"
                                        + "FOREIGN KEY(accountUsername) REFERENCES accounts(username)";
            database.createTable("TrustedAccounts", trustedAccountsSQL);
            
            //database = createNewDatabase(database);
            
            //ls.dispose();
        //}
        }
        catch (SQLException ex)
        {
            //Already created tables and stuff, just skip
            System.out.println("Tables already created, skipping...");
        }
        
        return database;
    }
}
