package main.java.partner;

import main.java.http.ExternalAPI;
import main.java.http.Http;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class PayME {

    @NotNull
    private static JSONObject getBody() throws UnknownHostException, JSONException {
        String transaction = ExternalAPI.generateRandomString(10);

        InetAddress ipAddress = InetAddress.getLocalHost();
        int amount = 20000;
        String desc = "Thanh toán đơn hàng";
        String ipnUrl = "https://43c4-27-75-110-72.ngrok-free.app/receive/ipn";
        String payMethod = "PAYME";

        JSONObject body = new JSONObject();
        body.put("partnerTransaction", transaction);
        body.put("amount", amount);
        body.put("desc", desc);
        body.put("ipnUrl", ipnUrl);
        body.put("ipnUrl", ipnUrl);
        body.put("payMethod", payMethod);
        System.out.println(ipAddress);
        body.put("ip", ipAddress.getHostAddress());
        return body;
    }

    private static JSONObject getBodyPayout() {
        return new JSONObject();
    }
    public static JSONObject createWebCheckSum() {
        try {
            JSONObject body = getBody();
            return Http.POSTChecksum("/payment/web","POST", body);
        } catch(JSONException | IOException e) {
            System.out.println("Error when calling /payment/web: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static JSONObject createPayout() {
        try {
            JSONObject body = getBody();
            return Http.POST("/payout", body, "");
        } catch(JSONException | IOException e) {
            System.out.println("Error when calling /payment/web: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static JSONObject createDirect() {
        try {
            JSONObject body = getBody();
            return Http.POST("/payment/direct", body, "");
        } catch(JSONException | IOException e) {
            System.out.println("Error when calling /payment/web: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    public static JSONObject createWeb() {
        try {
            JSONObject body = getBody();
            return Http.POST("/payment/web", body, "");
        } catch(JSONException | IOException e) {
            System.out.println("Error when calling /payment/web: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static JSONObject getPayment(String transaction) {
        try {
            JSONObject body = new JSONObject();
            body.put("partnerTransaction",  transaction);
            return Http.POST("/payment/query", body, "");
        } catch(JSONException | IOException e) {
            System.out.println("Error when calling /payment/query: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    public static JSONObject getEWallet() {
        try {
            String requestPath = "/be/ewallet/account?" + "phone=0333823057";
            return Http.GET(requestPath, "");
        } catch(IOException e) {
            System.out.println("Error when calling /payment/web: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
