package com.task_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpServerManager {

    private HttpServer server;

    // Initialiser le serveur HTTP
    public void init(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Définir les gestionnaires pour différents chemins
        server.createContext("/api/home", new HomeHandler());
        server.createContext("/api/goodbye", new GoodbyeHandler());
        server.createContext("/api/create", new CreateHandler());

        // Configurer un exécuteur par défaut
        server.setExecutor(null);
    }

    // Démarrer le serveur
    public void start() {
        if (server != null) {
            server.start();
            System.out.println("Server is running...");
        } else {
            throw new IllegalStateException("Server not initialized");
        }
    }

    // Arrêter le serveur
    public void stop(int delay) {
        if (server != null) {
            server.stop(delay);
        } else {
            throw new IllegalStateException("Server not initialized");
        }
    }

    // Gestionnaire des requêtes Home
    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Répondre aux requêtes OPTIONS CORS
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            // Lire les données de la base de données
            try {
                TestApi.readData();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Définir l'en-tête Content-Type pour JSON
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            // Envoyer la réponse
            exchange.sendResponseHeaders(200, TestApi.resultData.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(TestApi.resultData.getBytes());
            }
        }
    }

    // Gestionnaire des requêtes Goodbye
    static class GoodbyeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            String response = "Goodbye, world!";
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    // Gestionnaire des requêtes Create
    static class CreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Répondre aux requêtes OPTIONS CORS
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBodyStream = exchange.getRequestBody();
                String requestBody = new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);

                String description = "";
                try {
                    JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                    description = jsonObject.get("description").getAsString();
                    System.out.println(description); // Console log to check received value
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    TestApi.createData(description);
                    String response = "Note created";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    String response = "Failed to create note";
                    exchange.sendResponseHeaders(500, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    // Ajouter les en-têtes CORS
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
