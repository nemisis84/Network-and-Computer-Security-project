package secure_document;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class API_server {
    public static String protect(String musicFile, String keyPath) throws Exception {
        
        // produces Hmac of original unprotected song file
        String hash_org = Crypto_LIB.Hmac(musicFile, keyPath);
            
        // cipher audio file
        String cipherB64d = Crypto_LIB.AES_encrypt(musicFile, keyPath);
        String cipherFile = cipherB64d.split(" ")[0];
        String iv = cipherB64d.split(" ")[1];

        // produces Hmac of protected song file
        String hash_sec = Crypto_LIB.Hmac(cipherFile, keyPath);
        

        String send_message = "";
        send_message += hash_org;
        send_message += " " + hash_sec;
        send_message += " " + cipherFile;
        send_message += " " + iv;
        
        String enc_message = Crypto_LIB.AES_encrypt(send_message, keyPath);

        return enc_message;
            
    }

    public static String unprotect(String message, String keyPath) throws Exception {
 
        // receives message cipher and its IV
        String cipherMessage = message.split(" ")[0];
        String IV_message = message.split(" ")[1];

        // decipher message and retreive it
        String clearMessage = Crypto_LIB.AES_decrypt(cipherMessage, IV_message , keyPath);
        String hash_org = clearMessage.split(" ")[0];
        String hash_sec = clearMessage.split(" ")[1];
        String cipherFile = clearMessage.split(" ")[2];
        String ivFile = clearMessage.split(" ")[3];

        // check the confidentiality
        if(Crypto_LIB.check(cipherFile, hash_sec, keyPath) == -1){
            System.err.println("The encryption file is not correct.");
            return null;
        }

        // decypher song file
        String songFile = Crypto_LIB.AES_decrypt(cipherFile, ivFile, keyPath);

        // check the autenticity
        if(Crypto_LIB.check(songFile, hash_org, keyPath) == -1){
            System.err.println("The song file is unautentic.");
            return null;
        }

        // check if the song is compatible with our database
        if(check_song_content(songFile) == -1){
            System.err.println("Song file not compatible");
            return null;
        }
        
        return songFile;
    }
    
    public static int check_song_content(String File){

        JsonObject Json = JsonParser.parseString(File).getAsJsonObject();            
        JsonObject media = Json.get("media").getAsJsonObject();
        if(media == null){return -1;}

        // get mediaInfo and check it
        JsonObject mediaInfo = media.get("mediaInfo").getAsJsonObject();
        if(mediaInfo == null){return -1;}
        if(mediaInfo.get("owner") == null){return -1;}
        if(mediaInfo.get("format") == null){return -1;}
        if(mediaInfo.get("artist") == null){return -1;}
        if(mediaInfo.get("title") == null){return -1;}
        if(mediaInfo.get("genre") == null){return -1;}

        // get mediaContent and check it
        JsonObject mediaContent = media.get("mediaContent").getAsJsonObject();
        if(mediaContent == null){return -1;}
        if(mediaContent.get("lyrics") == null){return -1;}
        if(mediaContent.get("audioBase64") == null){return -1;}

        return 1;
    }

    public static String create_secretKey(int keySize, String path) throws Exception{

        String keysent = Crypto_LIB.generateKey(keySize);

        FileOutputStream fos = new FileOutputStream(path + "/secret.key");
        fos.write(keysent.getBytes());
        fos.close();

        return keysent;
    }

    public static String create_sessionKey(int keySize, String path) throws Exception{

        String keysent = Crypto_LIB.generateKey(keySize);

        String message = Crypto_LIB.AES_encrypt(keysent, path + "/secret.key");

        FileOutputStream fos = new FileOutputStream(path + "/session.key");
        fos.write(keysent.getBytes());
        fos.close();

        return message;
    }
    
    public static void main(String[] args) throws Exception{


        // TO RUN: mvn compile exec:java -Dmainclass=secure_document.API_server


        // Simulation of receiving data from database and protecting it to sendo to the client
        //FileReader fileReader = new FileReader("resources/secure_doc.json");
        //Gson gson = new Gson();
        //JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);
        //String enc_message = protect(rootJson.toString(), "resources/clientX_session.key");
        //System.out.println(enc_message);



        // Simulation of receiving a song from client (artist) to add to database
        //String message = "";
        //String songFile = unprotect(message, "resources/clientX_session.key");


        //generates a secret key and saves it to file
        //String message = create_sessionKey(256, "resources/clientX_session.key");
        //System.out.println(message);

    }
}
