package com.task_manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
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
        server.createContext("/api/update/", new UpdateHandler());
        server.createContext("/api/delete/", new DeleteHandler());

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
                } catch (JsonSyntaxException e) {
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

    // Gestionnaire des requêtes Update
    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Répondre aux requêtes OPTIONS CORS
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equals(exchange.getRequestMethod())) {
                URI requestUri = exchange.getRequestURI();
                String path = requestUri.getPath();
                String[] segments = path.split("/");
                if (segments.length != 4) { // /api/update/{id}
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                    return;
                }

                int id;
                try {
                    id = Integer.parseInt(segments[3]);
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                    return;
                }

                InputStream requestBodyStream = exchange.getRequestBody();
                String requestBody = new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);

                String description = "";
                try {
                    JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                    description = jsonObject.get("description").getAsString();
                    id = jsonObject.get("id").getAsInt();
                    System.out.println(description); // Console log to check received value
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }

                try {
                    TestApi.updateData(description, id);
                    String response = "Note updated";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    String response = "Failed to update note";
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

    // Gestionnaire des requêtes Update
    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Received request: " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                // Répondre aux requêtes OPTIONS CORS
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("DELETE".equals(exchange.getRequestMethod())) {
                URI requestUri = exchange.getRequestURI();
                String path = requestUri.getPath();
                String[] segments = path.split("/");
                if (segments.length != 4) { // /api/delete/{id}
                    System.out.println("Bad request: Incorrect URL segments");
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                    return;
                }

                int id;
                try {
                    id = Integer.parseInt(segments[3]);
                    System.out.println("Parsed ID: " + id);
                } catch (NumberFormatException e) {
                    System.out.println("Bad request: ID not a number");
                    exchange.sendResponseHeaders(400, -1); // Bad Request
                    return;
                }

                try {
                    TestApi.deleteData(id);
                    String response = "Note deleted";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    String response = "Failed to delete note";
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
