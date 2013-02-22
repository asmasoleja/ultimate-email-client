/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Adam
 */
public class ClientThreadPool 
{
    public static boolean shouldStop = false;
    public static final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    /**
     * Find message queue, add messages to this queue to try and find
     * the location of the message. The top of the queue will always be
     * examined first. Causes the background thread to stop and try and 
     * find queue data.
     */
    public static final ConcurrentLinkedQueue<Object[]> findMessageQueue 
            = new ConcurrentLinkedQueue<Object[]>();
}
