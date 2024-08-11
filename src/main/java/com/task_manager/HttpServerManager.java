package com.task_manager;

import java.io.IOException;
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

    public void init(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/home", new HomeHandler());
        server.createContext("/api/goodbye", new GoodbyeHandler());
        server.createContext("/api/create", new CreateHandler());
        server.createContext("/api/update/", new UpdateHandler());
        server.createContext("/api/delete/", new DeleteHandler());

        // Configurer un exécuteur par défaut
        server.setExecutor(null);
    }

    public void start() {
        if (server != null) {
            server.start();
            System.out.println("Server is running...");
        } else {
            throw new IllegalStateException("Server not initialized");
        }
    }

    public void stop(int delay) {
        if (server != null) {
            server.stop(delay);
        } else {
            throw new IllegalStateException("Server not initialized");
        }
    }

    static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                App.readData();
            } catch (SQLException e) {
                sendErrorResponse(exchange, "Database read error");
                return;
            }
            exchange.getResponseHeaders().set("Content-Type", "application/json");

            byte[] response = App.resultData.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }

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

    static class CreateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                try {
                    JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                    String description = jsonObject.get("description").getAsString();

                    App.createData(description);
                    String response = "Note created";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (JsonSyntaxException | SQLException e) {
                    sendErrorResponse(exchange, "Failed to create note");
                }
            } else {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            }
        }
    }

    static class UpdateHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("PUT".equals(exchange.getRequestMethod())) {
                URI requestUri = exchange.getRequestURI();
                String path = requestUri.getPath();
                String[] segments = path.split("/");
                if (segments.length != 4) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                int id = 0;
                try {
                    id = Integer.parseInt(segments[3]);
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(400, -1);
                }

                String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                try {
                    JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                    String description = jsonObject.get("description").getAsString();

                    App.updateData(description, id);

                    String response = "Note updated";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (JsonSyntaxException | SQLException e) {
                    sendErrorResponse(exchange, "Failed to update note");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

    }

    static class DeleteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("DELETE".equals(exchange.getRequestMethod())) {
                URI requestUri = exchange.getRequestURI();
                String path = requestUri.getPath();
                String[] segments = path.split("/");
                if (segments.length != 4) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                int id;
                try {
                    id = Integer.parseInt(segments[3]);
                } catch (NumberFormatException e) {
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }

                try {
                    App.deleteData(id);
                    String response = "Note deleted";
                    exchange.sendResponseHeaders(200, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                } catch (SQLException e) {
                    sendErrorResponse(exchange, "Failed to delete note");
                }
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }

    }

    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "https://task-manager-one-orcin.vercel.app");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
        String response = message;
        exchange.sendResponseHeaders(500, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
