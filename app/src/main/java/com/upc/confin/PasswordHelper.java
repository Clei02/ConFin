package com.upc.confin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHelper {

    /**
     * Encripta una contraseña usando SHA-256
     */
    public static String encryptPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Verifica si una contraseña coincide con el hash
     */
    public static boolean verifyPassword(String password, String hashedPassword) {
        String hashedInput = encryptPassword(password);
        return hashedInput != null && hashedInput.equals(hashedPassword);
    }
}