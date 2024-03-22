package https;

import java.io.*;
import java.net.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import env.Config;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

public class Https {
    public static @NotNull JSONObject POST(String requestURL, String path, JSONObject requestBody, String accessToken) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            Request request = new Request(path, Config.xApiClient, Config.privateKey, Config.publicKeyPayME);

            JSONObject requestEncrypt = request.encrypt("POST", requestBody, accessToken);
            String params = requestEncrypt.getJSONObject("body").toString();

            JSONObject requestHeader = requestEncrypt.getJSONObject("headers");
            System.out.println(requestHeader);

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
            System.out.println("Response Body:");
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            JSONObject body = null;
            while ((inputLine = in.readLine()) != null) {
                body = new JSONObject(inputLine);
            }
            System.out.println(body);

            JSONObject headers = new JSONObject();

            in.close();
            connection.disconnect();

            assert body != null;
            return request.decrypt(connection.getHeaderField("x-api-action"),
                    "POST",
                    connection.getHeaderField("x-api-client"),
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
                    System.out.println(errorResponse.toString());
                } catch (Exception ex) {
                    System.out.println("Error reading error response: " + ex.getMessage());
                }
            }
            throw new RuntimeException(e);
        }
    }

}
