package client;
import java.io.IOException;
import java.lang.reflect.Method;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import secure_document.API_client;

public class SimpleHttpClient {

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final HttpClient httpClient = HttpClient.newBuilder().followRedirects(Redirect.ALWAYS).connectTimeout(Duration.ofSeconds(1)).executor(executor).build();

    public String sendLoginRequest(String ip) throws Exception {
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

    public String sendPutRequest(String ip, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(json))
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

    public void CLI(String applicationServer) throws Exception{
        
        Scanner scanner = new Scanner(System.in);
      
        boolean running = true;
        String keyPath = null;

        try{
            //Connect to server
            System.out.println("Whats your name?");
            String ClientName = scanner.nextLine();
            String Login = this.sendLoginRequest(applicationServer + "/login?name=" + ClientName);

            if(Login.equals("null")){
                System.out.println("Server doesn't acknowledge you as its user.");
                System.out.println("Exiting...");
                running = false;
            }
            else{
                API_client.save_sessionKey_toFile(Login, "resources");
                keyPath = "resources/session.key";
            }

            while (running) {

                System.out.println("Choose an action: (1) Get song(s), (2) Post a song, (3) Delete a song, (4) Add family member, (5) Exit");
                String action = scanner.nextLine();
                switch (action) {
                    case "1": // Get song(s)
                        System.out.println("Enter the name of the song:");
                        String songName = scanner.nextLine();
                        String getResponse = this.sendGetRequest(applicationServer + "/get?id=" + ClientName + "&" + "song=" + songName);

                        if(getResponse == null){ 
                            System.out.println("No music in the database with name: " + songName);
                            break;
                        }
                        
                        API_client.unprotect(getResponse, keyPath);

                        System.out.println("Received: " + songName);
                        System.out.println("Stored in Musics directory");
                        break;

                    case "2": // Post a song
                        System.out.println("Enter the path to the song details file (JSON format):");
                        String filePath = scanner.nextLine();
                        System.out.println("Enter the name of the song:");
                        String songToPost = scanner.nextLine();
                        Path file = Paths.get(filePath);
                        if (Files.exists(file)) {
                            String json = API_client.protect(filePath, keyPath);
                            String postResponse = this.sendPostRequest(applicationServer + "/post?id=" + ClientName + "&song=" + songToPost, json);
                            System.out.println("POST Response: " + postResponse);
                        } else {
                            System.out.println("File not found: " + filePath);
                        }
                        break;

                    case "3": // Delete a song
                        System.out.println("Enter the name of the song to delete:");
                        String songId = scanner.nextLine();
                        String deleteResponse = this.sendDeleteRequest(applicationServer + "/delete?id=" + ClientName + "&song=" + songId);
                        System.out.println("DELETE Response: " + deleteResponse);
                        break;

                    case "4": // Add to family
                        System.out.println("Whats the name of your family member?");
                        String FamName = scanner.nextLine();
                        //String addFamResponse = this.sendAddFamRequest(applicationServer + "/invite?name=" + FamName);
                        break;

                    case "5": // Exit
                        running = false;
                        System.out.println("Exiting...");
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
        String applicationServer = "localhost:80";
        client.CLI(applicationServer);
    }
}

