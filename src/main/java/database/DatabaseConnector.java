package database;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import secure_document.API_server;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/songsdatabase";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static void serverRequest(String owner, String format, String artist, String title, String genre1) {
        try {
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Establish the database connection
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                // Use the protect function from the API class
                API_server.protect("resources/secure_doc.json", "resources/encrypted_file");

                // Insert the data into the database
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO media (owner, format, artist, title, genre1) VALUES (?, ?, ?, ?, ?)")) {
                    preparedStatement.setString(1, owner);
                    preparedStatement.setString(2, format);
                    preparedStatement.setString(3, artist);
                    preparedStatement.setString(4, title);
                    preparedStatement.setString(5, genre1);

                    // You can add more parameters and set them accordingly

                    preparedStatement.executeUpdate();
                }

                // Read the protected data from the file
                StringBuilder protectedData = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader("resources/encrypted_file"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        protectedData.append(line);
                    }
                }

                // Send the protected data to the application server
                sendToApplicationServer(protectedData.toString());

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            startServer("localhost", 8001); // Start the server on port 8001 for database requests
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startServer(String IP, int port) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        HttpServer server = HttpServer.create(inetSocketAddress, 0);
        server.createContext("/execute", new PostHandler());
        server.createContext("/get", new GetHandler());
        server.createContext("/delete", new DeleteHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Database server started on " + IP + ":" + port);
    }

    private static void sendToApplicationServer(String data) {
        try {
            URL url = new URL("http://localhost:8001/protect");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String response = br.readLine();
                System.out.println("Server response: " + response);
            }

            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Get the request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());

                // Split the request body into parameters
                String[] params = requestBody.split("&");

                String owner = getValue(params, "owner");
                String format = getValue(params, "format");
                String artist = getValue(params, "artist");
                String title = getValue(params, "title");
                String genre1 = getValue(params, "genre1");

                // Execute the serverRequest function
                serverRequest(owner, format, artist, title, genre1);

                // Send a response back to the client
                String response = "Request executed successfully";
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }

        private String getValue(String[] params, String key) {
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals(key)) {
                    return keyValue[1];
                }
            }
            return null;
        }
    }

    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Received DELETE request");
            if ("DELETE".equals(exchange.getRequestMethod())) {
                
                // Extract the request URI and query string to print action
                String requestPath = exchange.getRequestURI().getPath();
                String queryString = exchange.getRequestURI().getQuery();
                String request = requestPath + (queryString != null ? "?" + queryString : "");
                System.out.println("Received DELETE request: "+ request);

                // Delete data from database and return a response
                String response = "DELETE request Executed";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Extract the request URI and query string to print action
                String requestPath = exchange.getRequestURI().getPath();
                String queryString = exchange.getRequestURI().getQuery();
                String request = requestPath + (queryString != null ? "?" + queryString : "");
                System.out.println("Received GET request: "+ request);

                String jsonResponse = "{\"json\": \"Response\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                os.close();
            }
        }
    }

}
