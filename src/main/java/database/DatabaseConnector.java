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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/songsdatabase";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    private static void startServer(String IP, int port) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        HttpServer server = HttpServer.create(inetSocketAddress, 0);
        server.createContext("/post", new PostHandler());
        server.createContext("/get", new GetHandler());
        server.createContext("/delete", new DeleteHandler());
        server.createContext("/update", new UpdateHandler());
        server.createContext("/dubs", new DuplicatesHandler());


        server.setExecutor(null);
        server.start();
        System.out.println("Database server started on " + IP + ":" + port);
    }


    static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {

                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String songName = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                String request = exchange.getRequestURI().toString();
                System.out.println("Received GET request: "+ request);

                String SQL = "SELECT * FROM media WHERE owner = '" + client + "' AND title = '" + songName + "';"; 
                String jsonResponse = null;

                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(SQL);
                    if (rs.next()) {

                        jsonResponse = rs.getString("file");

                    }

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                OutputStream os = exchange.getResponseBody();
                os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
                os.close();
            }
        }
    }

    static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                // Get the request body
                InputStream is = exchange.getRequestBody();
                String file = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String owner = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String title = exchange.getRequestURI().toString().split("&")[1].split("=")[1];

                String response = null;
                
                String SQL = "SELECT * FROM media WHERE owner = '" + owner + "' AND title = '" + title + "' AND title = '" + file + "';"; 
                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(SQL);
                    if (rs.next()) {

                        response = "Already have that song";

                    }

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                if(response == null){

                    SQL = "INSERT INTO media (owner, title, file) VALUES ('" + owner + "', '" + title + "', '" + file + "');";
                    try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                        
                        PreparedStatement stmt = connection.prepareStatement(SQL);
                        stmt.executeUpdate();
                        response = "Song added successfully";

                    } catch (SQLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                // Send a response back to the client
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            }
        }
    }

    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Received DELETE request");
            if ("DELETE".equals(exchange.getRequestMethod())) {
                
                // Extract the request URI and query string to print action
                String owner = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String title = exchange.getRequestURI().toString().split("&")[1].split("=")[1];

                String response = null;
                String SQL = "DELETE FROM media WHERE owner = '" + owner + "' AND title = '" + title + "';";
                try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                    
                    PreparedStatement stmt = connection.prepareStatement(SQL);
                    stmt.executeUpdate();
                    response = "Request executed successfully";

                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // Send a response back to the client
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }

                // Delete data from database and return a response
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }
    }

    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
            String fam = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
            String request = exchange.getRequestURI().toString();
            System.out.println("Received UPDATE request: "+ request);

            String SQL = "UPDATE media SET owner = '"+ fam + "' WHERE owner = '" + client +"';"; 
            System.out.println(SQL);

            int update_values = 0;

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                
                PreparedStatement pstmt = connection.prepareStatement(SQL);
                update_values = pstmt.executeUpdate();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String response = "Done. Values update in database: " + update_values;
            
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    static class DuplicatesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String fam = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
            String request = exchange.getRequestURI().toString();
            System.out.println("Received DUPLICATES request: "+ request);

            String SQL = "DELETE FROM media WHERE id IN (SELECT id FROM (SELECT id, ROW_NUMBER() OVER( PARTITION BY owner = '" + fam + "', title, file ORDER BY  id ) AS row_num FROM table_name ) t WHERE t.row_num > 1 );"; 
            System.out.println(SQL);

            int update_values = 0;

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                
                PreparedStatement pstmt = connection.prepareStatement(SQL);
                update_values = pstmt.executeUpdate();

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String response = "Done. Values update in database: " + update_values;
            
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    public static void main(String[] args) {
        try {
            startServer("localhost", 8001); // Start the server on port 8001 for database requests
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
