package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import secure_document.API_server;
import client.SimpleHttpClient;

import java.io.BufferedWriter;
import java.io.File;
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

        server.createContext("/login", new GetLogin());
        server.createContext("/get", new SongHandler(DBConnection));
        server.createContext("/post", new SongHandler(DBConnection));
        server.createContext("/delete", new DeleteHandler(DBConnection));

        server.setExecutor(null); // Creates a default executor
        server.start();
        System.out.println("Server started on " + IP + ":" + port);
    }

    static class GetLogin implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received LOGIN of "+ clientName);

                String path = "Clients/"+ clientName;
                String response = "null";
                File f = new File(path +"/secret.key");
                if(f.exists() && !f.isDirectory()) { 
                    try {
                        response = API_server.create_sessionKey(256, path);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }


                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            }
        }   
    }


    static class SongHandler implements HttpHandler {
        String DBConnection;
        
        SongHandler(String DBConnection){
            this.DBConnection = DBConnection;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException { // client asks for song
            if ("GET".equals(exchange.getRequestMethod())) {
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String song = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received GET request from client " + client + " for the song named " + song);
    
                String response = null;
                try {
                    // Extract the request URI string
                    String request = exchange.getRequestURI().toString();

                    System.out.println("Received GET request: "+ request);

                    SimpleHttpClient serverClient = new SimpleHttpClient();

                    String DBResponse = serverClient.sendGetRequest(this.DBConnection+request);

                    response = API_server.protect(DBResponse, "Clients/" + client + "/session.key");
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            }

            else if("POST".equals(exchange.getRequestMethod())){ // artist post song
                System.out.println("---------------------------------------");
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String songName = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("Received POST request from client " + client);

                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                try {
                    requestBody = API_server.unprotect(requestBody, "Clients/" + client + "/session.key");
                    String requestPath = "/post?id=" + client + "&song=" + songName;
                    SimpleHttpClient serverClient = new SimpleHttpClient();
                    String response = serverClient.sendPostRequest(this.DBConnection + requestPath, requestBody);
                     exchange.sendResponseHeaders(200, response.length());
                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

               
                System.out.println("---------------------------------------");
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

                    // Extract the request URI string
                    String request = exchange.getRequestURI().toString();

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
