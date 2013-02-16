/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

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
}
