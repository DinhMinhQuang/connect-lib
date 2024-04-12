package main.java.http;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import main.java.models.Config;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Http {

    public static @NotNull JSONObject POST(String path, JSONObject requestBody, String accessToken) throws IOException {
        Config config = Config.getInstance();
        URL url = new URL(config.getUrl_payME());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ExternalAPI request = new ExternalAPI(path, config.getAppId_payME(), config.getPrivateKey(), config.getPublicKey_payME(), config.getSecretKey());

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

    public static @NotNull JSONObject POSTChecksum(String path,String method, JSONObject requestBody) throws IOException {
        Config config = Config.getInstance();
        String uri = config.getUrl_payME().concat(path);
        URL url = new URL(uri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ExternalAPI request = new ExternalAPI(path, config.getAppId_payME(), config.getPrivateKey(), config.getPublicKey_payME(), config.getSecretKey());
            String xApiValidate = request.hashChecksum(path, method.toUpperCase(), requestBody);
            String params = requestBody.toString();
            System.out.println(xApiValidate);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");

            connection.setRequestProperty("x-api-validate", xApiValidate);
            connection.setRequestProperty("x-api-client", config.getAppId_payME());
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
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
            return request.validateChecksum(method, path, body, connection.getHeaderField("x-api-validate"));
        } catch (JSONException e) {
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
            throw new RuntimeException(e);
        }
    }

    public static @NotNull JSONObject GET(String path, String accessToken) throws IOException {
        Config config = Config.getInstance();
        URL url = new URL(config.getUrl_payME());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ExternalAPI request = new ExternalAPI(path, config.getAppId_payME(), config.getPrivateKey(), config.getPublicKey_payME(), config.getSecretKey());
            JSONObject test = new JSONObject();
            JSONObject requestEncrypt = request.encrypt("GET", test, accessToken);
            System.out.println(requestEncrypt);
            String params = requestEncrypt.getJSONObject("body").toString();

            JSONObject requestHeader = requestEncrypt.getJSONObject("headers");
            System.out.println(requestHeader);

            connection.setRequestProperty("Content-Type", "application/json");
            if (!accessToken.isEmpty()) {
                connection.setRequestProperty("Authorization", requestHeader.getString("Authorization"));
            }
            connection.setRequestProperty("x-api-validate", requestHeader.getString("x-api-validate"));
            connection.setRequestProperty("x-api-key", requestHeader.getString("x-api-key"));
            connection.setRequestProperty("x-api-client", requestHeader.getString("x-api-client"));
            connection.setRequestProperty("x-api-action", requestHeader.getString("x-api-action"));

            // Không ghi dữ liệu vào body của yêu cầu

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
                    "GET",
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
