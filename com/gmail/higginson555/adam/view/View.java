package com.gmail.higginson555.adam.view;

import com.gmail.higginson555.adam.Account;
import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.gui.PropertyListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;

/**
 * A view represents some form of accessing a subset of all e-mails in an 
 * account which conform to some set of features. 
 * 
 * At the moment, a view simply displays e-mails with a list of 'key words'.
 * Each e-mail is stored with a key word in the database, and the view gets
 * e-mails that contain the specified key words.
 * @author Adam
 */
public class View 
{
    //The name of this view
    private String viewName;
    //The id of this view in the database
    private int id = -1;
    //The account this view belongs to
    private Account account;

    //The list of key words or 'tags' that e-mails should have to be
    //seen in this view
    private ArrayList<String> keyWords;
    //The relationships that are of interest to this view
    private ArrayList<Relationship> relationships;
    
    //Any listeners wanting to be updated
    private ArrayList<PropertyListener> listeners;
    
    
    
    /**
     * Creates an empty view with no key words, relationships or events
     * @param viewName The name the view should be given
     * @param account The account this view belongs to
     */
    public View(String viewName, Account account)
    {
        this.keyWords = new ArrayList<String>();
        this.account = account;
        this.relationships = new ArrayList<Relationship>();
        listeners = new ArrayList<PropertyListener>();
    }
    
    public View(String viewName, Account account, ArrayList<String> keyWords)
    {
        this.viewName = viewName;
        this.account = account;
        this.keyWords = keyWords;
        listeners = new ArrayList<PropertyListener>();
    }
    
    public View(String viewName, Account account, ArrayList<String> keyWords, ArrayList<Relationship> relationships)
    {
        this(viewName, account, keyWords);
        this.relationships = relationships;
    }
    
    /**
     * Write this view to the database for saving
     * @param database The database to use
     * @return false if view name already exists
     * @throws SQLException If anything goes wrong in writing to a database
     */
    public boolean writeToDatabase(Database database) throws SQLException
    {
        String[] fieldNames = {"viewName", "accountUsername"};
        Object[] fieldValues = {viewName, account.getUsername()};
        
        //Return false if row already exists!
        if (!database.selectFromTableWhere("Views", "viewID", "viewName = '" + viewName + "' AND accountUsername = '" + account.getUsername() + "'").isEmpty())
        {
            return false;
        }
      
        database.insertRecord("Views", fieldNames, fieldValues);
        //Get ID of inserted record
        ArrayList<Object[]> result = database.selectFromTableWhere("Views", "ViewID", 
                "viewName = '" + viewName + "' AND accountUsername = '" + account.getUsername() + "'");
        id = (Integer) result.get(0)[0];
        
        //Insert tags for this
        String[] viewTagsFields = {"viewTagValue"};
        ArrayList<Object[]> dbData = new ArrayList<Object[]>(keyWords.size());
        Iterator<String> keyWordsIter = keyWords.iterator();
        //The data to be inserted into the view to view tags table
        ArrayList<Object[]> viewToViewTagsData = new ArrayList<Object[]>(keyWords.size());
        while (keyWordsIter.hasNext())
        {
            String keyWord = keyWordsIter.next();
            Object[] line = {keyWord};
            database.insertRecord("ViewTags", viewTagsFields, line);
            //Get the id of the inserted word
            int keyWordID = (Integer) database.selectFromTableWhere("ViewTags", "viewTagID", "viewTagValue = '" + keyWord + "'").get(0)[0];
            //Build up data to insert into ViewToViewTags table
            Object[] viewToViewTagsLine = {id, keyWordID};
            viewToViewTagsData.add(viewToViewTagsLine);         
        }
        
        //Add data to view to view tags data!
        String[] viewToViewTagsNames = {"viewID", "viewTagID"};
        database.insertRecords("ViewToViewTags", viewToViewTagsNames, viewToViewTagsData);
        
        publishPropertyEvent("DatabaseWrite", this);
        
        
        return true;
    }
    

    public ArrayList<Relationship> getRelationships() {
        return relationships;
    }
    
    /**
     * 
     * @return The name of this view 
     */
    public String getViewName() {
        return viewName;
    }
    
    public int getId() {
        return id;
    }
    

    public Account getAccount() {
        return account;
    }

    /**
     * 
     * @return The key words, or 'tags' for this view 
     */
    public ArrayList<String> getKeyWords() {
        return keyWords;
    }
    
    public void addListener(PropertyListener listener)
    {
        listeners.add(listener);
    }
    
    private void publishPropertyEvent(String name, Object value)
    {
        for (PropertyListener listener : listeners)
        {
            listener.onPropertyEvent(this.getClass(), name, value);
        }
    }
    
   /**
    * Adds a key word to this view's key word list
    * @param word The word to add to the key word list
    */
    public void addKeyWord(String word)
    {
        keyWords.add(word);
    }
    
    public void addRelationship(Relationship relationship)
    {
        relationships.add(relationship);
    }
    
    public static View getView(Database database, 
                               String viewName, Account account) throws SQLException
    {
        ArrayList<Object[]> result = database.selectFromTableWhere("Views", "viewID", "viewName = '" 
                + viewName + "' AND accountUsername='" + account.getUsername() + "'");
        int id = (Integer) result.get(0)[0];
        
        View view = new View(viewName, account);
        view.id = id;
        
        return view;
    }
    
    public static ArrayList<View> getViewsForAccount(Database database, Account account)
            throws SQLException
    {  
        ArrayList<Object[]> result = database.selectFromTableWhere("Views", 
                "viewID, viewName", 
                "accountUsername='" + account.getUsername() + "'");
        ArrayList<View> returnList = new ArrayList<View>(result.size());
        
        Iterator<Object[]> resultIter = result.iterator();
        while (resultIter.hasNext())
        {
            Object[] line = resultIter.next();
            int viewID = (Integer) line[0];
            String viewName = (String) line[1];
            
            View view = new View(viewName, account);
            view.id = viewID;
            
            returnList.add(view);
        }
        
        return returnList;
    }   
    
    @Override
    public String toString()
    {
        return this.viewName;
    }
}
