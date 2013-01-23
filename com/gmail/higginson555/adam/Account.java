package com.gmail.higginson555.adam;

import javax.mail.Folder;

/**
 * A class which represents a single Account a user might have
 * @author Adam
 */
public class Account 
{
    //The ID of an account, which is used as a primary key in the Accounts table
    private int accountID;
    //The username and password of an account. The password should
    //be encrypted for security
    private String username, password;
    //The account type, POP3, IMAP, IMAPS
    private String accountType;
    //Incoming and outgoing server
    private String incoming, outgoing;
    //The outgoing port
    private int outgoingPort;
    //The folders owned by this account
    private Folder[] folders;

    public Account(String username, String password, String accountType, String incoming, String outgoing, int outgoingPort) {
        this.username = username;
        this.password = password;
        this.accountType = accountType;
        this.incoming = incoming;
        this.outgoing = outgoing;
        this.outgoingPort = outgoingPort;
    }

     
    
    public Account(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    public Account (int accountID, String username, String password)
    {
        this.accountID = accountID;
        this.username = username;
        this.password = password;
    }
    
    public Account(int accountID, String username, String password, Folder[] folders)
    {
        this.accountID = accountID;
        this.username = username;
        this.password = password;
        this.folders = folders;
    }
    
    public int getAccountID()
    {
        return accountID;
    }

    public String getUsername() {
        return username;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getIncoming() {
        return incoming;
    }

    public String getOutgoing() {
        return outgoing;
    }

    public int getOutgoingPort() {
        return outgoingPort;
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
    
    public void setFolders(Folder[] folders)
    {
        this.folders = folders;
    }
    
    @Override
    public String toString()
    {
        return "<html> <b>" + this.username + "</b> </html>";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.username != null ? this.username.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Account other = (Account) obj;
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        return true;
    }
    
    
    
    
}
