package client;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import secure_document.API;
public class SimpleHttpClient {

    private final HttpClient httpClient = HttpClient.newBuilder().build();

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

    public void CLI(String applicationServer){
                SimpleHttpClient client = new SimpleHttpClient();
        Scanner scanner = new Scanner(System.in);

        try {
            boolean running = true;

            while (running) {
                System.out.println("Choose an action: (1) Get song(s), (2) Post a song, (3) Delete a song, (4) Exit");
                String action = scanner.nextLine();

                switch (action) {
                    case "1": // Get song(s)
                        System.out.println("Enter the name of the song:");
                        String songName = scanner.nextLine();
                        String getResponse = client.sendGetRequest(applicationServer + "/get?name=" + songName);
                        // if (API.check(getResponse)==1){
                        //     System.out.println("GET Response: " + getResponse);
                        // }
                        // else{
                        //     System.out.println("Song not authentic");
                        // }
                        System.out.println("GET Response: " + getResponse);
                        break;

                    case "2": // Post a song
                        System.out.println("Enter the path to the song details file (JSON format):");
                        String filePath = scanner.nextLine();
                        Path file = Paths.get(filePath);
                        if (Files.exists(file)) {
                            String json = Files.readString(file);
                            String postResponse = client.sendPostRequest(applicationServer + "/post", json);
                            System.out.println("POST Response: " + postResponse);
                        } else {
                            System.out.println("File not found: " + filePath);
                        }
                        break;

                    case "3": // Delete a song
                        System.out.println("Enter the ID of the song to delete:");
                        String songId = scanner.nextLine();
                        String deleteResponse = client.sendDeleteRequest(applicationServer + "/delete?id=" + songId);
                        System.out.println("DELETE Response: " + deleteResponse);
                        break;

                    case "4": // Exit
                        running = false;
                        System.out.println("Exiting...");
                        break;

                    default:
                        System.out.println("Invalid action. Please try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }

    public static void main(String[] args) throws Exception {
        SimpleHttpClient client = new SimpleHttpClient();
        String applicationServer = "localhost:8000";
        client.CLI(applicationServer);
    }
}

