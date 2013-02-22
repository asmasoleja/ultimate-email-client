package com.gmail.higginson555.adam;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class which represents a connection to a database
 * object.
 * @author Adam Higginson
 */
public class Database
{
    private String databaseURL;
    private String username;
    private String password;
    private String selectedDB;
    private Connection connection = null;
    private Statement statement = null;
    private Class driverClass = null;
    
    /**
     * Creates a database object
     * @param databaseURL The URL of the database to connect to
     * @param username Username to connect to database
     * @param password Password to connect to database
     * @throws SQLException If fails to connect to the database
     * @throws ClassNotFoundException If cannot register com.mysql.jdbc.Driver
     */
    public Database(String databaseURL, String username, String password)
            throws SQLException, ClassNotFoundException
    {
        this.databaseURL = databaseURL;
        this.username = username;
        this.password = password;
        //Register JDBC driver
        Class.forName("com.mysql.jdbc.Driver");
        
        //Open connection
        Logger.getLogger("emailClient").log(Level.INFO, "Connecting to database...");
        connection = DriverManager.getConnection(databaseURL, username, password);
        statement = connection.createStatement();
        Logger.getLogger("emailClient").log(Level.INFO, "Success! Connected to database: {0}", databaseURL);
    }
    
    public Database(String databaseURL, String databaseName, String username, String password)
            throws SQLException, ClassNotFoundException
    {
        this(databaseURL + "/" + databaseName, username, password);
    }
    
    /**
     * Copy constructor
     * @param database The database to copy
     */
    public Database(Database database) throws SQLException, ClassNotFoundException
    {
        this.databaseURL = database.databaseURL;
        this.username = database.username;
        this.password = database.password;
        Class.forName("com.mysql.jdbc.Driver");
        
        connection = DriverManager.getConnection(databaseURL, username, password);
        statement = connection.createStatement();
    }
        
    /**
     * Closes this database object
     * @throws SQLException If fails to close database
     */
    public void close() throws SQLException
    {
        if (connection != null) 
        {
            connection.close();
        }
        if (statement != null) 
        {
            statement.close();
        }
    }
    
    /**
     * Creates a database with the given name
     * @param name The name of the database to be created
     * @throws SQLException 
     */
    public void createDatabase(String name) throws SQLException
    {
        connection.close();
        connection = DriverManager.getConnection(databaseURL, username, password);
        execute("CREATE DATABASE " + name);
        Logger.getLogger("emailClient").log(Level.INFO, "Created database: {0}", name);
    }
    
    public void dropDatabase(String name) throws SQLException
    {
        execute("DROP DATABASE " + name);
    }
    
    /**
     * Changes the connection of this database to the new database
     * @param name The new database to connect to
     * @throws SQLException 
     */
    public void selectDatabase(String name) throws SQLException
    {
        this.close();
        selectedDB = name;
        connection = DriverManager.getConnection(databaseURL + "/" + selectedDB + "?useServerPreStmts=false&rewriteBatchedStatements=true", username, password);
        statement = connection.createStatement();
    }
    
    public ArrayList<Object[]> selectFromTable(String tableName, String values) throws SQLException
    {
        String sql = "SELECT " + values + " FROM " + tableName;
        Statement select = connection.createStatement();
        ResultSet result = select.executeQuery(sql);
        ArrayList<Object[]> tableData = new ArrayList<Object[]>();
        //Add each line to the array list.
        while (result.next())
        {
            Object[] line = new Object[result.getMetaData().getColumnCount()];
            for (int i = 0; i < line.length; i++)
            {
                line[i] = result.getObject(i + 1);
            }
            tableData.add(line);
        }
        
        return tableData;      
    }
    
    public ArrayList<Object[]> selectFromTableWhere(String tableName, 
                                                    String values, 
                                                    String whereSQL) throws SQLException
    {
        String sql = "SELECT " + values + " FROM " + tableName + " WHERE " + whereSQL;
        //System.out.println("Query is: " + sql);
        Statement select = connection.createStatement();
        ResultSet result = select.executeQuery(sql);
        ArrayList<Object[]> tableData = new ArrayList<Object[]>();
        
        while (result.next())
        {
            Object[] line = new Object[result.getMetaData().getColumnCount()];
            for (int i = 0; i < line.length; i++)
            {
                line[i] = result.getObject(i + 1);
            }
            tableData.add(line);
        }
        
        return tableData;
    }
    
    public ArrayList<Object[]> selectAllFromTable(String tableName) throws SQLException
    {
        String sql = "SELECT * FROM " + tableName; 
        Statement select = connection.createStatement();
        ResultSet result = select.executeQuery(sql);
        ArrayList<Object[]> tableData = new ArrayList<Object[]>();
        //Add each line to the array list.
        while (result.next())
        {
            Object[] line = new Object[result.getMetaData().getColumnCount()];
            for (int i = 0; i < line.length; i++)
            {
                line[i] = result.getObject(i + 1);
            }
            tableData.add(line);
        }
        
        return tableData;  
    }
    
    /**
     * Creates a table in the currently selected database
     * @param tableName The name of the table to be created
     * @param criteria The details of the table to be created
     * @throws SQLException 
     */
    public void createTable(String tableName, String criteria) throws SQLException
    {
        String sql = "CREATE TABLE " + tableName + "(" + criteria + ")";
        execute(sql);
    }
    
