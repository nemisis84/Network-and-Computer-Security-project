import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class API {
    public static void protect(String in_file, String out_file) throws Exception {
        
        try (FileReader fileReader = new FileReader(in_file)) {
            Gson gson = new Gson();
            JsonObject rootJson = gson.fromJson(fileReader, JsonObject.class);

            String HmacPass = "bob1234";
            String hash_org = Crypto_LIB.Hmac(rootJson.toString(), HmacPass);
            
            JsonObject media = rootJson.get("media").getAsJsonObject();
            JsonObject mediaContent = media.get("mediaContent").getAsJsonObject();

            String audioBase64 = mediaContent.get("audioBase64").getAsString();
            
            // cipher audio file
            String cipherB64dString = Crypto_LIB.AES_encrypt(audioBase64, "resources/secret.key");

            mediaContent.addProperty("audioBase64", cipherB64dString);
            media.add("mediaContent", mediaContent);
            rootJson.add("media", media);

            // Write JSON object to file
            try (FileWriter fileWriter = new FileWriter(out_file)) {
                gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(rootJson, fileWriter);
            }


            FileWriter fileWriter2 = new FileWriter("resources/message.txt");
            PrintWriter printWriter2 = new PrintWriter(fileWriter2);
            String hash_sec = Crypto_LIB.Hmac(rootJson.toString(), HmacPass);
            String nonce = "4J8pirLzX6oIF0IIIaUU";  
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());  
            
            /*List<String> message = new ArrayList<>();
            message.add(hash_org);
            message.add(hash_sec);
            message.add(nonce);
            message.add(timeStamp);
            message.add(HmacPass);           
            printWriter2.print(message); */
            
            String message = "";
            message += hash_org;
            message += " " + hash_sec;
            message += " " + nonce;
            message += " " + timeStamp;
            message += " " + HmacPass;

            String enc_message = Crypto_LIB.AES_encrypt(message, "resources/secret.key");

            printWriter2.print(enc_message);
            /*printWriter2.print(hash_org + "\n");
            printWriter2.print(hash_sec + "\n");
            printWriter2.print(nonce + "\n");  // nonce
            printWriter2.print(timeStamp+"\n");  // timestamp
            printWriter2.print(HmacPass); // pass for Hmac */
            printWriter2.close();
            
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

            BufferedReader br= new BufferedReader(new FileReader("resources/message.txt"));
            String enc_message = br.readLine();
            br.close();
            String[] message = Crypto_LIB.AES_decrypt(enc_message, "resources/secret.key").split(" ");

            String hash_org = message[0];
            String hash_sec = message[1];
            String HmacKey = message[4];
            String hash_file = Crypto_LIB.Hmac(rootJson.toString(), HmacKey);

            if(hash_org.equals(hash_file)){//unprotected
                return 0;
            }
            else if(hash_sec.equals(hash_file)){//protected
                return 1;
            }
            else{
                return -1;
                
            }  
        }
    }

    


    public static void main(String[] args) throws Exception{
        
        if (args.length < 1) {
            System.err.println("Argument(s) missing! \nType -Dexec.args=\"help\" to get full information on the possible arguments");
            return;
        }

        if(args[0].equals("help")){
            System.out.println("You must indicate a function to execute and its arguments, as described:\n");
            System.out.println("-Dexec.args=\"protect [in_file] [out_file]\"   to protect [in_file] and export it as [out_file];\n");
            System.out.println("-Dexec.args=\"unprotect [in_file] [out_file]\"   to unprotect [in_file] and export it as [out_file];\n");
            System.out.println("-Dexec.args=\"check [in_file]\"   to heck if [in_file] is protected or not.\n");
        }

        else if(args[0].equals("protect")){
            if (args.length < 3) {
                System.err.println("Argument(s) missing!");
                return;
            }

            System.out.println("Protect()");
            protect("resources/" + args[1], "resources/" + args[2]);
        }
        
        else if(args[0].equals("unprotect")){
            if (args.length < 3) {
                System.err.println("Argument(s) missing!");
                return;
            }
            System.out.println("Unprotect()");
            int flag = check("resources/" + args[1]);
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
            unprotect("resources/" + args[1], "resources/" + args[2]);
        }

        else if(args[0].equals("check")){
            if (args.length < 2) {
                System.err.println("Argument(s) missing!");
                return;
            }
            int flag = check("resources/" + args[1]);
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
