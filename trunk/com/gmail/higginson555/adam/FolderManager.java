package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;


/**
 * A class which acts as a bridge between the folders in the database
 * and the program
 * @author Adam
 */
public class FolderManager 
{
    //The database to use
    private Database database;
    
    public FolderManager(Database database)
    {
        this.database = database;
    }
    
    public void addFolder(String folderName, Account account) throws SQLException
    {
        //Only add a folder if it is unique
        ArrayList<Object[]> resultDuplicate = database.selectFromTableWhere("Folders", "name", "name='" + folderName + "'");
        if (resultDuplicate.isEmpty())
        {
            String[] fieldNames = {"name", "accountID"};
            Object[] fieldValues = {folderName, account.getAccountID()};

            database.insertRecord("Folders", fieldNames, fieldValues);
        }
    }
    
    public void addFolder(String folderName, String parentFolder, Account account) throws SQLException
    {
        //Only add a folder if it is unique
        ArrayList<Object[]> resultDuplicate = database.selectFromTableWhere("Folders", "name", "name='" + folderName + "'");
        if (resultDuplicate.isEmpty())
        {
            String whereSQL = "name='" + parentFolder + "'";
            ArrayList<Object[]> result = database.selectFromTableWhere("Folders", "folderID", whereSQL);
            int parentID = (Integer) result.get(0)[0];

            String[] fieldNames = {"name", "accountID", "parentFolder"};
            Object[] fieldValues = {folderName, account.getAccountID(), parentID};


            database.insertRecord("Folders", fieldNames, fieldValues);
        }
    }
    
}
