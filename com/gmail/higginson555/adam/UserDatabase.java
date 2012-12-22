/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Adam
 */
public class UserDatabase 
{
    private static Database database;
    
    private UserDatabase()
    {
        
    }
    
    public static synchronized Database getInstance()
    {
        if (database != null)
            return database;
        
        UserDatabaseManager man = new UserDatabaseManager();
        try {
            database = man.getDatabaseInstance();
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        
        return database;
    }
    
}
