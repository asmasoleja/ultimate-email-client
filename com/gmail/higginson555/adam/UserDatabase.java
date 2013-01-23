/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
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
        
        Properties config = new Properties();
        try 
        {
            config.load(new FileInputStream("UltimateEmailClient.cfg"));
        } catch (IOException ex) 
        {
            Logger.getLogger("EmailClient").log(Level.WARNING, "Could not find UltimateEmailClient.cfg");
            config.setProperty("UserDatabaseURL", "jdbc:mysql://localhost:3306");
            config.setProperty("username", "root");
            config.setProperty("password", "");
        }
        
        String url = config.getProperty("UserDatabaseURL");
        String user = config.getProperty("username");
        String password = config.getProperty("password");
        
        if (url == null)
        {
            Logger.getLogger("EmailClient").log(Level.WARNING, "URL field is null");
            config.setProperty("UserDatabaseURL", "jdbc:mysql://localhost:3306");
            url = "jdbc:mysql://localhost:3306";
        }
        if (user == null)
        {
            Logger.getLogger("EmailClient").log(Level.WARNING, "Username field is null");
            config.setProperty("username", "root");
            user = "root";
        }
        if (password == null)
        {
            Logger.getLogger("EmailClient").log(Level.WARNING, "Password field is null");
            config.setProperty("password", "");
            password = "";
        }
        
        
        UserDatabaseManager man = new UserDatabaseManager(url, user, password);
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
