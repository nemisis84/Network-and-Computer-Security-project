package secure_document;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class API_server {

    public static String encrypt_message(String message, String key) throws Exception{

        return Crypto_LIB.AES_encrypt(message, key);

    }

    public static String decrypt_message(String message, String key) throws Exception{

        String cipher = message.split(" ")[0];
        String IV = message.split(" ")[1];
        return Crypto_LIB.AES_decrypt(cipher, IV, key);

    }

    public static String protect(String File, String keyPath, String sesskeyPath) throws Exception {
        
        JsonObject jsonObject = JsonParser.parseString(File).getAsJsonObject();
        String music = jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").get("audioBase64").getAsString();
        
        // cipher audio file
        String cipherB64d = Crypto_LIB.AES_encrypt(music, keyPath);
        String musicEnc = cipherB64d.split(" ")[0];
        String iv = cipherB64d.split(" ")[1];


        jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").addProperty("audioBase64", musicEnc);
        String protected_file = jsonObject.toString();

        // produces Hmac of protected song file
        String hash_protected = Crypto_LIB.Hmac(protected_file, keyPath);

        String send_message = hash_protected;
        send_message += "\n" + protected_file;
        send_message += "\n" + iv;


        
        return encrypt_message(send_message, sesskeyPath);
            
    }

    public static String unprotect(String message, String keyPath, String sesskeyPath) throws Exception {

        
        // decipher message and retreive it
        String clearMessage = decrypt_message(message , sesskeyPath);
        String hash_protected = clearMessage.split("\n")[0];
        String protected_file = clearMessage.split("\n")[1];
        String ivFile = clearMessage.split("\n")[2];

        // check the autenticity
        if(Crypto_LIB.check(protected_file, hash_protected, keyPath) == -1){
            System.err.println("The file is not autenthic");
            return null;
        }

        JsonObject jsonObject = JsonParser.parseString(protected_file).getAsJsonObject();
        String musicEnc = jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").get("audioBase64").getAsString();
        
        String music = Crypto_LIB.AES_decrypt(musicEnc, ivFile, keyPath);

        jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").addProperty("audioBase64", music);
        String songFile = jsonObject.toString();

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

    public static void createKey(int keySize, String store_path) throws Exception{

        String keysent = Crypto_LIB.generateKey(keySize);

        FileOutputStream fos = new FileOutputStream(store_path);
        fos.write(keysent.getBytes());
        fos.close();

    }

    public static String readKey(String keyPath, String enckey_path) throws Exception{

        File f = new File(keyPath);
        Scanner myReader = new Scanner(f);
        String keysent = myReader.nextLine();
        myReader.close();

        String message = keysent;
        if(enckey_path != null)
            message = Crypto_LIB.AES_encrypt(keysent, enckey_path);

        return message;
    }

    public static void main(String[] args) throws Exception{

    }
}
