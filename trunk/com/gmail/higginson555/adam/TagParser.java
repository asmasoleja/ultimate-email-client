package com.gmail.higginson555.adam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A class which parses 'tags' or key words from a String.
 * @author Adam
 */
public class TagParser 
{
    private static TagParser instance = null;
    
    private HashSet<String> wordsToIgnore;
    
    private TagParser()
    {
        wordsToIgnore = new HashSet<String>();
        //TODO: Add words that should be ignored here
    }
    
    public static synchronized TagParser getInstance()
    {
        if (instance == null)
        {
            instance = new TagParser();
        }
        
        return instance;
    }
    
    /**
     * Adds a word deemed to be unimportant and thus is not extracted
     * by this tag parser
     * @param word The unimportant word
     */
    public void addUnimporantWord(String word)
    {
        wordsToIgnore.add(word);
    }
    
    /**
     * Gets the 'tags' or key words for a given string
     * @param s The string to parse
     * @return A list of strings, each one representing a key word.
     */
    public ArrayList<String> getTags(String s)
    {
        ArrayList<String> keyWords = new ArrayList<String>();
        String[] splitString = s.split("\\s+");
        for (int i = 0; i < splitString.length; i++)
        {
            //Remove punctuation from a string
            String withoutPunc = splitString[i].replaceAll("[^A-Za-z0-9]", "");
            if (!wordsToIgnore.contains(withoutPunc)) {
                keyWords.add(withoutPunc);
            }           
        }
        
        return keyWords;
    }
    
    
    
}
