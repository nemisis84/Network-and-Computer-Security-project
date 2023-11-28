package src.secure_document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class API {
    public static void protect(String in_file, String out_file) throws Exception {
        
        try (FileReader fileReader = new FileReader(in_file)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);

            String hash_org = Crypto_LIB.Hmac(rootJson.toString(), "bob1234");
            FileWriter fileWriter = new FileWriter("resources/message.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(hash_org + "\n");

            JsonObject media = rootJson.get("media").getAsJsonObject();
            JsonObject mediaContent = media.get("mediaContent").getAsJsonObject();

            String audioBase64 = mediaContent.get("audioBase64").getAsString();
            
            String cipherB64dString = Crypto_LIB.AES_encrypt(audioBase64, "resources/secret.key");

            mediaContent.addProperty("audioBase64", cipherB64dString);
            media.add("mediaContent", mediaContent);
            rootJson.add("media", media);

            // Write JSON object to file
            try (FileWriter fileWriter2 = new FileWriter(out_file)) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(rootJson, fileWriter2);
            }


            String hash_sec = Crypto_LIB.Hmac(rootJson.toString(), "bob1234");
            String nonce = "4J8pirLzX6oIF0IIIaUU";  
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());  
            printWriter.print(hash_sec + "\n");
            printWriter.print(nonce + "\n");  // nonce
            printWriter.print(timeStamp);  // timestamp
            printWriter.close();
            
        }
    }

    public static void unprotect(String in_file, String out_file) throws Exception {
        
        
        try (FileReader fileReader = new FileReader(in_file)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);
            JsonObject media = rootJson.get("media").getAsJsonObject();
            JsonObject mediaContent = media.get("mediaContent").getAsJsonObject();

            String cipherB64dString = mediaContent.get("audioBase64").getAsString();
            
            String original = Crypto_LIB.AES_decrypt(cipherB64dString, "resources/secret.key");

            mediaContent.addProperty("audioBase64", original);
            media.add("mediaContent", mediaContent);
            rootJson.add("media", media);

            // Write JSON object to file
            try (FileWriter fileWriter = new FileWriter(out_file)) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(rootJson, fileWriter);
            } 
            
        }
        
        
    }

    public static int check(String in_file) throws Exception {
        
        // Read JSON object from file, and print its contets
        try (FileReader fileReader = new FileReader(in_file)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);
            
            //System.out.println("JSON object: " + rootJson + "\n");

            BufferedReader br= new BufferedReader(new FileReader("Files/message.txt"));
            String hash_org = br.readLine();
            String hash_sec = br.readLine();
            br.close();
            String hash_read = Crypto_LIB.Hmac(rootJson.toString(), "bob1234");

            if(hash_org.equals(hash_read)){//unprotected
                return 0;
            }
            else if(hash_sec.equals(hash_read)){//protected
                return 1;
            }
            else{
                return -1;
                
            }
        }
    }

    


    public static void main(String[] args) throws Exception{
        
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            return;
        }

        if(args[0].equals("protect") || args[0].equals("Protect")){
            if (args.length < 0) {
                System.err.println("Argument(s) missing!");
                return;
            }

            System.out.println("Protect()");
            protect("resources/secure_doc.json", "resources/encrypted_file");
        }
        
        else if(args[0].equals("unprotect") || args[0].equals("Unprotect")){
            if (args.length < 0) {
                System.err.println("Argument(s) missing!");
                return;
            }
            System.out.println("Unprotect()");
            int flag = check("Files/secure_doc.json");
            if(flag != 1){
                if(flag == -1){
                    System.out.println("The document was tempered with");
                    return;
                }
                if(flag == 0){
                    System.out.println("The document is already unprotected");
                    return;
                }
            }
            unprotect("Files/secure_doc.json", "Files/secure_doc.json");
        }

        else if(args[0].equals("check") || args[0].equals("Check")){
            int flag = check("Files/secure_doc.json");
            if(flag == 1){
                System.out.println("The document is protected");
                return;
            }

            if(flag == -1){
                System.out.println("The document was tempered with");
                return;
            }

            if(flag == 0){
                System.out.println("The document is unprotected");
                return;
            }
            
        }
    }
}
