package com.gmail.higginson555.adam;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import javax.mail.Message;

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
        if (s == null) {
            return null;
        }
        
        ArrayList<String> keyWords = new ArrayList<String>();
        String[] splitString = s.split("\\s+");
        for (int i = 0; i < splitString.length; i++)
        {
            //Remove punctuation from a string
            String withoutPunc = splitString[i].replaceAll("[^A-Za-z0-9]", "");
            //Don't insert if an ignored word, or it's empty or it contains numbers
            if (!wordsToIgnore.contains(withoutPunc) && !withoutPunc.isEmpty() && !isNumeric(withoutPunc)) {
                keyWords.add(withoutPunc);
            }           
        }
        
        return keyWords;
    }

    /**
     * Add tags to the given database
     * @param database The database to add tags for
     * @param tags The list of 'tags' or key words to add
     * @param messageID The message id these tags were extracted from
     * @throws SQLException If something goes wrong with the SQL database
     */
    public void insertTags(Database database, ArrayList<String> tags, int messageID)
            throws SQLException
    {
        ArrayList<Object[]> tagDBLines = new ArrayList<Object[]>(tags.size());
        //Create array of key words for this message
        for (String tag : tags)
        {
            //Only add if it doesn't already exist!
            if (database.selectFromTableWhere("Tags", "tagID", "tagValue='" + tag + "'").isEmpty())
            {
                Object[] tagFieldValues = {tag};
                tagDBLines.add(tagFieldValues);
                //System.out.println("Added: " + keyWord + " as it doesn't already exist in the table!");
            }

        }

        String[] tagFieldNames = {"tagValue"};
        database.insertRecords("Tags", tagFieldNames, tagDBLines);

        //get the ids of each key word
        int[] tagIDs = new int[tags.size()];
        for (int i = 0; i < tags.size(); i++)
        {
            ArrayList<Object[]> result = database.selectFromTableWhere("Tags", "tagID" , "tagValue='" + tags.get(i) + "'");

            tagIDs[i] = (Integer) result.get(0)[0];

        }

        //Now update message to tags table
        ArrayList<Object[]> newData = new ArrayList<Object[]>(tagIDs.length);
        for (int i = 0; i < tagIDs.length; i++)
        {
            Object[] line = {messageID, tagIDs[i]};
            newData.add(line);
        }

        String[] messagesToTagsFieldNames = {"messageID", "tagID"};
        database.insertRecords("MessagesToTags", messagesToTagsFieldNames, newData);

        //Set tags to be extracted
        database.updateRecord("Messages", "areTagsExtracted=0", "messageID=" + Integer.toString(messageID));
    }

    private static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
    
    
    
}
