package main.java;

import main.java.http.Server;
import main.java.models.Config;
import main.java.partner.PayME;
import org.json.JSONObject;

public class Main {

    public static void main(String[] args) throws Exception {
        // create example main.java.http server to handler ipn
        // you should use tool to expose your localhost to internet, example: ngrok
        Server server = new Server(Config.getInstance().getPort());
        server.start();

        JSONObject createWeb = PayME.createWeb();
        System.out.println(createWeb);
//      {"code":105000,"data":{"url":"https://sbx.payme.vn/g/payment/G1NC8N1VCULM","transaction":"G1NC8N1VCULM"},"message":"Tạo đơn thanh toán thành công"}
    }
}