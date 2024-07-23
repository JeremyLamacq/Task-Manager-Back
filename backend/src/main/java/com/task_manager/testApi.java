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

        } finally {
            DatabaseManager.close();
        }
    }

    public static void createData(String description) throws SQLException {
        System.out.println("Creating data...");
        int rowsInserted;
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO notes (description) VALUES (?)")) {
                statement.setString(1, description);
                rowsInserted = statement.executeUpdate();
                System.out.println("Rows inserted: " + rowsInserted);
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
                throw e;
            }
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
