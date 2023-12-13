package server;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import secure_document.API_server;
import secure_document.Crypto_LIB;
import client.SimpleHttpClient;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Comparator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class HttpApplicationServer {

    public static void startServer(String IP, int port, String DBConnection) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

        HttpServer server = HttpServer.create(inetSocketAddress, 0);

        // Define contexts for different HTTP methods

        server.createContext("/signin", new GetSignin());
        server.createContext("/login", new GetLogin());
        server.createContext("/get", new SongHandler(DBConnection));
        server.createContext("/post", new SongHandler(DBConnection));
        server.createContext("/delete", new DeleteHandler(DBConnection));
        server.createContext("/invite", new AddFamHandler());
        //server.createContext("/remove", new RemFamHandler());
        server.createContext("/famsess", new FamSessHandler());
        server.createContext("/acessfamsess", new AccessFamSessHandler());
        server.createContext("/logout", new GetLogout());

        server.setExecutor(null); // Creates a default executor
        server.start();
        System.out.println("Server started on " + IP + ":" + port);
    }


    static class GetSignin implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received SignIN for client "+ clientName);

                String path = "./Clients/"+ clientName;
                String response = "null";
                Path dir_path = Paths.get(path);
                if(!Files.exists(dir_path)){
                    Files.createDirectories(dir_path);
                    try {
                        response = API_server.create_secretKey(256, path);
                        File f = new File(path + "/family.txt");
                        f.createNewFile();
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                
                

                //response = API_server.create_secretKey(256, path);
                    


                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            }
        }   
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

    static class GetLogout implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String clientName = exchange.getRequestURI().toString().split("=")[1];
            System.out.println("---------------------------------------");
            System.out.println("Received LOGOUT of "+ clientName);

            String path_key = "Clients/"+ clientName + "/session.key";
            Path dir_path = Paths.get("Clients/"+ clientName + "/FamilySession");
            File f = new File(path_key);
            f.delete();

            if(Files.exists(dir_path)){
                Files.walk(dir_path).sorted(Comparator.reverseOrder()).forEach(path -> {
                    try {
                        Files.delete(path);  //delete each file or directory
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }


            String response = "Goodbye.";
            

            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("---------------------------------------");
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

    static class AddFamHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {

                // Extract the request URI string
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String fam = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received AddFam request from "+ client);

                String response = null;
                String path = "./Clients";
                String path1 = path + "/" + client + "/family.txt";

                if(client.equals(fam)){
                    response = "Can't add yourself to your own family";
                }
                
                if(Files.exists(Paths.get(path + "/" + fam))){
                    File f = new File(path1);
                    Scanner myReader = new Scanner(f);
                    String data = "";
                    if(myReader.hasNextLine())
                        data = myReader.nextLine();
                    myReader.close();

                    
                    for(String no : data.split(",")){
                        if(no.equals(fam)){
                            response = "Already added " + fam + " to your family.";
                            break;
                        }
                    }

                    if(response == null){
                        FileWriter myWriter = new FileWriter(path1);
                        myWriter.write(data + fam + ",");
                        myWriter.close();
                        response = "Added " + fam + " to family";
                    }

                    
                }
                else{
                    response = "Client " + fam + " not register in the server.";
                }
                
                // 
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static class AccessFamSessHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException { // client asks for song

            System.out.println("---------------------------------------");
            String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
            String sess_client = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
            System.out.println("Received ACESSFAMSESS request from client " + client);

            String path = "./Clients/" + sess_client;
            String client_secret = "./Clients/" + client + "/secret.key";
            Path dir_path = Paths.get("./Clients/" + sess_client + "/FamilySession");
            String response = null;
            if(!Files.exists(dir_path)){
                response = "Error: Session offline";
            }
            else{
                File f = new File(path + "/family.txt");
                Scanner myReader = new Scanner(f);
                String data = "";
                if(myReader.hasNextLine())
                    data = myReader.nextLine();
                myReader.close();
                
                for(String no : data.split(",")){
                    if(no.equals(client)){
                        f = new File(path + "/session.key");
                        myReader = new Scanner(f);
                        try {
                            response = Crypto_LIB.AES_encrypt(myReader.nextLine(), client_secret);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        myReader.close();
                        break;
                    }
                }
            }

            if(response == null){
                response = "Error: You don't have access to this session.";
            }
            
        
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("---------------------------------------");

        }
    }

    static class FamSessHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException { // client asks for song
            if("POST".equals(exchange.getRequestMethod())){
                System.out.println("---------------------------------------");
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String file_number = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("Received POST FAMSESS request from client " + client);

                String filepath = "./Clients/" + client + "/FamilySession/file_" + file_number;
                Path dir_path = Paths.get("./Clients/" + client + "/FamilySession");
                if(!Files.exists(dir_path)){
                    Files.createDirectories(dir_path);
                }
                File f = new File(filepath);
                f.createNewFile();
                FileWriter myWriter = new FileWriter(filepath);
                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                myWriter.write(requestBody);
                myWriter.close();

                String response = "Added file number " + file_number;
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            }

            else if("GET".equals(exchange.getRequestMethod())){
                System.out.println("---------------------------------------");
                String sess_client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String file_number = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("Received GET FAMSESS request from session " + sess_client);

                String filepath = "./Clients/" + sess_client + "/FamilySession/file_" + file_number;
                String response = null; 
                File f = new File(filepath);
                if(f.exists()){
                    Scanner myReader = new Scanner(f);
                    response = myReader.nextLine();
                    myReader.close();
                }
                else{
                    response = "END";
                }
            
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                System.out.println("---------------------------------------");
            }

        }
            
    }

    static class GetFamSessHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException { // client asks for song

            System.out.println("---------------------------------------");
            String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
            String sess_client = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
            String file_number = exchange.getRequestURI().toString().split("&")[2].split("=")[1];
            System.out.println("Received ACESSFAMSESS request from client " + client);

            String filepath = "./Clients/" + sess_client + "/FamilySession/file_" + file_number;
            String response = null;
            

            File f = new File(filepath);
            if(f.exists()){
                Scanner myReader = new Scanner(f);
                response = myReader.nextLine();
                myReader.close();
            }
            else{
                response = "No more files";
            }
            
        
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("---------------------------------------");

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
