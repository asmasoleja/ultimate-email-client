package com.gmail.higginson555.adam.queryParser;

import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.UserDatabase;
import com.gmail.higginson555.adam.queryParser.QueryNode.QueryNodeType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses a query by contacting a database and retrieving the 
 * correct message ids. 
 * A query is constructed for example: (Computer AND Science) OR Adam,
 * meaning messages containing computer science or adam are found (case does
 * not matter). Expressions can be nested, for example 
 * ((Computer AND Science) or Adam) AND (Manchester OR OLDHAM), so this is 
 * not that easy. This class deals with this problem by taking a stack based 
 * approach to solving smaller sub-expressions, until the overall larger
 * expression is found.
 * @author Adam
 */
public class QueryParser 
{   
    //These can be changed so a different type of bracket can be used
    private static final char OPEN_BRACKET = '(';
    private static final char CLOSE_BRACKET = ')';
    private static final String OPERATOR_OR = "OR";
    private static final String OPERATOR_AND = "AND";
    
    //The string query
    private String query;
    //The stack, holding the query broken down
    private Stack<QueryNode> stack;
    //The database to use
    private Database database;
    
    private QueryParser(String query, Database database)
    {
        this.query = query;
        this.database = database;
        this.stack = new Stack<QueryNode>();
    }

    public String getQuery() {
        return query;
    }
    
    public ArrayList<Integer> parseExpression() throws QueryParseException, SQLException
    {
        //Split query by space
        String[] tokens = query.split("\\s+");
        
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i].trim();
            //Read each character until we find no open brackets
            char currentChar = token.charAt(0);
            while (currentChar == OPEN_BRACKET)
            {
                //push bracket onto stack
                stack.push(new QueryNode(QueryNodeType.NODE_OPEN_BRACKET, OPEN_BRACKET));
                System.out.println("Adding: " + OPEN_BRACKET + " to stack");
                //remove bracket from word
                token = token.substring(1);
                //First char is now the next character in the overall string
                currentChar = token.charAt(0);            
            }
            
            //Get word/operator on its own
            String withoutBrackets = removeBrackets(token);
            //push onto stack
            boolean wasOperator;
            if (isOperatorWord(withoutBrackets))
            {
                stack.push(new QueryNode(QueryNodeType.NODE_OPERATOR, withoutBrackets));
                wasOperator = true;
            }
            else
            {
                stack.push(new QueryNode(QueryNodeType.NODE_TAG, withoutBrackets));
                wasOperator = false;
            }
            
            System.out.println("Adding: " + withoutBrackets + " to stack");
            
            //Now start working on any close brackets
            int index = token.indexOf(withoutBrackets) + withoutBrackets.length();
            
