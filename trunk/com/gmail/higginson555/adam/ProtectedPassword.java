package com.gmail.higginson555.adam;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Encrypts/Decrypts a given user password.
 * Based heavily off of: 
 * http://stackoverflow.com/questions/1132567/encrypt-password-in-configuration-files-java
 */
public class ProtectedPassword 
{
    private static final char[] PASSWORD = "asgwqejhapdfghjklddfj".toCharArray();
    private static final byte[] SALT  = {
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };
    
    public static String encrypt(String prop) throws GeneralSecurityException
    {
        SecretKeyFactory sKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDes");
        SecretKey key = sKeyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new BASE64Encoder().encode(cipher.doFinal(prop.getBytes()));
    }
    
    public static String decrypt(String prop) throws GeneralSecurityException, IOException
    {
        SecretKeyFactory sKeyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = sKeyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");
        cipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(prop)));
    }
}
