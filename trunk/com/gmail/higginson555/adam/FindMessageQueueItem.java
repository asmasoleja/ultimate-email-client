/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.PropertyListener;

/**
 * A class which holds information about messages that are lost and are
 * desired to be found again
 * @author Adam
 */
public class FindMessageQueueItem 
{
    private PropertyListener listener;
    private Object[] oldMessageData;

    public FindMessageQueueItem(PropertyListener listener, Object[] oldMessageData) {
        this.listener = listener;
        this.oldMessageData = oldMessageData;
    }

    public PropertyListener getListener() {
        return listener;
    }

    public Object[] getOldMessageData() {
        return oldMessageData;
    }
}
