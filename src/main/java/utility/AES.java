package main.java.utility;

import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class AES {
    private static final String SALTED_STR = "Salted__";
    private static final byte[] SALTED_MAGIC = SALTED_STR.getBytes(StandardCharsets.US_ASCII);

    private static byte[] arrayConcat(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static String encrypt(@NotNull String input, @NotNull String key) throws Exception {
        byte[] secret = key.getBytes();
        SecureRandom random = new SecureRandom();
        byte[] salt = random.generateSeed(8);
        byte[] inputByte = input.getBytes();
        byte[] passAndSalt = arrayConcat(secret, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        int i = 0;
        while (i < 3 && keyAndIv.length < 48) {
            byte[] hashData = arrayConcat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = arrayConcat(keyAndIv, hash);
            i++;
        }
        byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyValue, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] data = cipher.doFinal(inputByte);
        data = arrayConcat(arrayConcat(SALTED_MAGIC, salt), data);
        return Base64.getEncoder().encodeToString(data);
    }

    public static String decrypt(@NotNull String input, @NotNull String key) throws Exception {
        byte[] secretKey = key.getBytes();
        byte[] inBytes = Base64.getMimeDecoder().decode(input);
        byte[] shouldBeMagic = Arrays.copyOfRange(inBytes, 0, SALTED_MAGIC.length);
        if (!Arrays.equals(shouldBeMagic, SALTED_MAGIC)) {
            return "Initial bytes from input do not match OpenSSL SALTED_MAGIC salt value.";
        }
        byte[] salt = Arrays.copyOfRange(inBytes, SALTED_MAGIC.length, SALTED_MAGIC.length + 8);
        byte[] passAndSalt = arrayConcat(secretKey, salt);
        byte[] hash = new byte[0];
        byte[] keyAndIv = new byte[0];
        int i = 0;
        while (i < 3 && keyAndIv.length < 48) {
            byte[] hashData = arrayConcat(hash, passAndSalt);
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(hashData);
            keyAndIv = arrayConcat(keyAndIv, hash);
            i++;
        }
        byte[] keyValue = Arrays.copyOfRange(keyAndIv, 0, 32);
        byte[] iv = Arrays.copyOfRange(keyAndIv, 32, 48);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyValue, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] clear = cipher.doFinal(inBytes, 16, inBytes.length - 16);
        return new String(clear, StandardCharsets.UTF_8);
    }
}
