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

    public static String encrypt_message(String message, String key) throws Exception{

        return Crypto_LIB.AES_encrypt(message, key);

    }

    public static String decrypt_message(String message, String key) throws Exception{


        String cipher = message.split(" ")[0];
        String IV = message.split(" ")[1];
        byte[] cnt = Base64.getDecoder().decode(message.split(" ")[2]);
        return Crypto_LIB.AES_decrypt(cipher, IV, cnt, key);

    }

    public static void unprotect(String message, String keyPath, String sesskeyPath) throws Exception {
        
        String out_file = "Music/";        

        // decipher message and retreive it
        String clearMessage = decrypt_message(message , sesskeyPath);
        String hash_protected = clearMessage.split("\n")[0];
        String protected_file = clearMessage.split("\n")[1];
        String ivFile = clearMessage.split("\n")[2];
        byte[] cnt = Base64.getDecoder().decode(clearMessage.split("\n")[3]);

        // check the autenticity
        if(Crypto_LIB.check(protected_file, hash_protected, keyPath) == -1){
            System.err.println("The file is not autenthic");
            return;
        } 


        JsonObject jsonObject = JsonParser.parseString(protected_file).getAsJsonObject();
        String musicEnc = jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").get("audioBase64").getAsString();

        int divisions = musicEnc.length() / 64;
        System.out.println("You can divide the song into " + divisions + " sections.");
        Scanner scanner = new Scanner(System.in);
        int div = 0;
        while(true){
            System.out.println("Select from where you want to start. 0 - " + divisions);
            div = Integer.valueOf(scanner.nextLine());

            if(div >= 0 && div <= divisions)
                break;
        }       

        String musicDes = Crypto_LIB.AES_decrypt(musicEnc, ivFile, cnt, keyPath);

        jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").addProperty("audioBase64", musicDes);
        String songFile = jsonObject.toString();

        String musicDiv = musicEnc.substring(div*64, musicEnc.length());

        cnt = intToByteArray(div*3);

        String music = Crypto_LIB.AES_decrypt(musicDiv, ivFile, cnt, keyPath);

        export_song_files(songFile, music, out_file);     
    }


    public static String protect(String songFilePath, String keyPath, String sesskeyPath) throws Exception{    

        FileReader fileReader = new FileReader(songFilePath);
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(fileReader, JsonObject.class);
        String music = jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").get("audioBase64").getAsString();
        
        // cipher audio file
        String cipherB64d = Crypto_LIB.AES_encrypt(music, keyPath);
        String musicEnc = cipherB64d.split(" ")[0];
        String iv = cipherB64d.split(" ")[1];
        String cnt = cipherB64d.split(" ")[2];

        jsonObject.getAsJsonObject("media").getAsJsonObject("mediaContent").addProperty("audioBase64", musicEnc);
        String protected_file = jsonObject.toString();

        // produces Hmac of protected song file
        String hash_protected = Crypto_LIB.Hmac(protected_file, keyPath);

        String send_message = hash_protected;
        send_message += "\n" + protected_file;
        send_message += "\n" + iv;
        send_message += "\n" + cnt;
        
        return encrypt_message(send_message, sesskeyPath);
                    
    } 

    public static void export_song_files(String songFile, String mp3File, String out_file) throws IOException{

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


        // write mp3 file
        byte[] data = DatatypeConverter.parseBase64Binary(mp3File);
        File file = new File(out_file + artist + " - " + title + format);
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file));
        outputStream.write(data);
        outputStream.close();

    }

    public static void saveKey_toFile(String message, String key_path, String store_path) throws Exception{
        
        String clearMessage = null;
        if(key_path == null){
            clearMessage = message;
        }
        else{
            clearMessage = Crypto_LIB.AES_decrypt(message.split(" ")[0], message.split(" ")[1] , Base64.getDecoder().decode(message.split(" ")[2]), key_path);
        }
        
        byte[] key = clearMessage.getBytes();
        
        FileOutputStream fos = new FileOutputStream(store_path);
        fos.write(key);
        fos.close();
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }


    public static void main(String[] args) throws Exception{
             
    }
}
