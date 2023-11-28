import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

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

    public static void main(String[] args) {
        SimpleHttpClient client = new SimpleHttpClient();
        
        try {
            String applicationServer = "localhost:8000";
            // GET request
            String getResponse = client.sendGetRequest(applicationServer+"/get");
            System.out.println("GET Response: " + getResponse);

            // POST request
            String postResponse = client.sendPostRequest(applicationServer+"/post", "{\"name\":\"value\"}");
            System.out.println("POST Response: " + postResponse);

            // PUT request
            String putResponse = client.sendPutRequest(applicationServer+"/put", "{\"name\":\"new value\"}");
            System.out.println("PUT Response: " + putResponse);

            // DELETE request
            String deleteResponse = client.sendDeleteRequest(applicationServer+"/delete");
            System.out.println("DELETE Response: " + deleteResponse);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

