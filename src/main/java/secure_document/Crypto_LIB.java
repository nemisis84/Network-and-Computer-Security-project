package secure_document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto_LIB {

    public static String generateKey(int keySize) throws NoSuchAlgorithmException{

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        keyGen.init(keySize);

    
        return Base64.getEncoder().encodeToString(keyGen.generateKey().getEncoded());
    }

    public static String Hmac(String target, String keyPath) throws Exception{
        Mac mac = Mac.getInstance("HmacSHA256");
        Key HmacKey = readSecretKey(keyPath);

        mac.init(HmacKey);
        return byteArrayToHexString(mac.doFinal(target.getBytes("utf-8")));
    }

    public static int check(String File, String hash, String keyPath) throws Exception {
        System.out.println("Checking authenticity of data...\n\nRecieved tag: "+hash);
        String hashFile = Hmac(File, keyPath);
        System.err.println("\nCalulated hash: "+hashFile+"\n\nChecking if tags match...");
        
        if(hash.equals(hashFile)){ // same as the original
            System.err.println("It's a match, authenticity ensured!");
            return 1;
        }

        return -1;

    }

    public static String AES_encrypt(String target, String keyPath) throws Exception{
        System.err.println("Encrypting using AES/CTR/NoPadding...");
        byte[] audioBytes = target.getBytes();

        Key secretKey = readSecretKey(keyPath);

        Cipher c = Cipher.getInstance("AES/CTR/NoPadding");

        byte[] iv = new byte[16]; 
        new SecureRandom().nextBytes(iv);
        byte[] cnt = new byte[3];
        Arrays.fill( cnt, (byte) 0 );

        Array.setByte(iv, 13, cnt[0]);
        Array.setByte(iv, 14, cnt[1]);
        Array.setByte(iv, 15, cnt[2]);

        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        c.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encryptedBytes = c.doFinal(audioBytes);

        String cipherString = Base64.getEncoder().encodeToString(encryptedBytes);
        String iv_send = Base64.getEncoder().encodeToString(iv);
        String cnt_send = Base64.getEncoder().encodeToString(cnt);
        
        String cipher = cipherString + " " + iv_send + " " + cnt_send;

        System.err.println("Encrypting data using key: " +secretKey+ "\nStart of cipher: "+cipherString.substring(0, 20)+"...");

        return cipher;
        
    }

    public static String AES_decrypt(String target, String IV, byte[] cnt, String keyPath) throws Exception{

        Key secretKey = readSecretKey(keyPath);
        
        System.err.println("Decrypting data using key: " +secretKey+ "\nStart of cipher: "+target.substring(0, 20)+"...");

        byte[] encryptedText = Base64.getDecoder().decode(target);
        byte[] iv = Base64.getDecoder().decode(IV);

        //encryptedText = Arrays.copyOfRange(encryptedText, 16*127, encryptedText.length);

        Array.setByte(iv, 13, cnt[0]);
        Array.setByte(iv, 14, cnt[1]);
        Array.setByte(iv, 15, cnt[2]);
        
        IvParameterSpec ivSpec = new IvParameterSpec(iv); 
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] originalBytes = cipher.doFinal(encryptedText);

        String original = new String(originalBytes);

        System.err.println("Decrypting complete. Start of data: "+ original.substring(0, 20)+"...");

        return original;
        
    }

    private static String byteArrayToHexString(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }

    private static byte[] readFile(String path) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static Key readSecretKey(String secretKeyPath) throws Exception {
        byte[] encoded = readFile(secretKeyPath);
        SecretKeySpec keySpec = new SecretKeySpec(Base64.getDecoder().decode(encoded), "AES");
        return keySpec;
    }
}
