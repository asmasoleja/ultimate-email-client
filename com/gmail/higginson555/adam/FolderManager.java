package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;


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
    
    public void addFolder(String folderName, String parentFolderList[], Account account) throws SQLException
    {
        
        int[] idList = new int[parentFolderList.length];
        //for each item in list
        for (int i = 0; i < parentFolderList.length; i++)
        {
            //If at top of list
            if (i == 0)
            {
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                        "folderID", 
                        "name='" + parentFolderList[0] + "' AND parentFolder IS NULL");
                idList[0] = (Integer) result.get(0)[0];
            }
            else
            {
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                        "folderID", 
                        "name='" + parentFolderList[i] + 
                        "' AND parentFolder=" + Integer.toString(idList[i - 1]));
                idList[i] = (Integer) result.get(0)[0];
                
            }
        }
        
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                "folderID", 
                "name='" + folderName + 
                "' AND parentFolder=" + Integer.toString(idList[idList.length - 1]));
        
        //Only add a folder if it is unique
        if (result.isEmpty())
        {
            String[] fieldNames = {"name", "accountID", "parentFolder"};
            Object[] fieldValues = {folderName, account.getAccountID(), idList[idList.length - 1]};


            database.insertRecord("Folders", fieldNames, fieldValues);
        } 
    }
    
    public int getFolderID(String folderName) throws SQLException
    {
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", "folderID", "name='" + folderName + "'");
        int folderID = -1;
        if (!result.isEmpty())
        {
            folderID = (Integer) result.get(0)[0];
        }
        
        return folderID;
    }
    
    public int getFolderID(String folderName, String[] parentFolderList) throws SQLException
    {
        //The ids of all the parents
        int[] idList = new int[parentFolderList.length];
        //for each item in list
        for (int i = 0; i < parentFolderList.length; i++)
        {
            //If at top of list
            if (i == 0)
            {
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                        "folderID", 
                        "name='" + parentFolderList[0] + "' AND parentFolder IS NULL");
                idList[0] = (Integer) result.get(0)[0];
            }
            else
            {
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                        "folderID", 
                        "name='" + parentFolderList[i] + 
                        "' AND parentFolder=" + Integer.toString(idList[i - 1]));
                idList[i] = (Integer) result.get(0)[0];
                
            }
        }
        
        //Select id of folder where the name is the given name, and it's parent
        //is the last in the id list, i.e it's parent
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", 
                "folderID", 
                "name='" + folderName + 
                "' AND parentFolder=" + Integer.toString(idList[idList.length - 1]));
        
        int foundID = -1;
        if (!result.isEmpty())
        {
            foundID = (Integer) result.get(0)[0];
        }
        
        return foundID;
    }    
}
