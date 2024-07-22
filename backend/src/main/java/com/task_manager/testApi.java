package com.task_manager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.zaxxer.hikari.HikariDataSource;


public class testApi {

    private static HikariDataSource dataSource;
    public static String resultData = "";

    public static void main(String[] args) throws IOException, SQLException {

        try {
            initDatabaseConnectionPool();
            readData();

            // Créer un serveur HTTP sur le port 8080
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            
            // Définir les gestionnaires pour différents chemins
            server.createContext("/api/hello", new HelloHandler());
            server.createContext("/api/goodbye", new GoodbyeHandler());
            
            // Démarrer le serveur
            server.setExecutor(null); // Créer un pool de threads par défaut
            server.start();
            
            System.out.println("Server is running on port 8080...");

        } finally {
            closeDatabaseConnectionPool();
        }
    }

    private static void initDatabaseConnectionPool() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mariadb://localhost:3306/our_pronos");
        dataSource.setUsername("lamacqdev");
        dataSource.setPassword("mymdpdev44");
    }

    private static void closeDatabaseConnectionPool() {
        dataSource.close();
    }

    private static void readData() throws SQLException {
        System.out.println("Reading data...");
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("""
                SELECT firstname, lastname, email, password
                FROM user
            """)) {
                ResultSet resultSet = statement.executeQuery();
                boolean empty = true;
                StringBuilder dataBuilder = new StringBuilder();
                while(resultSet.next()) {
                    String firstname = resultSet.getString(1);
                    String lastname = resultSet.getString(2);
                    String email = resultSet.getString(3);
                    String password = resultSet.getString(4);
                    System.out.println("\t>" + firstname + " " + lastname + " by " + email + " " + password);
                    dataBuilder.append(firstname).append(" ").append(lastname).append(" ").append(email).append(" ").append(password).append("\n");
                    empty = false;
                }
                if(empty) {
                    System.out.println("\t (no data)");
                    resultData = "(no data)";
                } else {
                    resultData = dataBuilder.toString();
                }
            }
        }
    }
}
    
class HelloHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = testApi.resultData.isEmpty() ? "No data available" : testApi.resultData;
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