package com.task_manager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

public class TestApi {

    public static String resultData = "";

    public static void main(String[] args) throws IOException, SQLException {
        HttpServerManager serverManager = new HttpServerManager();
        try {
            DatabaseManager.init();
            readData();
            createData("trop tard");
            readData();

            // Initialiser et démarrer le serveur HTTP sur le port 8080
            serverManager.init(8080);
            serverManager.start();

        // Ajouter un hook pour fermer la connexion proprement quand le serveur s'arrête
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                serverManager.stop(0);
                DatabaseManager.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
    }

    public static void createData(String description) throws SQLException {
        System.out.println("Creating data with description: " + description);
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO notes (description) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, description);
                int rowsInserted = statement.executeUpdate();
                System.out.println("Rows affected: " + rowsInserted);
                // statement.close();

            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            throw e;
        }
    }

    public static void readData() throws SQLException {
        System.out.println("Reading data...");
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT description FROM notes")) {
                ResultSet resultSet = statement.executeQuery();
                List<String> dataList = new ArrayList<>();
                while (resultSet.next()) {
                    String description = resultSet.getString(1);
                    System.out.println(description);
                    dataList.add(description);
                }
                // Convertir la liste en JSON
                Gson gson = new Gson();
                resultData = gson.toJson(dataList);  
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
                throw e;
            }
        }
    }
}
