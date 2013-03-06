package com.gmail.higginson555.adam.queryParser;

import com.gmail.higginson555.adam.Database;
import com.gmail.higginson555.adam.MessageFolderInfo;
import com.gmail.higginson555.adam.UserDatabase;
import com.gmail.higginson555.adam.queryParser.QueryNode.QueryNodeType;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

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

    //Extensions
    private static final String DATE_AFTER = "DATE_AFTER";
    private static final String DATE_BEFORE = "DATE_BEFORE";
    private static final String DATE_IS = "DATE_IS";
    
    private static final String MESSAGE_FROM = "MESSAGE_FROM";
    private static final String MESSAGE_TO = "MESSAGE_TO";
    
    //The string query
    private String query;
    //The stack, holding the query broken down
    private Stack<QueryNode> stack;
    //The database to use
    private Database database;
    //Whether an operator has previously been found, if true, a tag 
    //cannot be on its own
    private boolean operatorPrevFound;
    //The map mapping UIDs to a message's folder info
    private HashMap<String, MessageFolderInfo> uidToMessageInfo;
    
    public QueryParser(String query, Database database)
    {
        this.query = query;
        this.database = database;
        this.stack = new Stack<QueryNode>();
        this.operatorPrevFound = false;
        this.uidToMessageInfo = new HashMap<String, MessageFolderInfo>();
    }

    public String getQuery() {
        return query;
    }

    /**
     * Parses the expression held by this QueryParser object
     * @return A list of integers, each representing the ID of a message in the db which matches this query
     * @throws QueryParseException If query cannot be executed
     * @throws SQLException If database error
     */
    public ArrayList<Integer> parseExpression() throws QueryParseException, SQLException
    {
        operatorPrevFound = false;
        //Split query by space
        String[] tokens = query.split("\\s+");
        
        int openBracketCount = 0;
        int closeBracketCount = 0;
        String foundExtension = null;
        for (int i = 0; i < tokens.length; i++)
        {
            String token = tokens[i].trim();
            //Read each character until we find no open brackets

            char currentChar = token.charAt(0);
            while (currentChar == OPEN_BRACKET)
            {
                openBracketCount++;
                //push bracket onto stack
                stack.push(new QueryNode(QueryNodeType.NODE_OPEN_BRACKET, OPEN_BRACKET));
                System.out.println("Adding: " + OPEN_BRACKET + " to stack");
                //remove bracket from word
                token = token.substring(1);
                //First char is now the next character in the overall string
                currentChar = token.charAt(0);            
            }
            
            System.out.println("Found: " + openBracketCount + " open brackets!");
            //Get word/operator on its own
            String withoutBrackets = removeBrackets(token);
            //push onto stack
            boolean wasOperator;
            if (isOperatorWord(withoutBrackets))
            {
                stack.push(new QueryNode(QueryNodeType.NODE_OPERATOR, withoutBrackets));
                wasOperator = true;
            }
            else if (isExtensionWord(withoutBrackets))
            {
                //stack.push(new QueryNode(QueryNodeType.NODE_EXTENSION, withoutBrackets));
                foundExtension = withoutBrackets;
                wasOperator = false;
            }
            else
            {
                //Tag is the second part of the found extension, 
                //e.g MESSAGE_FROM adam.higginson@gmail.com
                //the tag would be the e-mail address
                if (foundExtension != null)
                {
                    foundExtension += (" " + withoutBrackets);
                    stack.push(new QueryNode(QueryNodeType.NODE_EXTENSION, foundExtension));
                    foundExtension = null;
                }
                else
                {
                    stack.push(new QueryNode(QueryNodeType.NODE_TAG, withoutBrackets));
                }
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
                //While we find a last bracket or are on the last word of all tokens
                System.out.println("is is: " + i + " length -1 is: " + (tokens.length - 1));
                while (currentChar == CLOSE_BRACKET || i == tokens.length - 1)
                {
                    closeBracketCount++;
                    if (closeBracketCount > openBracketCount) 
                    {
                        throw new QueryParseException("Found mismatching brackets!");
                    }
                    
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
                
                System.out.println("Found: " + closeBracketCount + " close brackets!");
                

            }
            
        } //for each token
        
        if (openBracketCount != closeBracketCount)
        {
            throw new QueryParseException("Found mismatching brackets!");
        }
        
        if (stack.peek().getData() instanceof ArrayList)
        {
            ArrayList<Integer> top = (ArrayList<Integer>) stack.peek().getData();
            System.out.println("Returning list of size: " + top.size());
            return (ArrayList<Integer>) stack.pop().getData();
        }
        else
        {
            return new ArrayList<Integer>();
            //throw new QueryParseException("Error, last remaining item on the stack was not a list!");
        }
        
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
        //Extension words, so far MESSAGE_FROM and MESSAGE_TO
        String extension = null;
        
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
            else if (currentStackValue.getType() == QueryNodeType.NODE_EXTENSION)
            {
                extension = (String)currentStackValue.getData();
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
        //No Operator found
        //////////////////
        if (operator == null)
        {
            if (!operatorPrevFound)
            {
                //E.G MESSAGE_FROM MESSAGE_TO
                if (extension != null)
                {
                    String[] split = extension.split("\\s+");
                    if (split.length != 2)
                    {
                        throw new QueryParseException("Should have EXTENSION_OP Tag!");
                    }
                    String extensionOp = split[0];
                    String extensionTag = split[1];
                    
                    //Select from database all messages from such a person
                    if (extensionOp.equalsIgnoreCase(MESSAGE_FROM))
                    {
                        ArrayList<Object[]> result = database.selectFromTableWhere(
                                "Messages", 
                                "messageID, messageUID, folderID, seqNo", 
                                "isValidMessage=1 AND messageFrom LIKE '%" + extensionTag + "%'");
                        ArrayList<Integer> messageIDs = new ArrayList<Integer>(result.size());
                        for (Object[] line : result)
                        {
                            int id = (Integer) line[0];
                            String UID = (String) line[1];
                            int folderID = (Integer) line[2];
                            int seqNo = (Integer) line[3];
                            if (uidToMessageInfo.containsKey(UID))
                            {
                                MessageFolderInfo msgInfo = uidToMessageInfo.get(UID);
                                msgInfo.addFolder(folderID);
                            }
                            else
                            {
                                messageIDs.add(id);
                                MessageFolderInfo msgInfo = new MessageFolderInfo(id, seqNo);
                                uidToMessageInfo.put(UID, msgInfo);
                            }
                        }
                        System.out.println("Message IDs size: " + messageIDs.size());
                        stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, messageIDs));
                    }
                    else if (extensionOp.equalsIgnoreCase(MESSAGE_TO))
                    {
                        ArrayList<Object[]> result = database.selectFromTableWhere("Messages", 
                                "messageID, messageUID, folderID, seqNo", 
                                "isValidMessage=1 AND messageTo LIKE '%" + extensionTag + "%'");
                        ArrayList<Integer> messageIDs = new ArrayList<Integer>(result.size());
                        for (Object[] line : result)
                        {
                            int id = (Integer) line[0];
                            String UID = (String) line[1];
                            int folderID = (Integer) line[2];
                            int seqNo = (Integer) line[3];
                            if (uidToMessageInfo.containsKey(UID))
                            {
                                MessageFolderInfo msgInfo = uidToMessageInfo.get(UID);
                                msgInfo.addFolder(folderID);
                            }
                            else
                            {
                                
                                messageIDs.add(id);
                                MessageFolderInfo msgInfo = new MessageFolderInfo(id, seqNo);
                                uidToMessageInfo.put(UID, msgInfo);
                            }
                        }
                        stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, messageIDs));
                    }
                }
                //Get id of tag
                else if (!selectList.isEmpty())
                {
                    if (selectList.size() > 1) {
                        throw new QueryParseException("Must use an operator between tags!");
                    }
                    String tag = selectList.get(0);
                    //Select from database
                    ArrayList<Object[]> result = database.selectFromTableWhere("Tags", "tagID", "tagValue='" + tag + "'");
                    //Get tag ID
                    if (!result.isEmpty())
                    {
                        int tagID = (Integer)result.get(0)[0];
                        ArrayList<Object[]> messageResults = database.selectFromTableWhere("MessagesToTags", "messageID", "tagID=" + Integer.toString(tagID));
                        selectedIDs = new ArrayList<Integer>(messageResults.size());
                        for (Object[] messageResult : messageResults)
                        {
                            int messageID = (Integer) messageResult[0];
                                                    ArrayList<Object[]> checkValidResult 
                                = database.selectFromTableWhere
                                ("Messages", 
                                "messageID", 
                                "messageID=" + Integer.toString(messageID) 
                                + " AND isValidMessage=1");
                            if (!checkValidResult.isEmpty())
                            {
                                selectedIDs.add(messageID);
                            }
                            System.out.println("Adding message: " + messageID);
                        }
                        
                        stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
                    }
                }
                //stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, new ArrayList<Integer>()));
            }
            //No operator found, throw exception
            else
            {
                throw new QueryParseException("Syntax error");
            }
        }
        //////////////////
        //OR Operator
        /////////////////
        else if (operator.equalsIgnoreCase(OPERATOR_OR))
        {
            operatorPrevFound = true;
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
                        ArrayList<Object[]> checkValidResult 
                                = database.selectFromTableWhere
                                ("Messages", 
                                "messageID", 
                                "messageID=" + Integer.toString(messageID) 
                                + " AND isValidMessage=1");
                        if (!checkValidResult.isEmpty())
                        {
                            messageIDs.add(messageID);
                        }
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
            
            if (extension != null)
            {
                String[] split = extension.split("\\s+");
                if (split.length != 2)
                {
                    throw new QueryParseException("Should have EXTENSION_OP Tag!");
                }
                String extensionOp = split[0];
                String extensionTag = split[1];
                
                if (extensionOp.equalsIgnoreCase(MESSAGE_FROM))
                {
                    ArrayList<Object[]> result 
                        = database.selectFromTableWhere("Messages", 
                        "messageID", "isValidMessage=1 AND messageFrom LIKE '%" + extensionTag + "%'");
                    
                    for (Object[] line : result)
                    {
                        int id = (Integer) line[0];
                        selectedIDs.add(id);
                    }
                }
            }
            
            //Now push to stack
            stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
            //printStack();
        }//OR operator
        
        //////////////
        //AND operator
        /////////////
        else if (operator.equalsIgnoreCase(OPERATOR_AND))
        {
            operatorPrevFound = true;
            //Get tag ids
            ArrayList<Integer> tagIDs = new ArrayList<Integer>(selectList.size());
            for (String tag : selectList)
            {
                ArrayList<Object[]> result = database.selectFromTableWhere("Tags", "tagID", "tagValue='" + tag + "'");
                if (!result.isEmpty())
                {
                    System.out.println("Found tagID: " + result.get(0)[0] + " for tag: " + tag);
                    int tagID = (Integer) result.get(0)[0];
                    tagIDs.add(tagID);
                }
                else
                {
                    System.out.println("No tagID found for tag: " + tag);
                    //Just add empty list and return
                    stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
                    return;
                }
            }
            //Easier case
            if (selectedIDs == null)
            {
                selectedIDs = new ArrayList<Integer>();
                if (!tagIDs.isEmpty())
                {
                    //Get id of message which contains at least one tag
                    ArrayList<Object[]> result;
                    String tagString = Integer.toString(tagIDs.get(0));
                    result = database.selectFromTableWhere("MessagesToTags", "messageID", "tagID=" + tagString);
                    
                    
                    if (result.isEmpty())
                    {
                        //Just add empty list and return
                        stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
                        return;
                    }
                    
                    //Build up WHERE clause of SQL query
                    for (Object[] line : result)
                    {
                        int messageID = (Integer) line[0];
                        String messageString = Integer.toString(messageID);

                        String whereSQL = "messageID=" + messageString + " AND (tagID=" + Integer.toString(tagIDs.get(0));
                        for (int i = 1; i < tagIDs.size(); i++)
                        {
                            whereSQL += " OR tagID=" + Integer.toString(tagIDs.get(i));
                        }
                        
                        whereSQL += ")";

                        //System.out.println("Where SQL: " + whereSQL);
                        result = database.selectFromTableWhere("MessagesToTags", "messageID", whereSQL);
                        System.out.println("Where sql: " + whereSQL + " result size: " + result.size() + " tagID size: " + tagIDs.size());
                        //if messagesToTags found is equal to the number of tags,
                        //we have found a message
                        if (result.size() == tagIDs.size())
                        {
                            ArrayList<Object[]> checkValidResult 
                                = database.selectFromTableWhere
                                ("Messages", 
                                "messageID", 
                                "messageID=" + Integer.toString(messageID) 
                                + " AND isValidMessage=1");
                            if (!checkValidResult.isEmpty())
                            {
                                selectedIDs.add(messageID);
                            }
                            //System.out.println("Found message: " + messageID);
                        }
                    }
                }
                
                if (extension != null)
                {
                    String[] split = extension.split("\\s+");
                    if (split.length != 2)
                    {
                        throw new QueryParseException("Should have EXTENSION_OP Tag!");
                    }
                    String extensionOp = split[0];
                    String extensionTag = split[1];
                    for (int i = 0; i < selectedIDs.size(); i++)
                    {
                        Integer id = selectedIDs.get(0);
                        String whereSQL = ""; 
                        if (extensionOp.equalsIgnoreCase(MESSAGE_FROM))
                        {
                            whereSQL += "isValidMessage=1 AND messageID=" + Integer.toString(id) + " AND messageFrom LIKE '%" + extensionTag + "%'";
                        }
                        else if (extensionOp.equalsIgnoreCase(MESSAGE_TO))
                        {
                            whereSQL += "isValidMessage=1 AND messageID=" + Integer.toString(id) + " AND messageTo LIKE '%" + extensionTag + "%'";
                        }
                        
                        ArrayList<Object[]> result = database.selectFromTableWhere("Messages", "messageID", whereSQL);
                        
                        if (result.isEmpty())
                        {
                            selectedIDs.remove(id);
                        }
                    }
                    
                }
                                
                //Push onto stack, even if empty
                stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, selectedIDs));
            } //if selectedIDs == null
            else
            {
                ArrayList<Integer> newData = new ArrayList<Integer>();
                //For each message in the list
                for (int i = 0; i < selectedIDs.size(); i++)
                {
                    int messageID = selectedIDs.get(i);
                    String messageString = Integer.toString(messageID);
                    
                    String whereSQL = "isValidMessage=1 AND messageID=" + messageString + " AND (tagID=" + Integer.toString(tagIDs.get(0));
                    for (int j = 1; j < tagIDs.size(); j++)
                    {
                        whereSQL += " OR tagID=" + Integer.toString(tagIDs.get(j));
                    }
                    whereSQL += ")";
                    
                    //System.out.println("Where SQL: " + whereSQL);
                    ArrayList<Object[]> result = database.selectFromTableWhere("MessagesToTags", "messageID", whereSQL);
                    //System.out.println("Message id: " + messageID + " result size: " + result.size() + " tags size: " + tagIDs.size());
                    //if messagesToTags found is equal to the number of tags,
                    //we have found a message
                    if (result.size() == tagIDs.size())
                    {
                        newData.add(messageID);
                        //System.out.println("Found message: " + messageID);
                    }
                }
                
                if (extension != null)
                {
                    String[] split = extension.split("\\s+");
                    if (split.length != 2)
                    {
                        throw new QueryParseException("Should have EXTENSION_OP Tag!");
                    }
                    String extensionOp = split[0];
                    String extensionTag = split[1];
                    for (int i = 0; i < newData.size(); i++)
                    {
                        Integer id = newData.get(0);
                        String whereSQL = ""; 
                        if (extensionOp.equalsIgnoreCase(MESSAGE_FROM))
                        {
                            whereSQL += "isValidMessage=1 AND messageID=" + Integer.toString(id) + " AND messageFrom LIKE '%" + extensionTag + "%'";
                        }
                        else if (extensionOp.equalsIgnoreCase(MESSAGE_TO))
                        {
                            whereSQL += "isValidMessage=1 AND messageID=" + Integer.toString(id) + " AND messageTo LIKE '%" + extensionTag + "%'";
                        }
                        
                        ArrayList<Object[]> result = database.selectFromTableWhere("Messages", "messageID", whereSQL);
                        
                        if (result.isEmpty())
                        {
                            newData.remove(id);
                        }
                    }
                    
                }
                
                stack.push(new QueryNode(QueryNodeType.NODE_MESSAGE_LIST, newData));
            }
        } //operator AND
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
    
    private boolean isExtensionWord(String word)
    {
        return word.equalsIgnoreCase(MESSAGE_FROM) 
                || word.equalsIgnoreCase(MESSAGE_TO)
                || word.equalsIgnoreCase(DATE_AFTER)
                || word.equalsIgnoreCase(DATE_BEFORE)
                || word.equalsIgnoreCase(DATE_IS);
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
        QueryParser parser = new QueryParser("Test OR (Computer AND Science)", UserDatabase.getInstance());
        try {
            ArrayList<Integer> result = parser.parseExpression();
            System.out.println("Result:\n" + result);
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
