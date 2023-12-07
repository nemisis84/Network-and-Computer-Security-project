package database;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;

import secure_document.API_server;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/songsdatabase";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static void main(String[] args) {
        if (args.length < 5) {
            System.err.println("Usage: DatabaseConnector <owner> <format> <artist> <title> <genre1>");
            System.exit(1);
        }

        String owner = args[0];
        String format = args[1];
        String artist = args[2];
        String title = args[3];
        String genre1 = args[4];

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

    private static void sendToApplicationServer(String data) {
        try {
            URL url = new URL("http://localhost:8000/protect");
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
}
