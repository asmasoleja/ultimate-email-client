/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.util.ArrayList;
import java.util.HashSet;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Adam
 */
public class TagParserTest {
    
    private Database database;
    
    public TagParserTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() 
    {
        database = UserDatabase.getInstance();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getInstance method, of class TagParser.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        TagParser result = TagParser.getInstance();
        assertNotNull("Instance is null!", result);
    }

    /**
     * Test of addUnimporantWord method, of class TagParser.
     */
    @Test
    public void testAddUnimporantWord() {
        System.out.println("addUnimporantWord");
        String word = "TestWord";
        TagParser instance = TagParser.getInstance();
        instance.addUnimporantWord(word);
        
        HashSet<String> ignored = instance.getUnimportantWords();
        if (!ignored.contains(word))
        {
            fail("Word not added!");
        }
    }

    /**
     * Test of getTags method, of class TagParser.
     */
    @Test
    public void testGetTags() {
        System.out.println("getTags");
        String s = "";
        TagParser instance = TagParser.getInstance();
        ArrayList<String> tags = instance.getTags("Hello, I am adding tags");
        
        if (tags.size() != 5)
        {
            fail("Not all tags added!");
        }
    }
}
