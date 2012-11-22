package com.gmail.higginson555.adam.view;

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
