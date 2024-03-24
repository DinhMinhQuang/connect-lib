package main.java.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import main.java.models.Config;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import java.util.List;
import java.util.Map;


public class HandlerIPN implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.getResponseHeaders().set("Accept", "application/json");

            String requestMethod = exchange.getRequestMethod();
            System.out.println("Request Method IPN: " + requestMethod);

            Map<String, List<String>> headers = exchange.getRequestHeaders();
            System.out.println("Request Headers IPN: ");
            headers.forEach((key, value) -> System.out.println("Key: " + key + " Value: " + value));

            StringBuilder requestBody = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }
            }
            JSONObject body = new JSONObject(requestBody.toString());
            System.out.println("Request Body IPN: " + requestBody);

            Config config = Config.getInstance();
            ExternalAPI request = new ExternalAPI("", config.getAppId_payME(), config.getPrivateKey(), config.getPublicKey_payME());
            JSONObject requestPartner;

            requestPartner = request.decrypt(headers.get("X-api-action").get(0),
                    requestMethod,
                    headers.get("X-api-key").get(0),
                    body.getString("x-api-message"),
                    headers.get("X-api-validate").get(0),
                    "");

            System.out.println("IPN data: " + requestPartner);

            String response = "Thành công";
            byte[] responseBytes = response.getBytes();
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                int chunkSize = 1024; // Set the chunk size
                int offset = 0;
                while (offset < responseBytes.length) {
                    int bytesToWrite = Math.min(chunkSize, responseBytes.length - offset);
                    outputStream.write(responseBytes, offset, bytesToWrite);
                    offset += bytesToWrite;
                }
            }
        } catch (Exception e) {
            System.out.println("Error " + e.getMessage());
            String response = e.getMessage();
            byte[] responseBytes = response.getBytes();
            exchange.sendResponseHeaders(500, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                int chunkSize = 1024; // Set the chunk size
                int offset = 0;
                while (offset < responseBytes.length) {
                    int bytesToWrite = Math.min(chunkSize, responseBytes.length - offset);
                    outputStream.write(responseBytes, offset, bytesToWrite);
                    offset += bytesToWrite;
                }
            }
        }
    }
}
