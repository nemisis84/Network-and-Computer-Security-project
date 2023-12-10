package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import client.SimpleHttpClient;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class HttpApplicationServer {

    public static void startServer(String IP, int port, String DBConnection) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

        HttpServer server = HttpServer.create(inetSocketAddress, 0);

        // Define contexts for different HTTP methods
        server.createContext("/get", new GetHandler(DBConnection));
        server.createContext("/post", new PostHandler(DBConnection));
        server.createContext("/delete", new DeleteHandler(DBConnection));
        server.createContext("/protect", new ProtectHandler());

        server.setExecutor(null); // Creates a default executor
        server.start();
        System.out.println("Server started on " + IP + ":" + port);
    }


    static class GetHandler implements HttpHandler {
        
        String DBConnection;
        
        GetHandler(String DBConnection){
            this.DBConnection = DBConnection;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                
                try {
                    // Get data from DB

                    // Extract the request URI and query string
                    String requestPath = exchange.getRequestURI().getPath();
                    String queryString = exchange.getRequestURI().getQuery();
                    String request = requestPath + (queryString != null ? "?" + queryString : "");

                    System.out.println("Received GET request: "+ request);

                    SimpleHttpClient client = new SimpleHttpClient();

                    String response = client.sendGetRequest(this.DBConnection+request);
                    
                    // Respond to client
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                    os.close();
                    System.out.println("Get response: \"" + response + "\" returned to client");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    static class ProtectHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Save the protected data to a file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("resources/protected_data.json"))) {
                writer.write(requestBody);
            }

            // Additional processing can be done here

            String response = "Protected data received";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

            }
        }
    }
    static class PostHandler implements HttpHandler {
        
        String DBConnection;
        
        PostHandler(String DBConnection){
            this.DBConnection = DBConnection;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                String requestPath = exchange.getRequestURI().getPath();
                System.out.println("Received POST request from client");
                
                SimpleHttpClient client = new SimpleHttpClient();
                String response = client.sendPostRequest(this.DBConnection + requestPath, requestBody);

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("POST response: \"" + response + "\" returned to client");
            
            } catch (Exception e) {
                e.printStackTrace();
            }

            }
        }
    }


    static class DeleteHandler implements HttpHandler {
        
        String DBConnection;
        
        DeleteHandler(String DBConnection){
            this.DBConnection = DBConnection;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("DELETE".equals(exchange.getRequestMethod())) {
                try {

                    // Extract the request URI and query string
                    String requestPath = exchange.getRequestURI().getPath();
                    String queryString = exchange.getRequestURI().getQuery();
                    String request = requestPath + (queryString != null ? "?" + queryString : "");

                    System.out.println("Received DELETE request: "+ request);

                    SimpleHttpClient client = new SimpleHttpClient();
                    

                    String response = client.sendDeleteRequest(this.DBConnection+request);
                    
                    // Delete data from database and return a response
                    exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                    System.out.println("Delete response: \"" + response + "\" returned to client");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void connectToDB(String action, Map content) throws Exception{

        if (action.equals("Get")){
            // Handle Get
        }
        else if (action.equals("Post")){
            // Handle Get
        }
        else if (action.equals("Put")){
            // Handle Post
        }
        else if (action.equals("Delete")){
            // Handle Delete
        }
    }

    public static void main(String[] args) {
        try {
            startServer("localhost", 80, "localhost:8001"); // Start the server on port 8000
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
