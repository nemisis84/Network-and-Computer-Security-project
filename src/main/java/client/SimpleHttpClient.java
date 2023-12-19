package client;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import secure_document.API_client;

public class SimpleHttpClient {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(1)).executor(executor).build();

    public String sendSigninRequest(String ip, String pass) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .POST(BodyPublishers.ofString(pass))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendLoginRequest(String ip, String pass) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .POST(BodyPublishers.ofString(pass))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendLogoutRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendTypeRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendGetRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendPostRequest(String ip, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendDeleteRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendUpgradeRequest(String ip, String pass) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .POST(BodyPublishers.ofString(pass))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendInviteRequest(String ip, String pass) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .POST(BodyPublishers.ofString(pass))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendCheckInviteRequest(String ip, String pass) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .POST(BodyPublishers.ofString(pass))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendCheckFamRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }


    public void CLI(String applicationServer) throws Exception{
        
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        String ClientName = null;
        String response = null;
        String Login = null;
        String songName = null;
        String action = null;
        String FamName = null;
        String filePath = null;
        String type = null;
        String Singin = null;
        String secretkey_path = null;
        String sessionkey_path = null;
        String familykey_path = null;
        String pass = null;

        try{
            
            //Connect to server
            System.out.println("Do you have an account? (1) Yes  (2) No");
            action = scanner.nextLine();
            if(action.equals("2")){
                System.out.println("Choose your user name?");
                ClientName = scanner.nextLine();
                System.out.println("Choose your password?");
                pass = scanner.nextLine();
                Singin = this.sendSigninRequest(applicationServer + "/signin?name=" + ClientName, pass);

                if(Singin.equals("null")){
                    System.out.println("Something went wrong.");
                    System.out.println("Exiting...");
                    running = false;
                }
                else{
                    secretkey_path = "resources/" + ClientName + "_secret.key";
                    sessionkey_path = "resources/" + ClientName + "_session.key";
                    API_client.saveKey_toFile(Singin, null, secretkey_path);
                    Login = this.sendLoginRequest(applicationServer + "/login?name=" + ClientName, pass);
                    API_client.saveKey_toFile(Login, secretkey_path, sessionkey_path);
                    type = "NORMAL";
                }
            }

            else if(action.equals("1")){
                System.out.println("Whats your user name?");
                ClientName = scanner.nextLine();
                System.out.println("Whats your password?");
                pass = scanner.nextLine();
                Login = this.sendLoginRequest(applicationServer + "/login?name=" + ClientName, pass);

                if(Login.equals("null")){
                    System.out.println("Server doesn't acknowledge you as its user.");
                    System.out.println("Exiting...");
                    running = false;
                }
                else if(Login.equals("incorrect")){
                    System.out.println("User name and password do not match.");
                    System.out.println("Exiting...");
                    running = false;
                }
                else{
                    secretkey_path = "resources/" + ClientName + "_secret.key";
                    sessionkey_path = "resources/" + ClientName + "_session.key";

                    type = this.sendTypeRequest(applicationServer + "/type?name=" + ClientName);
                    if(!type.equals("NORMAL")){
                        familykey_path = "resources/" + ClientName + "_family.key";
                    }
                    
                    API_client.saveKey_toFile(Login, secretkey_path, sessionkey_path);
                }
            }

            else{
                System.out.println("Invalid action.");
                System.out.println("Exiting...");
                running = false;
            }
            

            while (running) {

                if(type.equals("FAM LEADER")){
                    System.out.println("Choose an action: (1) Get song, (2) Post a song, (3) Delete a song, (4) Add family member, (5) Check family tree, (6) Exit");
                }
                else if(type.equals("FAM")){
                    System.out.println("Choose an action: (1) Get song, (2) Post a song, (3) Delete a song, (4) Check family tree, (5) Exit");
                }
                else{
                    System.out.println("Choose an action: (1) Get song, (2) Post a song, (3) Delete a song, (4) Create family, (5) Join family, (6) Exit");
                }
                action = scanner.nextLine();
                response = null;
                switch (action) {
                    case "1": // Get song(s)
                        System.out.println("Enter the name of the song:");
                        songName = scanner.nextLine();
                        response = this.sendGetRequest(applicationServer + "/get?id=" + ClientName + "&" + "song=" + songName);

                        if(response.equals("No")){ 
                            System.out.println("No music in the database with name: " + songName);
                            break;
                        }

                        
                        if(type.equals("NORMAL")){
                             System.err.println("\nUnprotects using shared secret and session key:\n");
                            API_client.unprotect(response, secretkey_path, sessionkey_path);
                        }
                        else {
                            System.err.println("\nUnprotects using family key and session key:\n");
                            API_client.unprotect(response, familykey_path, sessionkey_path);
                        }
                        System.out.println("Received: " + songName);
                        System.out.println("Stored in Musics directory");
                        break;

                    case "2": // Post a song

                        System.out.println("Enter the path to the song details file (JSON format):");
                        filePath = scanner.nextLine();
                        System.out.println("Enter the name of the song:");
                        songName = scanner.nextLine();
                        Path file = Paths.get(filePath);
                        if (Files.exists(file)) {
                            String json;
                            if(type.equals("NORMAL")){
                                System.out.println("Protecting using secret key and session key:\n");                                
                                json = API_client.protect(filePath, secretkey_path, sessionkey_path);
                            }
                            else{
                                System.out.println("Protecting using family key and session key:\n");
                                json = API_client.protect(filePath, familykey_path, sessionkey_path);
                            }

                            response = this.sendPostRequest(applicationServer + "/post?id=" + ClientName + "&song=" + songName, json);
                            System.out.println("POST Response: " + response);
                        } else {
                            System.out.println("File not found: " + filePath);
                        }
                        break;

                    case "3": // Delete a song
                        System.out.println("Enter the name of the song to delete:");
                        songName = scanner.nextLine();
                        response = this.sendDeleteRequest(applicationServer + "/delete?id=" + ClientName + "&song=" + songName);
                        System.out.println("DELETE Response: " + response);
                        break;

                    case "4": 
                        if(type.equals("FAM LEADER")){ // Add to family
                            System.out.println("Whats the name of your family member?");
                            FamName = scanner.nextLine();
                            response = this.sendInviteRequest(applicationServer + "/invite?id=" + ClientName + "&name=" + FamName, pass);
                            System.out.println(response);
                            break;
                        }
                        else if(type.equals("FAM")){ // Check family tree
                            response = this.sendCheckFamRequest(applicationServer + "/check?name=" + ClientName);
                            System.out.println(response);
                            break;
                        }
                        else if(type.equals("NORMAL")){ // Create family
                            response = sendUpgradeRequest(applicationServer + "/up?id=" + ClientName, pass);
                            if(response != null){
                                familykey_path = "resources/" + ClientName + "_family.key";
                                API_client.saveKey_toFile(response, sessionkey_path, familykey_path);
                                type = "FAM LEADER";
                                System.out.println("Family created successfully.");
                            }
                            else{
                                System.out.println("Error creating family.");
                            }
                            break;
                        }

                    case "5": 
                        if(type.equals("FAM LEADER")){ // Check family tree
                            response = this.sendCheckFamRequest(applicationServer + "/check?name=" + ClientName);
                            System.out.println(response);
                            break;
                        }
                        else if(type.equals("FAM")); // Exit
                        else if(type.equals("NORMAL")){ // Join family
                            System.out.println("Whats the name of the family you want to join?");
                            FamName = scanner.nextLine();
                            response = sendCheckInviteRequest(applicationServer + "/checkinv?id=" + ClientName + "&name=" + FamName, pass);
                            if(response.equals("noinvite")){
                                System.out.println("You don't have an invite to " + FamName + "'s' family.");
                            }
                            else if(response.equals("nofam")){
                                System.out.println("Family " + FamName + "doesn't exist.");
                            }
                            else if(response.equals("null")){
                                System.out.println("Something went wrong.");
                            }
                            else{
                                familykey_path = "resources/" + ClientName + "_family.key";
                                API_client.saveKey_toFile(response, sessionkey_path, familykey_path);
                                type = "FAM";
                                System.out.println("Family joined successfully.");
                            }
                            break;
                        }
                    
                    case "6":
                        running = false;
                        String exit = this.sendLogoutRequest(applicationServer + "/logout?id=" + ClientName);
                        System.out.println(exit);
                        System.out.println("Exiting...");
                        File f = new File(sessionkey_path);
                        f.delete();
                        break;

                    default:
                        System.out.println("Invalid action. Please try again.");
                }
            }

        }
        catch(IOException e){
            
        }
        finally{
            if(running){
                System.out.println("The server is offline.\nExiting...");
            }
            scanner.close();
            executor.shutdownNow();
            System.gc();
        }
        

    }

    public static void main(String[] args) throws Exception {
        SimpleHttpClient client = new SimpleHttpClient();
        String applicationServer = "192.168.2.4:80";
        client.CLI(applicationServer);
    }
}

