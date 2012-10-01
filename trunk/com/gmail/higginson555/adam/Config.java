/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the config file for the mail system
 * @author Adam
 */
public class Config 
{
    private String fileName;
    private Properties properties;
    private FileOutputStream fileOut;
    
    public Config(String fileName, Properties properties)
    {
        try 
        {
            this.fileName = fileName;
            this.properties = properties;
            this.fileOut = new FileOutputStream(this.fileName);
            this.properties.store(fileOut, null);
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
}
