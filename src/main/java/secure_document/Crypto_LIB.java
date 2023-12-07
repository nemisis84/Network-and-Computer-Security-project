package secure_document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto_LIB {

    public static byte[] generateKey(int keySize) throws NoSuchAlgorithmException{

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");

        keyGen.init(keySize);

        return keyGen.generateKey().getEncoded();
    }

    public static String Hmac(String target, String keyPath) throws Exception{
        Mac mac = Mac.getInstance("HmacSHA256");
        Key HmacKey = readSecretKey(keyPath);

        mac.init(HmacKey);
        return byteArrayToHexString(mac.doFinal(target.getBytes("utf-8")));
    }

    public static int check(String File, String hash, String keyPath) throws Exception {
        
        String hashFile = Hmac(File, keyPath);

        if(hash.equals(hashFile)){ // same as the original
            return 1;
        }

        return -1;

    }

    public static List <String> AES_encrypt(String target, String keyPath) throws Exception{

        byte[] audioBytes = target.getBytes();

        Key secretKey = readSecretKey(keyPath);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(audioBytes);

        String cipherString = Base64.getEncoder().encodeToString(encryptedBytes);
        String iv_send = Base64.getEncoder().encodeToString(iv);
        
        List <String> cipherList = new ArrayList<>();
        cipherList.add(cipherString);
        cipherList.add(iv_send);

        return cipherList;
        
    }

    public static String AES_decrypt(String target, String IV, String keyPath) throws Exception{

        Key secretKey = readSecretKey(keyPath);

        byte[] encryptedText = Base64.getDecoder().decode(target);
        byte[] iv = Base64.getDecoder().decode(IV);
        
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] originalBytes = cipher.doFinal(encryptedText);

        String original = new String(originalBytes);

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
        SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");
        return keySpec;
    }
}