            //Only do it if something after token, i.e close brackets
            if (index < token.length())
            { 
                //System.out.println("Token: " + token + " index: " + index);
                currentChar = token.charAt(index);

                //System.out.println("Current char: " + currentChar);
                while (currentChar == CLOSE_BRACKET)
                {
                    if (!wasOperator)
                    {
                        evaluate();
                    }
                    else
                    {
                        //If word we're dealing with was an operator, this isn't
                        //allowed in the syntax
                        throw new QueryParseException("Tried to close a bracket on an operator word!");
                    }

                    index++;
                    if (index < token.length()) 
                    {
                        currentChar = token.charAt(index);
                    }
                    else
                    {
                        break;
                    }
                }
            }
        } //for each token
        
        //Only remaining item on the stack should be the evaulated message id list
        //return (ArrayList<Integer>) stack.pop().getData();  
        
        //TODO
        return null;
    }
    
    /*
     * Evaluate the stack up to the first found "(".
     * Compresses the value of the stack into a single list
     * containing the ids of the evaluated messages
     */
    private void evaluate() throws QueryParseException, SQLException
    {
        System.out.println("Evaluating stack at current state...");
        printStack();
        System.out.println();
        
        //The list of ids of messages already selected from the database
        ArrayList<Integer> selectedIDs = null;
        //The list of tags to select from the database
        ArrayList<String> selectList = new ArrayList<String>();
        String operator = null;
        
        //While the stack still has values, and the currentStackValue is not an
        //open bracket
        QueryNode currentStackValue;
        while (!stack.empty() 
                && ( (currentStackValue = stack.pop() ).getType() != QueryNodeType.NODE_OPEN_BRACKET))
        {
            if (currentStackValue.getType() == QueryNodeType.NODE_TAG)
            {
                selectList.add((String)currentStackValue.getData());
            }
            else if (currentStackValue.getType() == QueryNodeType.NODE_OPERATOR)
            {
                
                String foundOperator = (String)currentStackValue.getData();
                if (operator != null && !operator.equalsIgnoreCase(foundOperator))
                {
                    throw new QueryParseException("Within brackets, operators must be the same!");
                }
                operator = foundOperator;
            }
            else if (currentStackValue.getType() == QueryNodeType.NODE_MESSAGE_LIST)
            {
                if (selectedIDs == null)
                {
                    selectedIDs = (ArrayList<Integer>)currentStackValue.getData();
                }
                else
                {
                    selectedIDs.addAll((ArrayList<Integer>)currentStackValue.getData());
                }
            }
            else
            {
                throw new QueryParseException("Something unforseen happened within evaluation!");
            }    
        } //while
        
        for (String tag : selectList)
        {
            System.out.println("Tag found: " + tag);
        }        
        
        System.out.println("With operator: " + operator);
        
        //////////////////
        //OR Operator
        /////////////////
        if (operator.equalsIgnoreCase(OPERATOR_OR))
        {
            ArrayList<Integer> messageIDs = new ArrayList<Integer>();
            //Select values from database
            for (String tag : selectList)
            {
                //First get tag id
                ArrayList<Object[]> result = database.selectFromTableWhere("Tags", "tagID", "tagValue='" + tag + "'");
                //Then for each tag id, get message id
                for (Object[] line : result)
                {
                    int tagID = (Integer)line[0];
                    ArrayList<Object[]> messageResults = database.selectFromTableWhere("MessagesToTags", "messageID", "tagID=" + Integer.toString(tagID));
                    for (Object[] messageResult : messageResults)
                    {
                        int messageID = (Integer) messageResult[0];
                        messageIDs.add(messageID);
                    }
                }
            }
            
            //Add to current list if already there
            if (selectedIDs == null)
            {
                selectedIDs = messageIDs;
            }
            else
            {
                selectedIDs.addAll(messageIDs);
            }
            
            //Now push to stack
            stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
        }//OR operator
        
        //////////////
        //AND operator
        /////////////
        if (operator.equalsIgnoreCase(OPERATOR_AND))
        {
            
        }
    }
    
    private void printStack()
    {
        ArrayList<QueryNode> poppedValues = new ArrayList<QueryNode>(stack.size());
        
        while (!stack.empty())
        {
            QueryNode top = stack.pop();
            System.out.println(top.getData());
            poppedValues.add(top);
        }
        
        for (int i = poppedValues.size() - 1; i >= 0; i--)
        {
            stack.add(poppedValues.get(i));
        }
    }
    
    private boolean isOperatorWord(String word)
    {
        return word.equalsIgnoreCase(OPERATOR_OR) 
                || word.equalsIgnoreCase(OPERATOR_AND);
    }
    
    private String removeBrackets(String word)
    {
        String withoutClose = word.replace(CLOSE_BRACKET, '\0');
        return withoutClose.replace(OPEN_BRACKET, '\0').trim(); //Remove open bracket
    }
    
    public static void main(String[] args)
    {
        QueryParser parser = new QueryParser("(Test AND (Naomi OR Adam))", UserDatabase.getInstance());
        try {
            parser.parseExpression();
        } catch (QueryParseException ex) {
            System.err.println(ex.getMessage());
            System.exit(-1);
        } catch (SQLException ex)
        {
            System.err.println(ex.getMessage());
            System.exit(-1);
        }
        
    }
}
