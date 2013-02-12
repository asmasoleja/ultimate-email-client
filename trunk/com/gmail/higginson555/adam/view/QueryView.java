/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.gmail.higginson555.adam.view;

import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.MessageManager;
import com.gmail.higginson555.adam.UserDatabase;
import com.gmail.higginson555.adam.queryParser.QueryParseException;
import com.gmail.higginson555.adam.queryParser.QueryParser;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 *
 * @author higgina0
 */
public class QueryView
{
    //The query
    private String query;
    //The query parser
    private QueryParser parser;
    //The view name
    private String name;
    //The view account
    private Account account;
    //The list of messageData
    private Object[][] messageData;



    public QueryView(String query, String name, Account account) throws QueryParseException, SQLException
    {
        this.query = query;
        this.name = name;
        this.account = account;
        this.parser = new QueryParser(this.query, UserDatabase.getInstance());
        updateMessages();
    }

    /*
     * Gets the corresponding messages
     */
    public final void updateMessages() throws QueryParseException, SQLException
    {
        //Parse the query and get the ids
        ArrayList<Integer> messageIDs = parser.parseExpression();

        MessageManager mm = new MessageManager(account, UserDatabase.getInstance());
        messageData = mm.getMessageTableData(messageIDs);
    }

    public Account getAccount() {
        return account;
    }

    public Object[][] getMessageData() {
        return messageData;
    }

    public String getName() {
        return name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) throws SQLException, QueryParseException {
        this.query = query;
        updateUserDatabaseQuery();
        updateMessages();
    }

    /**
     * Writes this view to the database
     * @return True if database written to, i.e view does not already exist,
     *         False otherwise
     * @throws SQLException If database error
     */
    public boolean writeToUserDatabase() throws SQLException
    {
        Database userDB = UserDatabase.getInstance();
        //If view does not already exist
        if (userDB.selectFromTableWhere("Views", "*",
                "viewName='" + name + "' AND accountUsername='" + account.getUsername() + "'")
                .isEmpty())
        {
            String[] fieldNames = {"viewName", "accountUsername", "query"};
            Object[] fieldData = {name, account.getUsername(), query};
            userDB.insertRecord("Views", fieldNames, fieldData);

            return true;
        }
        
        return false;
    }

    public void updateUserDatabaseQuery() throws SQLException
    {
        Database userDB = UserDatabase.getInstance();
        ArrayList<Object[]> result = userDB.selectFromTableWhere("Views", "viewID",
                "viewName='" + name + "' AND accountUsername='" + account.getUsername() + "'");

        if (!result.isEmpty())
        {
            int viewID = (Integer) result.get(0)[0];
            userDB.updateRecord("Views", "query='" + query + "'", "viewID=" + Integer.toString(viewID));
        }
    }



}
