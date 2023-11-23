package secure_document;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESencryption {
    
    public static String protect(String plainText, String keyString)throws Exception  {
        byte[] keyBytes = keyString.getBytes();

        Key key = new SecretKeySpec(keyBytes, "AES");

        Cipher cipher;
        cipher = Cipher.getInstance("AES/CTR/NoPadding");
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);


        System.out.println("Ciphering ...");
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
        byte[] encryptedIVAndText = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedIVAndText, iv.length, encryptedBytes.length);

        return Base64.getEncoder().encodeToString(encryptedIVAndText);
    }

    public static String unprotect(String cipherText, String keyString) throws Exception{
        byte[] keyBytes = keyString.getBytes();
        Key key = new SecretKeySpec(keyBytes, "AES");
        
        byte[] encryptedIVAndText = Base64.getDecoder().decode(cipherText);

        byte[] iv = Arrays.copyOfRange(encryptedIVAndText, 0, 16);
        byte[] cipherBytes = Arrays.copyOfRange(encryptedIVAndText, 16, encryptedIVAndText.length);
        
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        System.out.println("IV: " + ivSpec.getIV());
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        System.out.println("Deciphering ...");
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);

        byte[] originalBytes = cipher.doFinal(cipherBytes);

        return new String(originalBytes);
    }

    public static void main(String[] args) throws Exception {
        //Information and key
        String text = "hello world";
        String key = "1234567890123456";
        // Encryption
        String encrypted_text = AESencryption.protect(text, key);
        System.out.println(encrypted_text);

        // Decryption
        String plaintext = unprotect(encrypted_text, key);
        System.err.println(plaintext);

    }
}
