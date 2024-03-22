package crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5 {
    public static String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            byte[] hashBytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString(); // return hash string
        } catch (NoSuchAlgorithmException error) {
            return error.toString();
        }
    }
}
