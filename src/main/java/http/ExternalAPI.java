package main.java.http;

import main.java.utility.AES;
import main.java.utility.Md5;
import main.java.utility.RSA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class ExternalAPI {
    private final String path;
    private final String appId;

    private final String publicKeyPayME;
    private final String privateKey;
    private final String secretKey;

    ExternalAPI(String path, String appId, String privateKey,
            String publicKeyPayME, @Nullable String secretKey) {
        this.path = path;
        this.appId = appId;
        this.privateKey = privateKey;
        this.publicKeyPayME = publicKeyPayME;
        this.secretKey = secretKey;
    }

    public static @NotNull String generateRandomString(int length) {
        // Define the characters that can be used in the random string
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        // Create a StringBuilder to store the random string
        StringBuilder sb = new StringBuilder(length);

        // Create a Random object
        Random random = new Random();

        // Generate random characters and append them to the StringBuilder
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }

        // Convert the StringBuilder to a String and return it
        return sb.toString();
    }

    protected JSONObject encrypt(String method, JSONObject payload, String accessToken) throws Exception {
        try {
            String encryptKey = generateRandomString(21);
            String xApiKey = RSA.encrypt(encryptKey, this.publicKeyPayME);
            String xApiAction = AES.encrypt(this.path, encryptKey);
            String xApiMessage = null;

            if (payload.length() != 0) {
                xApiMessage = AES.encrypt(payload.toString(), encryptKey);
            }
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(xApiAction);
            arrayList.add(method);
            if (!accessToken.isEmpty()) {
                arrayList.add(accessToken);
            }
            if (xApiMessage != null && !xApiMessage.isEmpty()) {
                arrayList.add(xApiMessage);
            }

            System.out.println(arrayList);
            String xApiValidate = Md5.hash(String.join("", arrayList) + encryptKey);

            JSONObject body = new JSONObject().put("x-api-message", xApiMessage);
            JSONObject headers = new JSONObject();
            headers.put("x-api-client", this.appId);
            headers.put("x-api-key", xApiKey);
            headers.put("x-api-action", xApiAction);
            headers.put("x-api-validate", xApiValidate);
            if (!accessToken.isEmpty()) {
                headers.put("Authorization", accessToken);
            }
            JSONObject request = new JSONObject();
            request.put("body", body);
            request.put("headers", headers);
            return request;
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    protected JSONObject decrypt(String xApiAction, String method,
                                 String xApiKey, String xApiMessage, String xApiValidate, String accessToken) throws JSONException {
        String encryptKey;
        try {
            encryptKey = RSA.decrypt(xApiKey, this.privateKey);
        } catch (Exception e) {
            throw new RuntimeException("Thông tin x-api-key không chính xác" + e.getMessage());
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(xApiAction);
        arrayList.add(method);
        arrayList.add(accessToken);
        arrayList.add(xApiMessage);
        String validate = Md5.hash(String.join("", arrayList) + encryptKey);
        if (!validate.equals(xApiValidate)) {
            throw new RuntimeException("Thông tin x-api-validate không chính xác");
        }

        String body;
        try {
            body = AES.decrypt(xApiMessage, encryptKey);
        } catch (Exception e) {
            throw new RuntimeException("Thông tin x-api-message không chính xác" + e.getMessage());
        }
        return new JSONObject(body);
    }
    protected String hashChecksum(String method, String path, JSONObject payload) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(method);
        arrayList.add(path);
        arrayList.add(payload.toString());
        return Md5.hash(String.join("", arrayList) + secretKey);
    }

    protected JSONObject validateChecksum(String method, String path, JSONObject payload, String xApiValidate) throws JSONException {
            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(path);
            arrayList.add(method);
            JSONObject body = new JSONObject();
            body.putOpt("code", payload.get("code"));
            body.put("message", payload.get("message"));
            body.put("data", payload.get("data"));
            arrayList.add(body.toString());
            String validate = Md5.hash(String.join("", arrayList) + secretKey);
            System.out.println(validate);
            System.out.println(String.join("", arrayList) + secretKey);
            if (!validate.equals(xApiValidate)) {
                throw new RuntimeException("Thông tin x-api-validate không chính xác");
            }
            return payload;
    }
}
