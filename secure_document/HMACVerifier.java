package secure_document;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class HMACVerifier {

    public static boolean check(String message, String key, byte[] receivedTag) {
        // Calculate the new tag using the calculateTag function
        byte[] newTag = calculateTag(message, key);

        // Compare the received tag with the newly computed tag using a constant-time comparison
        return isEqualConstantTime(receivedTag, newTag);
    }

    private static byte[] calculateTag(String message, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            return mac.doFinal(message.getBytes());
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean isEqualConstantTime(byte[] a, byte[] b) {
        // Compare two byte arrays using a constant-time algorithm
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    public static void main(String[] args) {
        String message = "Hello, world!";
        String key = "your_secret_key";

        // Calculate the tag for the original message
        byte[] originalTag = calculateTag(message, key);
        System.out.println(originalTag.toString());
        // Simulate sending the message and received tag over the network
        // In a real scenario, you would get the receivedTag from some external source
        byte[] receivedTag = originalTag;
        System.out.println(receivedTag.toString());
        // Check if the received tag matches the newly computed tag
        boolean isTagValid = check(message, key, receivedTag);

        if (isTagValid) {
            System.out.println("Tag verification successful. The message is authentic.");
        } else {
            System.out.println("Tag verification failed. The message may have been tampered with.");
        }
    }
}