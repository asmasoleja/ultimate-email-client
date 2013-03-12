/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author Adam
 */
public class TrustedAccount 
{
    public static boolean isTrustedAccount(Account account, String accountToCheck) throws SQLException
    {
        ArrayList<Object[]> result = UserDatabase.getInstance().selectFromTableWhere
                ("TrustedAccounts", 
                "trustedAccount", 
                "accountUsername='" + account.getUsername() 
                + "' AND trustedAccount LIKE '%" + accountToCheck + "%'");
        
        return !result.isEmpty();
    }
    
    public static void setTrustedAccount(Account account, String accountToSet) throws SQLException
    {
        String[] fieldNames = {"accountUsername", "trustedAccount"};
        Object[] fieldValues = {account.getUsername(), accountToSet};
        UserDatabase.getInstance().insertRecord("TrustedAccounts", fieldNames, fieldValues);
    }
    
}
