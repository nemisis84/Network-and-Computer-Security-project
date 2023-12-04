package secure_document;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto_LIB {

    public static String Hmac(String target, String key) throws NoSuchAlgorithmException, IllegalStateException, UnsupportedEncodingException, InvalidKeyException{
        Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec HmacKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(HmacKey);
            return byteArrayToHexString(mac.doFinal(target.getBytes("utf-8")));
    }

    public static String AES_encrypt(String target, String keyPath) throws Exception{

        byte[] audioBytes = target.getBytes();

        Key secretKey = readSecretKey(keyPath);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        byte[] encryptedBytes = cipher.doFinal(audioBytes);
        byte[] encryptedIVAndText = new byte[iv.length + encryptedBytes.length];
        System.arraycopy(iv, 0, encryptedIVAndText, 0, iv.length);
        System.arraycopy(encryptedBytes, 0, encryptedIVAndText, iv.length, encryptedBytes.length);

        String cipherString = Base64.getEncoder().encodeToString(encryptedIVAndText);

        return cipherString;
        
    }

    public static String AES_decrypt(String target, String keyPath) throws Exception{

        Key secretKey = readSecretKey(keyPath);

        byte[] encryptedIVAndText = Base64.getDecoder().decode(target);

        byte[] iv = Arrays.copyOfRange(encryptedIVAndText, 0, 16);
        byte[] cipherBytes = Arrays.copyOfRange(encryptedIVAndText, 16, encryptedIVAndText.length);
        
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

        byte[] originalBytes = cipher.doFinal(cipherBytes);

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
