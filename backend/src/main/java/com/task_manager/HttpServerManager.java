package com.task_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HttpServerManager {

    private HttpServer server;

    // Initialiser le serveur HTTP
    public void init(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Définir les gestionnaires pour différents chemins
        server.createContext("/api/hello", new HelloHandler());
        server.createContext("/api/goodbye", new GoodbyeHandler());
        server.createContext("/api/notes", new NotesHandler());

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

    // Gestionnaire des requêtes Hello
    static class HelloHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
    
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

    // Gestionnaire des requêtes Notes
    static class NotesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream requestBody = exchange.getRequestBody();
                String description = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
                
                try {
                    TestApi.createData(description);
                    String response = "Note created";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (SQLException e) {
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
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
