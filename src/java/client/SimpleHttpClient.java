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

    public String sendSigninRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendLoginRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
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

    public String sendAddFamRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendFamSessPOSTRequest(String ip, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendFamSessGETRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        return response.body();
    }

    public String sendAccessFamSessRequest(String ip) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + ip))
                .GET()
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
        String action = null;
        String ClientName = null;

        try{
            //Connect to server
            System.out.println("Do you have an account? (1) Yes  (2) No");
            action = scanner.nextLine();
            if(action.equals("2")){
                System.out.println("Whats your name?");
                ClientName = scanner.nextLine();
                String Singin = this.sendSigninRequest(applicationServer + "/signin?name=" + ClientName);

                if(Singin.equals("null")){
                    System.out.println("Something went wrong.");
                    System.out.println("Exiting...");
                    running = false;
                }
                else{
                    API_client.save_secretKey_toFile(Singin, "resources/" + ClientName);
                    String Login = this.sendLoginRequest(applicationServer + "/login?name=" + ClientName);
                    API_client.save_sessionKey_toFile(Login, "resources/" + ClientName, "resources/" + ClientName);
                    keyPath = "resources/" + ClientName + "_session.key";
                }
            }

            else if(action.equals("1")){
                System.out.println("Whats your name?");
                ClientName = scanner.nextLine();
                String Login = this.sendLoginRequest(applicationServer + "/login?name=" + ClientName);

                if(Login.equals("null")){
                    System.out.println("Server doesn't acknowledge you as its user.");
                    System.out.println("Exiting...");
                    running = false;
                }
                else{
                    API_client.save_sessionKey_toFile(Login, "resources/" + ClientName, "resources/" + ClientName);
                    keyPath = "resources/" + ClientName + "_session.key";
                }
            }

            else{
                System.out.println("Invalid action.");
                System.out.println("Exiting...");
                running = false;
            }


            while (running) {

                System.out.println("Choose an action: (1) Song management, (2) Family management, (3) Exit");
                String menu = scanner.nextLine();
                String menu2;
                switch (menu) {
                    case "1":
                        System.out.println("Choose an action: (1) Get song(s), (2) Post a song, (3) Delete a song");
                        menu2 = scanner.nextLine();
                        switch (menu2){
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

                            default:
                                System.out.println("Invalid action. Please try again.");
                        }

                        break;

                    case "2":
                        System.out.println("Choose an action: (1) Add member, (2) Delete member, (3) Create share session, (4) Join share session");
                        menu2 = scanner.nextLine();
                        switch (menu2){
                            case "1": // Add to family
                                System.out.println("Enter the name of the user to add to your family");
                                String FamName = scanner.nextLine();
                                String addFamResponse = this.sendAddFamRequest(applicationServer + "/invite?id=" + ClientName + "&name=" + FamName);
                                System.out.println(addFamResponse);
                                break;

                            case "2":
                                System.out.println("Enter the name of the user to remove from your family");
                                FamName = scanner.nextLine();
                                //String remFamResponse = this.sendRemFamRequest(applicationServer + "/invite?id=" + ClientName + "&name=" + FamName);
                                //System.out.println(remFamResponse);
                                break;
                            
                            case "3": // Create share session
                                String filesPath = "";
                                int cnt = 0;
                                while(true){
                                    System.out.println("Enter the path to the song details file (JSON format) or (q) to stop adding:");
                                    filesPath = scanner.nextLine();
                                    if(filesPath.equals("q")){
                                        break;
                                    }
                                    Path f = Paths.get(filesPath);
                                    if (Files.exists(f)) {
                                        String json = API_client.protect(filesPath, keyPath);
                                        String postResponse = this.sendFamSessPOSTRequest(applicationServer + "/famsess?id=" + ClientName + "&fn=" + cnt, json);
                                        System.out.println("Response: " + postResponse);
                                    } else {
                                        System.out.println("File not found: " + filesPath);
                                    }
                                    cnt ++;
                                }
                                break;

                            case "4":
                                System.out.println("What user's session you would like to join?");
                                String sess = scanner.nextLine();
                                String AccessResponse = this.sendAccessFamSessRequest(applicationServer + "/acessfamsess?id=" + ClientName + "&sess=" + sess);
                                if(AccessResponse.split(":")[0].equals("Error")){
                                    System.out.println(AccessResponse);
                                    break;
                                }
                                
                                API_client.save_sessionKey_toFile(AccessResponse, "resources/" + ClientName, "resources/" + sess);
                                int ctr = 0;
                                while(true){
                                    AccessResponse = this.sendFamSessGETRequest(applicationServer + "/famsess?id=" + sess + "&fn=" + ctr);
                                    System.out.println(AccessResponse);
                                    if(AccessResponse.equals("END")){
                                        break;
                                    }

                                    ctr ++;
                                }
                                
                                break;

                            default:
                                System.out.println("Invalid action. Please try again.");
                                break;
                        }
                        
                        break;

                    case "3": // Exit
                        running = false;
                        String exit = this.sendLogoutRequest(applicationServer + "/logout?id=" + ClientName);
                        System.out.println(exit);
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

