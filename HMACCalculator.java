import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HMACCalculator {

    public static byte[] calculateTag(String message, String key) {
        try {
            // Create a Mac object
            Mac mac = Mac.getInstance("HmacSHA256");

            // Create a SecretKeySpec object for the given key
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");

            // Initialize the Mac object with the key
            mac.init(secretKey);

            // Compute the HMAC tag for the message
            byte[] tag = mac.doFinal(message.getBytes());

            return tag;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Handle exceptions
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {
        String message = "Hello, world!";
        String key = "your_secret_key";

        byte[] tag = calculateTag(message, key);

        if (tag != null) {
            System.out.println("HMAC Tag: " + byteArrayToHexString(tag));
        }
    }

    // Helper method to convert a byte array to a hexadecimal string
    private static String byteArrayToHexString(byte[] array) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : array) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}