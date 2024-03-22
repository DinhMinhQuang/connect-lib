package https;

import crypto.AES;
import crypto.Md5;
import crypto.RSA;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Request {
    private final String path;
    private final String appId;

    private final String publicKeyPayME;
    private final String privateKey;

    Request(String path, String appId, String privateKey,
            String publicKeyPayME) {
        this.path = path;
        this.appId = appId;
        this.privateKey = privateKey;
        this.publicKeyPayME = publicKeyPayME;
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
            String xApiKey = RSA.Encrypt(encryptKey, this.publicKeyPayME);
            String xApiAction = AES.encrypt(this.path, encryptKey);
            String xApiMessage = null;
            System.out.println(payload.toString());
            if (payload.isNull(String.valueOf(false))) {
                xApiMessage = AES.encrypt(payload.toString(), encryptKey);
            }

            ArrayList<String> arrayList = new ArrayList<>();
            arrayList.add(xApiAction);
            arrayList.add(method);

            if (!Objects.equals(accessToken, "")) {
                arrayList.add(accessToken);
            }
            arrayList.add(xApiMessage);

            String xApiValidate = Md5.hash(String.join("", arrayList) + encryptKey);
            System.out.printf(String.join("", arrayList) + encryptKey);
            System.out.print(xApiValidate);

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

    protected JSONObject decrypt(String xApiAction, String method, String xApiClient,
                                 String xApiKey, String xApiMessage, String xApiValidate, String accessToken) throws JSONException {
        String encryptKey = null;
        try {
            encryptKey = RSA.Decrypt(xApiKey, this.privateKey);
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
        ;
        String body = null;
        try {
            body = AES.decrypt(xApiMessage, encryptKey);
        } catch (Exception e) {
            throw new RuntimeException("Thông tin x-api-message không chính xác" + e.getMessage());
        }
        return new JSONObject(body);
    }

}
