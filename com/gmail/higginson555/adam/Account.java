package com.gmail.higginson555.adam;

import javax.mail.Folder;

/**
 * A class which represents a single Account a user might have
 * @author Adam
 */
public class Account 
{
    //The username and password of an account. The password should
    //be encrypted for security
    private String username, password;
    //The folders owned by this account
    private Folder[] folders;
    
    public Account(String username, String password, Folder[] folders)
    {
        this.username = username;
        this.password = password;
        this.folders = folders;
    }

    public String getUsername() {
        return username;
    }

    /**
     * 
     * @return The <b>Encrypted</b> password 
     */
    public String getPassword() {
        return password;
    }

    public Folder[] getFolders() {
        return folders;
    }
    
    
    
    
}
