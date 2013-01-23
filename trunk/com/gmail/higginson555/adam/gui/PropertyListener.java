package com.gmail.higginson555.adam.gui;

/**
 * Allows a class to listen to some data class.
 * When the data class updates some data, it calls each of its listener's 
 * onPropertyEvent.
 * @author Adam
 */
public interface PropertyListener 
{
    public void onPropertyEvent(Class source, String name, Object value);
    
}
