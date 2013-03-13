package com.gmail.higginson555.adam;

import java.util.ArrayList;
import java.util.List;

/**
 * A class which has information about which folders a message id belongs
 * to in the database.
 * @author Adam
 */
public class MessageFolderInfo 
{
    //The message id in the database
    private int messageID;
    //The message sequence number
    private long seqNo;
    //The list of folder ids the message belongs to
    private ArrayList<Integer> folderIDs;

    public MessageFolderInfo(int messageID, long seqNo) {
        this.messageID = messageID;
        this.seqNo = seqNo;
        this.folderIDs = new ArrayList<Integer>();
    }
    
    public void addFolder(int folderID)
    {
        folderIDs.add(folderID);
    }
    
    public List<Integer> getFolderNames()
    {
        return folderIDs;
    }
    
    public int getMessageID()
    {
        return messageID;
    }
    
    public long getSeqNo()
    {
        return seqNo;
    }
}