    /**
     * Inserts multiple rows into the database at once
     * @param tableName The table to insert rows into
     * @param fieldNames The names of the fields to update
     * @param fieldValues An arraylist, where each element corresponds to a single row in the database
     * @throws SQLException 
     */
    public void insertRecords(String tableName, String[] fieldNames, ArrayList<Object[]> fieldValues)
            throws SQLException
    {
        String query = "INSERT INTO " + tableName + " (";
        for (int i = 0; i < fieldNames.length - 1; i++)
        {
            query += (fieldNames[i] + ", ");
        }
        query += fieldNames[fieldNames.length - 1] + ") VALUES (";
        //Now insert as many question marks as needed
        for (int i = 0; i < fieldNames.length - 1; i++)
        {
            query += "?, ";
        }
        query += "?)";
        
        System.out.println("Query is: " + query);
        
        Iterator<Object[]> lineIter = fieldValues.iterator();
        
        PreparedStatement ps = connection.prepareStatement(query);
        while (lineIter.hasNext())
        {
            long startTime = System.currentTimeMillis();
            Object[] line = lineIter.next();
            
            
            for (int i = 0; i < line.length; i++)
            {
                if (line[i] instanceof Integer) {
                    ps.setInt(i + 1, (Integer)line[i]);
                }
                else if (line[i] instanceof Double) {
                    ps.setDouble(i + 1, (Double)line[i]);
                }
                else if (line[i] instanceof Long) {
                    ps.setLong(i + 1, (Long)line[i]);
                }
                else if (line[i] instanceof java.util.Date)
                {
                    java.util.Date date = (java.util.Date) line[i];
                    Timestamp ts = new Timestamp(date.getTime());
                    ps.setTimestamp(i + 1, ts);
                }
                else if (line[i] instanceof String) {
                    ps.setString(i + 1, (String)line[i]);
                }
                else
                {
                    System.out.println("Warning, not setting something! Value: " + line[i] + " fieldName " + fieldNames[i]);
                    ps.setNull(i + 1, java.sql.Types.VARCHAR);
                    if (line[i] != null)
                        System.out.println("Found weird one: " + line[i].getClass().getName());
                }
                
                //System.out.println("Field value: " + line[i]);
            }
            //System.out.println("\n\n");
           
            ps.addBatch();
            long endTime = System.currentTimeMillis();
            //System.out.println("Took: " + (endTime - startTime) + " to add a single item to the batch!");
        }
        
        ps.executeBatch();
    }
    

    
    public void insertRecord(String tableName, String[] fieldNames, Object[] fieldValues) throws SQLException
    {
        String query = "INSERT INTO " + tableName + " (";
        for (int i = 0; i < fieldNames.length - 1; i++)
        {
            query += (fieldNames[i] + ", ");
        }
        query += fieldNames[fieldNames.length - 1] + ") VALUES (";
        //Now insert as many question marks as needed
        for (int i = 0; i < fieldNames.length - 1; i++)
        {
            query += "?, ";
        }
        query += "?)";
        
       //System.out.println("Query is: " + query);

        PreparedStatement ps = connection.prepareStatement(query);
        for (int i = 0; i < fieldValues.length; i++)
        {
            if (fieldValues[i] instanceof Integer) {
                ps.setInt(i + 1, (Integer)fieldValues[i]);
            }
            else if (fieldValues[i] instanceof Double) {
                ps.setDouble(i + 1, (Double)fieldValues[i]);
            }
            else if (fieldValues[i] instanceof Long) {
                    ps.setLong(i + 1, (Long)fieldValues[i]);
            }
            else if (fieldValues[i] instanceof java.util.Date)
            {
                System.out.println("Found date!");
                java.util.Date date = (java.util.Date) fieldValues[i];
                Timestamp ts = new Timestamp(date.getTime());
                ps.setTimestamp(i + 1, ts);
            }
            else if (fieldValues[i] instanceof String) {
                ps.setString(i + 1, (String)fieldValues[i]);
            }
            
            //System.out.print("Field value type: " + fieldValues[i].getClass().getName() + ", ");
            System.out.println("Field value: " + fieldValues[i]);
            //ps.addBatch();
    
        }
        ps.addBatch();
        ps.executeBatch();
        ps.close();
        
    }
    
    /**
     * 
     * @param tableName Name of table to update
     * @param setSQL The sql that follows the "SET" clause, e.g Address='Nissestien 67', City='Sandnes'
     * @param whereSQL The sql that follows the "WHERE" clause e.g LastName='Tjessem' AND FirstName='Jakob'
     */
    public void updateRecord(String tableName, String setSQL, String whereSQL)
            throws SQLException
    {
        Statement update = connection.createStatement();
        String sql = "UPDATE " + tableName + " SET " + setSQL + " WHERE " + whereSQL;
        //System.out.println("Query is: " + sql);
        update.execute(sql);
        update.close();
    }

    public void deleteRecord(String tableName, String whereSQL)
            throws SQLException
    {
        Statement update = connection.createStatement();
        String sql = "DELETE FROM " + tableName + " WHERE " + whereSQL;
        update.execute(sql);
        update.close();
    }
        
    private ResultSet execute(String sqlCommand) throws SQLException
    {
        if (statement != null)
            statement.close();
        statement = connection.createStatement();
        statement.executeUpdate(sqlCommand);        
        ResultSet result = statement.getResultSet();
                
        return result;
    }
    
    public String getSelectedDB()
    {
        return selectedDB;
    }
    
    
}
