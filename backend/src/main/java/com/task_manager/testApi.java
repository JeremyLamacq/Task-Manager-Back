package com.task_manager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class testApi {
    public static void main(String[] args) throws IOException {
        // Créer un serveur HTTP sur le port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Définir les gestionnaires pour différents chemins
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/goodbye", new GoodbyeHandler());

        // Démarrer le serveur
        server.setExecutor(null); // Créer un pool de threads par défaut
        server.start();

        System.out.println("Server is running on port 8080...");
    }
}

class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "Hello, world!";
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}

class GoodbyeHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "Goodbye, world!";
        exchange.sendResponseHeaders(200, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
