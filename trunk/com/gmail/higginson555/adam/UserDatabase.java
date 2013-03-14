/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

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
        try {
            config.store(new FileOutputStream(new File("UltimateEmailClient.cfg")), "Ultimate Email Client config");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        UserDatabaseManager man = new UserDatabaseManager(url, user, password);
        try {
            database = man.getDatabaseInstance();
        } catch (SQLException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Cannot connect to: " + database.getDatabaseURL(), url, JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(UserDatabase.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        
        return database;
    }
    
}
