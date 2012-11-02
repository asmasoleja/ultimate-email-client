/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.HomeScreen;
import com.gmail.higginson555.adam.gui.OptionsScreen;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Adam
 */
public class Main 
{
    public static void main(String[] args)
    {
        Logger.getLogger("emailClient").log(Level.INFO, "Started e-mail client...");

        //Check if the config file exists, if it doesn't, run first time stuff
        Properties config = new Properties();
        try 
        {
            FileInputStream configIn = new FileInputStream("email.cfg");
            config.load(configIn);
            
            String username = config.getProperty("username");
            String password = config.getProperty("password");
            String incoming = config.getProperty("incoming_server");
            String outgoing = config.getProperty("outgoing_server");
            String port = config.getProperty("smtp_port");
            
            //Let info popup appear if first time
            if (username == null || password == null || incoming == null
                    || outgoing == null || port == null)
            {
                OptionsScreen options = new OptionsScreen(config, true);
                options.setVisible(true);
            }
            else
            {
                HomeScreen home = new HomeScreen(config);
                home.setVisible(true);
            }
        } 
        catch (FileNotFoundException ex) 
        {
            System.out.println("Could not find email.cfg...");
            OptionsScreen options = new OptionsScreen(config, true);
            options.setVisible(true);
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(null, ex.toString(), "IO Exception", JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }
    }
    
}
