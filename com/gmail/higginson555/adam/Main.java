/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.higginson555.adam;

import com.gmail.higginson555.adam.gui.LoginScreen;

/**
 *
 * @author Adam
 */
public class Main 
{
    public static void main(String[] args)
    {
        System.out.println("Started ultimate e-mail client...");
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }
    
}
