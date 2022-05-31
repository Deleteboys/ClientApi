package de.deleteboys.clientapi.methods;

import javax.crypto.Cipher;
import java.security.*;
import java.util.Base64;

public class RSA {

    private PrivateKey privateKey;
    public PublicKey publicKey;

    public RSA() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String encrypt(String message, PublicKey key) {
        try {
            byte[] messageToBytes = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(messageToBytes);
            return encode(encryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String encode(byte[] date) {
        return Base64.getEncoder().encodeToString(date);
    }

    public String decrypt(String message) {
        try {
            byte[] encryptedBytes = decode(message);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE,privateKey);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage,"UTF8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decode(String data) {
        return Base64.getDecoder().decode(data);
    }

}
