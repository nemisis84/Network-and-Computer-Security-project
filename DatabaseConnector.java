import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;


public class DatabaseConnector {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/SongsDatabase";
    // admin for now
    private static final String USER = "admin";
    private static final String PASSWORD = "admin123";

    public static void main(String[] args) {
        try {
            // loads the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // establishes the database connection
            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
                // uses the protect function from the API class
                API.protect("resources/secure_doc.json", "resources/encrypted_file");

                // put the data into the database
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO media (owner, format, artist, title, genre1) VALUES (?, ?, ?, ?, ?)")) {
                    preparedStatement.setString(1, "eduardo_albino");
                    preparedStatement.setString(2, "WAV");
                    preparedStatement.setString(3, "Ed_Sheeran");
                    preparedStatement.setString(4, "Shape_of_you");
                    preparedStatement.setString(5, "pop");

                    // will have to add more rows once we use everything (lyrics, genre2 etc)

                    preparedStatement.executeUpdate();
                }

                // read the protected data from the file
                StringBuilder protectedData = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader("resources/encrypted_file"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        protectedData.append(line);
                    }
                }

                // sends the protected data to the application server
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