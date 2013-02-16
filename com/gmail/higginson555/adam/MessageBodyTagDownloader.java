/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import org.jsoup.Jsoup;

/**
 *
 * @author Adam
 */
public class MessageBodyTagDownloader extends Thread
{
    //The folder to download and get message bodies from
    private Folder folder;
    private int folderID;
    
    private int level;
    private String body;
    
    
    public MessageBodyTagDownloader(Folder folder, int folderID) throws MessagingException
    {
        this.folder = folder;
        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }
        this.folderID = folderID;
    }
    
    @Override
    public void run()
    {
        try {
            folder.open(Folder.READ_ONLY);
            Message[] messages = folder.getMessages();
            for (int i = 0; i < messages.length; i++)
            {
                if (ClientThreadPool.shouldStop)
                {
                    return;
                }
                Message message = messages[i];
                writeMessage(message);
                String withoutHTML = Jsoup.parse(body).text();
                int messageID = (Integer) (UserDatabase.getInstance().selectFromTableWhere(
                        "Messages", "messageID", 
                        "messageNo=" + Integer.toString(message.getMessageNumber()) + " AND folderID=" + Integer.toString(folderID))
                        .get(0)[0]);
                ArrayList<String> tags = TagParser.getInstance().getTags(withoutHTML);
                TagParser.getInstance().insertTags(UserDatabase.getInstance(), tags, messageID);
                
                        //Set tags to be extracted
                UserDatabase.getInstance().updateRecord("Messages", "areTagsExtracted=1", "messageID=" + Integer.toString(messageID));
            }
                
        } catch (MessagingException ex) {
            Logger.getLogger(MessageBodyTagDownloader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(MessageBodyTagDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void writeMessage(Part part) throws Exception 
    {            
        //We use isMimeType to determine the content type, avoiding
        //having to get the content data until we need it
        if (part.isMimeType("text/plain") || part.isMimeType("TEXT/PLAIN"))
        {

            //bodyTextPane.setContentType("text/plain");
            //System.out.println("Found plain");
            //System.out.println("\nPlain Section:\n" + part.getContent());
            //body += (String)part.getContent();
        }
        else if (part.isMimeType("text/html") || part.isMimeType("TEXT/HTML"))
        {
            //bodyTextPane.setContentType("text/html");
            //System.out.println("Found html");
            body += (String)part.getContent();
            //this.messageBody = part;
        }
        else if (part.isMimeType("multipart/*"))
        {
            //System.out.println("Found multipart/*");
            Multipart multipart = (Multipart)part.getContent();
            level++; //Increment the level...
            int count = multipart.getCount();
            for (int i = 0; i < count; ++i) {
                writeMessage(multipart.getBodyPart(i));
            }
            level--; //We're out, so decrement the level
        }
        else if (part.isMimeType("message/rfc822"))
        {
            System.out.println("Found message/rfc822");
            level++; //Again increment as we're going 'deeper' into the message
            writeMessage((Part)part.getContent());
            level--; //Decrement level
        }
    }
}
