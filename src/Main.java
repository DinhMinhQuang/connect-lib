import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import crypto.AES;
import crypto.Md5;

import https.HandlerIPN;
import https.Https;
import https.Request;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


public class Main {

    public static void main(String[] args) throws Exception {
        // create example http server to handler ipn
        // you should user tool to external localhost to internet example ngrok
        HttpServer server = HttpServer.create(new InetSocketAddress(3001), 0);
        server.createContext("/receive/ipn", new HandlerIPN());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server running on: " + server.getAddress());

        JSONObject body = getBody();
        String url = "https://sbx-gapi.payme.vn";

        JSONObject request = Https.POST(url, "/payment/web", body, "");
        System.out.println(request);
        // {"code":105000,"data":{"url":"https://sbx.payme.vn/g/payment/G1NC8N1VCULM","transaction":"G1NC8N1VCULM"},"message":"Tạo đơn thanh toán thành công"}

//        JSONObject queryBody = new JSONObject();
//        body.put("partnerTransaction", partnerTransaction);
//        JSONObject query = Https.POST(url, "/payment/query", body, "");
//        System.out.println(query);
        // {"code":105002,"data":{"amount":2000000,"partnerTransaction":"opJjk7x1cA","method":"","extraData":null,"fee":0,"createdAt":"2024-03-22T04:27:52.369Z","total":2000000,"merchantId":90859,"amountDetail":{"usd":80.98,"vnd":2000000,"cny":534.01},"state":"PENDING","transaction":"AP0TX5OB6QNS","updatedAt":"2024-03-22T04:27:52.397Z","desc":"Thanh toán đơn hàng"},"message":"Truy vấn thông tin giao dịch thành công"}


    }

    @NotNull
    private static JSONObject getBody() throws UnknownHostException, JSONException {
        String transaction = Request.generateRandomString(10);
        InetAddress ipAddress = InetAddress.getLocalHost();
        int amount = 20000;
        String desc = "Thanh toán đơn hàng";
        String ipnUrl = "https://c122-113-161-36-155.ngrok-free.app/receive/ipn";

        JSONObject body = new JSONObject();
        body.put("partnerTransaction", transaction);
        body.put("ip", ipAddress.toString());
        body.put("amount", amount);
        body.put("desc", desc);
        body.put("ipnUrl", ipnUrl);
        return body;
    }
}