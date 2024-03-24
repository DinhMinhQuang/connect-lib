package main.java.models;

import java.io.*;
import java.util.Properties;

public class Config {
    private String port;

    private String url_payME;
    private String publicKey_payME;
    private String appId_payME;
    private String privateKey;

    public String getUrl_payME() {
        return url_payME;
    }
    public String getPublicKey_payME() {
        return publicKey_payME;
    }
    public String getAppId_payME() {
        return appId_payME;
    }
    public String getPrivateKey() {
        return privateKey;
    }

    public String getPort() {
        return port;
    }


    private Config() {
        String filePath = "src/main/resources/config.properties";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            Properties properties = new Properties();
            properties.load(br);
            this.port = properties.getProperty("port");
            this.url_payME = properties.getProperty("url");
            this.publicKey_payME = properties.getProperty("publicKeyPayME");
            this.privateKey = properties.getProperty("privateKey");
            this.appId_payME = properties.getProperty("appId");
        } catch (IOException e) {
            System.out.println("Error when get config file " + e.getMessage());
        }

    }

    private static class SingletonHelper {
        private static final Config INSTANCE = new Config();
    }

    public static Config getInstance() {
        return SingletonHelper.INSTANCE;
    }

}
