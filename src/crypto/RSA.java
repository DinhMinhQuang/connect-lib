package crypto;

import javax.crypto.Cipher;

import java.io.IOException;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.pkcs.*;

import java.util.Base64;


public class RSA {
    public static PublicKey GetPublicKey(String puKey) {
        try {
            String publicKeyPEM = puKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");
            byte[] result = Base64.getMimeDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(result);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey GetPrivateKey(String pkey) throws NoSuchAlgorithmException {
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
            throw new RuntimeException(e);
        }
    }

    public static String Encrypt(String original, String key) throws Exception {
        PublicKey publicKey = GetPublicKey(key);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] byteEncrypted = cipher.doFinal(original.getBytes());
        return Base64.getEncoder().encodeToString(byteEncrypted);
    }

    public static String Decrypt(String encryptText, String key) throws Exception {
        byte[] result = Base64.getMimeDecoder().decode(encryptText);
        PrivateKey privateKey = GetPrivateKey(key);
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA1AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] byteDecrypted = cipher.doFinal(result);

        return new String(byteDecrypted);
    }
}
