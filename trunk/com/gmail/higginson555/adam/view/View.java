package com.gmail.higginson555.adam.view;

import com.gmail.higginson555.adam.Database;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

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
    //The list of key words or 'tags' that e-mails should have to be
    //seen in this view
    private ArrayList<String> keyWords;
    //The relationships that are of interest to this view
    private ArrayList<Relationship> relationships;
    
    /**
     * Creates an empty view with no key words, relationships or events
     * @param viewName The name the view should be given
     */
    public View(String viewName)
    {
        this.keyWords = new ArrayList<String>();
        this.relationships = new ArrayList<Relationship>();
    }
    
    public View(String viewName, ArrayList<String> keyWords)
    {
        this.viewName = viewName;
        this.keyWords = keyWords;
    }
    
    public View(String viewName, ArrayList<String> keyWords, ArrayList<Relationship> relationships)
    {
        this(viewName, keyWords);
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
        String[] fieldNames = {"viewName"};
        Object[] fieldValues = {viewName};
        
        //Return false if row already exists!
        if (!database.selectFromTableWhere("Views", "viewID", "viewName = '" + viewName + "'").isEmpty())
        {
            return false;
        }
      
        database.insertRecord("Views", fieldNames, fieldValues);
        //Get ID of inserted record
        ArrayList<Object[]> result = database.selectFromTableWhere("Views", "ViewID", "viewName = '" + viewName + "'");
        int id = (Integer) result.get(0)[0];
        
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

    /**
     * 
     * @return The key words, or 'tags' for this view 
     */
    public ArrayList<String> getKeyWords() {
        return keyWords;
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
    
    
}
