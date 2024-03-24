package main.java.utility;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.pkcs.*;

import java.util.Base64;


public class RSA {
    private static PublicKey getPublicKey(String puKey) {
        try {
            String publicKeyPEM = puKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");
            byte[] result = Base64.getMimeDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(result);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error RSA.getPublicKey " + e.getCause());
            throw new RuntimeException(e);
        }
    }

    private static PrivateKey getPrivateKey(String pkey) throws NoSuchAlgorithmException {
        try {
            String privateKey = pkey.replace("-----BEGIN RSA PRIVATE KEY-----", "").replaceAll(System.lineSeparator(), "")
                    .replace("-----END RSA PRIVATE KEY-----", "");
            byte[] encoded = Base64.getMimeDecoder().decode(privateKey);

            RSAPrivateKey rsaPrivateKey = RSAPrivateKey.getInstance(encoded);
            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(rsaPrivateKey.getModulus(),
                    rsaPrivateKey.getPrivateExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(rsaPrivateKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.out.println("Error RSA.getPrivateKey " + e.getCause());
            throw new RuntimeException(e);
        }
    }

    public static String encrypt(String original, String key) throws Exception {
        try {
            PublicKey publicKey = getPublicKey(key);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] byteEncrypted = cipher.doFinal(original.getBytes());
            return Base64.getEncoder().encodeToString(byteEncrypted);
        } catch(Exception e) {
            System.out.println("Error RSA.Encrypt " + e.getCause());
            throw new Exception(e);
        }
    }

    public static String decrypt(String encryptText, String key) throws NoSuchAlgorithmException {
        try {
            byte[] result = Base64.getMimeDecoder().decode(encryptText);
            PrivateKey privateKey = getPrivateKey(key);
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] byteDecrypted = cipher.doFinal(result);

            return new String(byteDecrypted);
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            System.out.println("Error RSA.decrypt " + e.getCause());
            throw new NoSuchAlgorithmException(e);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            System.out.println("Error private key exception " + e.getCause());
            throw new RuntimeException(e);
        }
    }
}
