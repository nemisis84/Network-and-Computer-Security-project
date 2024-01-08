package server;
import java.io.File;
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
import java.util.Map;
import java.util.Scanner;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import client.SimpleHttpClient;
import secure_document.API_server;


public class HttpApplicationServer {

    public static void startServer(String IP, int port, String DBConnection) throws IOException {
        InetAddress address = InetAddress.getByName(IP);
        InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);

        HttpServer server = HttpServer.create(inetSocketAddress, 0);

        // Define contexts for different HTTP methods

        server.createContext("/signin", new GetSignin());
        server.createContext("/login", new GetLogin());
        server.createContext("/logout", new GetLogout());
        server.createContext("/type", new GetType());
        server.createContext("/get", new SongHandler(DBConnection));
        server.createContext("/post", new SongHandler(DBConnection));
        server.createContext("/delete", new SongHandler(DBConnection));
        server.createContext("/invite", new InviteHandler());
        server.createContext("/up", new UpgradeHandler());
        server.createContext("/checkinv", new CheckInviteHandler(DBConnection));
        server.createContext("/check", new CheckFamHandler());

        server.setExecutor(null); // Creates a default executor
        server.start();
        System.out.println("Server started on " + IP + ":" + port);
    }

    static class GetSignin implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {

                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received SignIN for client "+ clientName);

                InputStream is = exchange.getRequestBody();
                String password = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String path = "./Clients/"+ clientName;
                String response = "null";
                Path dir_path = Paths.get(path);
                if(!Files.exists(dir_path)){
                    Files.createDirectories(dir_path);
                    try {
                        API_server.createKey(256, path + "/secret.key");
                        response = API_server.readKey(path + "/secret.key", null);
                        File f = new File(path + "/family.txt");
                        f.createNewFile();

                        f = new File(path + "/type.txt");
                        f.createNewFile();
                        FileWriter myWriter = new FileWriter(f);
                        myWriter.write("NORMAL");
                        myWriter.close();

                        f = new File(path + "/password");
                        f.createNewFile();
                        myWriter = new FileWriter(f);
                        myWriter.write(password);
                        myWriter.close();

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

    static class GetLogin implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received LOGIN of "+ clientName);

                InputStream is = exchange.getRequestBody();
                String password = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                String path = "Clients/"+ clientName;
                String response = "null";
                File f = new File(path +"/password");
                if(f.exists() && !f.isDirectory()) { 
                    try {
                        Scanner myReader = new Scanner(f);
                        if(myReader.nextLine().equals(password)){
                            API_server.createKey(256, path + "/session.key");
                            response = API_server.readKey(path + "/session.key", path + "/secret.key");
                        }
                        else{
                            response = "incorrect";
                        }
                        myReader.close();
                        
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
            File f = new File(path_key);
            f.delete();

            String response = "Goodbye.";
            
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            System.out.println("---------------------------------------");
        }   
    }

    static class GetType implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received TYPE of "+ clientName);

                String path = "Clients/"+ clientName + "/type.txt";
                File f = new File(path);
                Scanner myReader = new Scanner(f);
                String response = myReader.nextLine();
                myReader.close();

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
            String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
            String songName = exchange.getRequestURI().toString().split("&")[1].split("=")[1];

            File f = new File("Clients/" + client + "/type.txt");
            Scanner myReader = new Scanner(f);
            String type = myReader.nextLine();
            myReader.close();
            String enckeypath;
            String sessionkeypath = "Clients/" + client + "/session.key";
            if(type.equals("NORMAL")){
                enckeypath = "Clients/" + client + "/secret.key";
            }
            else{
                f = new File("Clients/" + client + "/family.txt");
                myReader = new Scanner(f);
                String fam = myReader.nextLine();
                myReader.close();

                client = fam;
                enckeypath = "Family/" + fam + "/family.key";
            }

            if ("GET".equals(exchange.getRequestMethod())) {
                
                System.out.println("---------------------------------------");
                System.out.println("Received GET request from client/family " + client + " for the song named " + songName);
    
                String response = null;
                try {
                    // Extract the request URI string
                    String request = "/get?id=" + client + "&song=" + songName;

                    System.out.println("Received GET request: "+ request);

                    SimpleHttpClient serverClient = new SimpleHttpClient();

                    String DBResponse = serverClient.sendGetRequest(this.DBConnection+request);

                    if (DBResponse.equals("Music not found")) {
                        response = "No";
                    }
                    else {
                        response = API_server.protect(DBResponse, enckeypath, sessionkeypath);
                    }
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
                System.out.println("Received POST request from client/family " + client);

                InputStream is = exchange.getRequestBody();
                String requestBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                
                try {
                    requestBody = API_server.unprotect(requestBody, enckeypath, sessionkeypath);
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

            if ("DELETE".equals(exchange.getRequestMethod())) {
                try {

                    // Extract the request URI string

                    System.out.println("Received DELETE request from client/family: "+ client);
                    String request = "/delete?id=" + client + "&song=" + songName;

                    SimpleHttpClient serverclient = new SimpleHttpClient();

                    String response = serverclient.sendDeleteRequest(this.DBConnection+request);
                    
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

    static class UpgradeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String clientName = exchange.getRequestURI().toString().split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received UPGRADE of "+ clientName);

                InputStream is = exchange.getRequestBody();
                String password = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                
                boolean flag = false;
                String path = "Clients/" + clientName;
                String response = null;
                File f = new File(path +"/password");
                if(f.exists() && !f.isDirectory()) { 
                    try {
                        Scanner myReader = new Scanner(f);
                        if(myReader.nextLine().equals(password)){
                            flag = true;
                        }
                        myReader.close();
                        
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

                if(flag){
                    Path dir_path = Paths.get("Family/"+ clientName);
                    Files.createDirectories(dir_path);

                    f = new File("Family/"+ clientName + "/members.txt");
                    f.createNewFile();
                    FileWriter myWriter = new FileWriter(f);
                    myWriter.write(clientName + ",");
                    myWriter.close();

                    f = new File(path + "/family.txt");
                    myWriter = new FileWriter(f);
                    myWriter.write(clientName);
                    myWriter.close();

                    f = new File(path + "/type.txt");
                    myWriter = new FileWriter(f);
                    myWriter.write("FAM LEADER");
                    myWriter.close();
                    


                    try {
                        API_server.createKey(256, "Family/"+ clientName + "/family.key");
                        response = API_server.readKey("Family/"+ clientName + "/family.key", path + "/session.key");
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

    static class InviteHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {

                // Extract the request URI string
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String fam = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received AddFam request from "+ client);

                String response = null;
                String path = "./Clients/" + fam;
                InputStream is = exchange.getRequestBody();
                String password = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                File f = new File("Clients/" + client + "/password");
                Scanner myReader = new Scanner(f);
                if(myReader.nextLine().equals(password)){
                    myReader.close();
                    if(client.equals(fam)){
                        response = "Can't add yourself to your own family";
                    }
                    
                    if(Files.exists(Paths.get(path))){                  
                        
                        f = new File(path + "/family.txt");
                        myReader = new Scanner(f);
                        if(!myReader.hasNextLine()){

                            response = "Invite sent to " + fam;
                            myReader.close();

                            f = new File(path + "/invite_" + client);
                            f.createNewFile();
                        }
                        else{
                            response = fam + "is already in a family";
                        }

                    }
                    else{
                        response = "Client " + fam + " not register in the server.";
                    }
                    
                }
                else{
                    myReader.close();
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

    static class CheckInviteHandler implements HttpHandler {

        String DBConnection;
        
        CheckInviteHandler(String DBConnection){
            this.DBConnection = DBConnection;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {

                // Extract the request URI string
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                String fam = exchange.getRequestURI().toString().split("&")[1].split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received JoinFam request from "+ client);

                String response = "null";
                InputStream is = exchange.getRequestBody();
                String password = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                File f = new File("Clients/" + client + "/password");
                Scanner myReader = new Scanner(f);
                String pass = myReader.nextLine();
                myReader.close();

                if(pass.equals(password) && Files.exists(Paths.get("./Clients/" + fam))){
                    
                    
                    f = new File("Clients/" + client + "/invite_" + fam);
                    if(f.exists()){                  
                        f.delete();

                        f = new File("Clients/" + client + "/family.txt");
                        FileWriter myWriter = new FileWriter(f);
                        myWriter.write(fam);
                        myWriter.close();
                        
                        f = new File("Clients/" + client + "/type.txt");
                        myWriter = new FileWriter(f);
                        myWriter.write("FAM");
                        myWriter.close();

                        f = new File("Family/" + fam + "/members.txt");
                        myReader = new Scanner(f);
                        String data = myReader.nextLine();
                        myWriter = new FileWriter(f);
                        myWriter.write(data + client + ",");
                        myWriter.close();

                        //read key
                        response = API_server.readKey("Family/" + fam + "/family.key", "Clients/" + client + "/session.key");

                        SimpleHttpClient serverClient = new SimpleHttpClient();

                        String DBrequest = "/update?id=" + client + "&fam=" + fam;
                        String DBResponse = serverClient.sendGetRequest(this.DBConnection+DBrequest);
                        System.out.println(DBResponse);

                        DBrequest = "/dubs?id=" + fam;
                        DBResponse = serverClient.sendGetRequest(this.DBConnection+DBrequest);
                        System.out.println(DBResponse);
                        



                    }
                    else{
                        response = "noinvite";
                    }
                    
                }
                else{
                    if(Files.exists(Paths.get("./Clients/" + fam)))
                        response = "nofam";
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

    static class CheckFamHandler implements HttpHandler {
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {

                // Extract the request URI string
                String client = exchange.getRequestURI().toString().split("&")[0].split("=")[1];
                System.out.println("---------------------------------------");
                System.out.println("Received CheckFam request from "+ client);


                File f = new File("Clients/" + client + "/family.txt");
                Scanner myReader = new Scanner(f);
                String fam = myReader.nextLine();
                myReader.close();

                f = new File("Family/" + fam + "/members.txt");
                myReader = new Scanner(f);
                String members = myReader.nextLine();
                myReader.close();


                int cnt = 0;
                String response = "";
                for(String member : members.split(",")){
                    
                    if(cnt == 0)
                        response += "Leader: " + member;
                    
                    else
                        response += "\nMember n" + cnt + ": " + member;
                    

                    cnt ++;
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
            startServer("192.168.1.1", 80, "192.168.0.100:80"); // Start the server on port 8000
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String musicFILE = "{\"media\": {\"mediaInfo\": {\"owner\": \"Bob\",\"format\": \"mp3\",\"artist\": \"Alison Chains\",\"title\": \"Man in the Bin\",\"genre\": [\"Grunge\",\"Alternative Metal\"]},\"mediaContent\": {\"lyrics\": [\"Trapped in a world, a box of my own\",\"Container whispers, in this space alone\",\"Echoes of silence, in the walls I confide\",\"A man in the box, with nowhere to hide\",\"Chained by thoughts, in a silent uproar\",\"Searching for keys, to unlock the door\"],\"audioBase64\": \"YWJkYw==\"}}}";
}
