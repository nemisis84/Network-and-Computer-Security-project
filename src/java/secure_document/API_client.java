package secure_document;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import javax.xml.bind.DatatypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class API_client {

    public static void unprotect(String message, String keyPath) throws Exception {
        
        String out_file = "Music/";

        String cipherMessage = message.split(" ")[0];
        String IV_message = message.split(" ")[1];

        String clearMessage = Crypto_LIB.AES_decrypt(cipherMessage, IV_message , keyPath);
        String hash_org = clearMessage.split(" ")[0];
        String hash_sec = clearMessage.split(" ")[1];
        String cipherFile = clearMessage.split(" ")[2];
        String ivFile = clearMessage.split(" ")[3];

        //check() the confidentiality 
        if(Crypto_LIB.check(cipherFile, hash_sec, keyPath) == -1){
            System.err.println("The encryption file is not correct.");
            return;
        }

        String songFile = Crypto_LIB.AES_decrypt(cipherFile, ivFile, keyPath);

        //check() the autenticity
        if(Crypto_LIB.check(songFile, hash_org, keyPath) == -1){
            System.err.println("The song file is unautentic.");
            return;
        }

		export_song_files(songFile, out_file);
        
    }

    public static String protect(String songFilePath, String keyPath) throws Exception{

        FileReader fileReader = new FileReader(songFilePath);
        Gson gson = new Gson();
        JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);
        String musicFile = rootJson.toString();
            
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

    public static void export_song_files(String songFile, String out_file) throws IOException{

        JsonObject Json = JsonParser.parseString(songFile).getAsJsonObject();
        JsonObject media = Json.get("media").getAsJsonObject();

        // get artist and music names and format
        JsonObject mediaInfo = media.get("mediaInfo").getAsJsonObject();
        String artist = mediaInfo.get("artist").getAsString();
        String title = mediaInfo.get("title").getAsString();
        String format = "." + mediaInfo.get("format").getAsString();

        // Write JSON object to file
        try (FileWriter fileWriter = new FileWriter(out_file + artist + " - " + title + ".json")) {
            Gson gson = new Gson();
            gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(Json, fileWriter);
        } 


        // get audio file
        JsonObject mediaContent = media.get("mediaContent").getAsJsonObject();
        String audioBase64 = mediaContent.get("audioBase64").getAsString();

        // write mp3 file
        byte[] data = DatatypeConverter.parseBase64Binary(audioBase64);
        File file = new File(out_file + artist + " - " + title + format);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        outputStream.write(data);
        outputStream.close();

    }

    public static void save_secretKey_toFile(String message, String path) throws Exception{
        
        byte[] key = message.getBytes();
        
        FileOutputStream fos = new FileOutputStream(path + "_secret.key");
        fos.write(key);
        fos.close();
    }

    public static void save_sessionKey_toFile(String message, String clientpath, String path) throws Exception{
        String clearMessage = Crypto_LIB.AES_decrypt(message.split(" ")[0], message.split(" ")[1] , clientpath + "_secret.key");
        
        byte[] key = clearMessage.getBytes();
        
        FileOutputStream fos = new FileOutputStream(path + "_session.key");
        fos.write(key);
        fos.close();
    }


    public static void main(String[] args) throws Exception{


        // TO RUN: mvn compile exec:java -Dmainclass=secure_document.API_client

        // Simulate receiving a song and processing it
        //String message = "";
        //unprotect(message, "resources/secret.key");


        
        // Silulate sending a song to add to database
        //String message = protect("resources/secure_doc.json", "resources/secret.key");
        //System.out.println(message);


        // Simulate receiving the key by server on create account and saving it
        //String message = "";
        //save_sessionKey_toFile(message, "resources/session.key");
    }
}
