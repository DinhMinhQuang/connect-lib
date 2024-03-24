package main.java.http;

import java.io.*;
import java.net.*;

import main.java.models.Config;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class Http {

    public static @NotNull JSONObject POST(String path, JSONObject requestBody, String accessToken) throws IOException {
        Config config = Config.getInstance();
        URL url = new URL(config.getUrl_payME());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ExternalAPI request = new ExternalAPI(path, config.getAppId_payME(), config.getPrivateKey(), config.getPublicKey_payME());

            JSONObject requestEncrypt = request.encrypt("POST", requestBody, accessToken);
            String params = requestEncrypt.getJSONObject("body").toString();

            JSONObject requestHeader = requestEncrypt.getJSONObject("headers");

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            if (!accessToken.isEmpty()) {
                connection.setRequestProperty("Authorization", requestHeader.get("Authorization").toString());
            }
            connection.setRequestProperty("x-api-validate", requestHeader.get("x-api-validate").toString());
            connection.setRequestProperty("x-api-key", requestHeader.get("x-api-key").toString());
            connection.setRequestProperty("x-api-client", requestHeader.get("x-api-client").toString());
            connection.setRequestProperty("x-api-action", requestHeader.get("x-api-action").toString());
            connection.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(connection.getOutputStream())) {
                wr.writeBytes(params);
                wr.flush();
            }
            // Get the response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            String responseMessage = connection.getResponseMessage();
            System.out.println("Response Message: " + responseMessage);

            // Read the response body
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            JSONObject body = null;
            while ((inputLine = in.readLine()) != null) {
                body = new JSONObject(inputLine);
            }
            System.out.println("Response Body: " + body);
            if (body == null) {
                JSONObject message = new JSONObject();
                message.put("code", -1);
                message.put("message", "Body is null");
                return message;
            }
            in.close();
            connection.disconnect();

            return request.decrypt(connection.getHeaderField("x-api-action"),
                    "POST",
                    connection.getHeaderField("x-api-key"),
                    body.getString("x-api-message"),
                    connection.getHeaderField("x-api-validate"),
                    accessToken);
        } catch (Exception e) {
            if (connection != null) {
                try {
                    // Read the error response body
                    BufferedReader errorIn = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    String inputLine;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((inputLine = errorIn.readLine()) != null) {
                        errorResponse.append(inputLine);
                    }
                    errorIn.close();

                    // Print the error response body
                    System.out.println("Error Response Body:");
                    System.out.println(errorResponse);
                } catch (Exception ex) {
                    System.out.println("Error reading error response: " + ex.getMessage());
                }
            }
            throw new RuntimeException(e);
        }
    }

}
