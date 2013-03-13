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
        String withoutPunc = accountToCheck.replaceAll("[^A-Za-z0-9]", "");
        ArrayList<Object[]> result = UserDatabase.getInstance().selectFromTableWhere
                ("TrustedAccounts", 
                "trustedAccount", 
                "accountUsername='" + account.getUsername() 
                + "' AND trustedAccount LIKE '%" + withoutPunc + "%'");
        
        return !result.isEmpty();
    }
    
    public static void setTrustedAccount(Account account, String accountToSet) throws SQLException
    {
        String withoutPunc = accountToSet.replaceAll("[^A-Za-z0-9]", "");
        String[] fieldNames = {"accountUsername", "trustedAccount"};
        Object[] fieldValues = {account.getUsername(), withoutPunc};
        UserDatabase.getInstance().insertRecord("TrustedAccounts", fieldNames, fieldValues);
    }
    
}
