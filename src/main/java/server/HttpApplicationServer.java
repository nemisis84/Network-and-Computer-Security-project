package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

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

    public static void startServer(String IP, int port) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

        HttpServer server = HttpServer.create(inetSocketAddress, 0);

        // Define contexts for different HTTP methods
        server.createContext("/get", new GetHandler());
        server.createContext("/post", new PostHandler());
        server.createContext("/put", new PutHandler());
        server.createContext("/delete", new DeleteHandler());
        server.createContext("/protect", new ProtectHandler());

        server.setExecutor(null); // Creates a default executor
        server.start();
        System.out.println("Server started on " + IP + ":" + port);
    }

    static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Received GET request");
                // Get data

                String response = "GET request received";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
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
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Received POST request with data: " + requestBody);
                // Get Data

                String response = "POST request received";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class PutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("PUT".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                System.out.println("Received PUT request with data: " + requestBody);
                // Insert into database

                String response = "PUT request received";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("DELETE".equals(exchange.getRequestMethod())) {
                System.out.println("Received DELETE request");
                // Delete data
                String response = "DELETE request received";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
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
            startServer("localhost", 80); // Start the server on port 8000
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
