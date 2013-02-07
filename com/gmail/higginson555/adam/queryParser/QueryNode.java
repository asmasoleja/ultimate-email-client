package com.gmail.higginson555.adam.queryParser;

/**
 *
 * @author Adam
 */
public class QueryNode
{
    
    public static enum QueryNodeType { NODE_MESSAGE_LIST, NODE_OPERATOR, NODE_TAG, NODE_OPEN_BRACKET };
    
    private QueryNodeType type;
    private Object data;
    
    public QueryNode(QueryNodeType type, Object data)
    {
        this.type = type;
        this.data = data;
    }

    public QueryNodeType getType() {
        return type;
    }

    public Object getData() {
        return data;
    }  
}
