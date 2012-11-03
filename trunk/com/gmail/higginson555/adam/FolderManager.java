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
                ArrayList<Object[]> result = database.selectFromTableWhere("Folders", "folderID", "name='" + parentFolderList[0] + "' AND parentID=NULL");
                idList[0] = (Integer) result.get(0)[0];
            }
        }
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", "folderID, parentFolder", "name='" + folderName + "'");
        ArrayList<Object[]> parentResult = database.se
        Iterator<Object[]> lineIter = result.iterator();
        while (lineIter.hasNext())
        {
            Object[] line = lineIter.next();
            
        }
        int folderID = -1;
        if (!result.isEmpty())
        {
            folderID = (Integer) result.get(0)[0];
        }
    }
    
    public int getParentFolderID(String parentFolder) throws SQLException
    {
        
        ArrayList<Object[]> result = database.selectFromTableWhere("Folders", "folderID", parentFolder)
    }
    
}
