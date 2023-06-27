package com.naukma.clientserver.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = md.digest(password.getBytes());
            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String convertByteArrayToHexString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array)
            sb.append(Integer.toHexString((b & 0xFF) | 0x100), 1, 3);

        return sb.toString();
    }
}