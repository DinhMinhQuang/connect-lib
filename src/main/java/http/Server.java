package main.java.http;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Server {
    private final String port;
    public Server(String port) {
        this.port = port;
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(Integer.parseInt(this.port)), 0);
        server.createContext("/receive/ipn", new HandlerIPN());
        server.setExecutor(null);
        server.start();
        System.out.println("Server running on: " + server.getAddress());
    }
}
