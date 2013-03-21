/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam.queryParser;

import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.UserDatabase;
import java.util.ArrayList;
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
public class QueryParserTest {
    
    private Database database;
    public QueryParserTest() {
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
     * Test of parseExpression method, of class QueryParser.
     */
    @Test
    public void testParseExpressionNormal() throws Exception {
        System.out.println("parseExpression");
        QueryParser instance = new QueryParser("(Test OR Not)", database);
        ArrayList result = instance.parseExpression();
        assertNotNull("Returned a null list!", result);
    }
    
    @Test(expected=QueryParseException.class)
    public void testParseExpressionWrongBrackets() throws Exception
    {
        System.out.println("parseExpressionWrongBrackets");
        QueryParser instance = new QueryParser("(Test OR Not))", database);
        ArrayList result = instance.parseExpression();
    }
    
    @Test(expected=QueryParseException.class)
    public void testParseExpressionWrongKeyword() throws Exception
    {
        System.out.println("parseExpressionWrongKeyword");
        QueryParser instance = new QueryParser("(Test OR Not AND Test)", database);
        ArrayList result = instance.parseExpression();
    }
    
    @Test(expected=QueryParseException.class)
    public void testParseExpressionWrongOperator() throws Exception
    {
        System.out.println("parseExpressionWrongOperator");
        QueryParser instance = new QueryParser("(Test OR)", database);
        ArrayList result = instance.parseExpression();
    }
    
    @Test
    public void testParseExpressionExtension() throws Exception
    {
        System.out.println("parseExpressionExtension");
        QueryParser instance = new QueryParser("(Test OR MESSAGE_FROM test@test.com)", database);
        ArrayList result = instance.parseExpression();
    }
}
