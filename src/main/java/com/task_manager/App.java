package com.task_manager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);
    public static String resultData = "";

    public static void main(String[] args) throws IOException, SQLException {
        System.out.println("Application started!");
        HttpServerManager serverManager = new HttpServerManager();

        try {
            DatabaseManager.init();
            readData();

            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

            // Initialiser et démarrer le serveur HTTP sur le port 8080
            serverManager.init(port);
            serverManager.start();

            // Ajouter un hook pour fermer la connexion proprement quand le serveur s'arrête
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    serverManager.stop(0);
                    DatabaseManager.close();
                } catch (Exception e) {
                    logger.error("Error during shutdown", e);
                }
            }));

        } catch (SQLException e) {
            logger.error("SQL Exception occurred", e);
        } catch (IOException e) {
            logger.error("IO Exception occurred", e);
        }
    }

    public static void deleteData(int id) throws SQLException {
        System.out.println("Deleting data with id: " + id);
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM notes WHERE id = (?) ";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted == 0) {
                    throw new SQLException("No rows affected, deletion failed.");
                }
                System.out.println("Rows affected: " + rowsDeleted);

            }
        } catch (SQLException e) {
            System.err.println("Failed to delete data: " + e.getMessage());
            throw new SQLException("Error while deleting data", e);
        }
    }

    public static void updateData(String description, int id) throws SQLException {
        System.out.println("Updating data with id: " + id);
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "UPDATE notes SET description = (?) WHERE id = (?) ";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, description);
                statement.setInt(2, id);
                int rowsInserted = statement.executeUpdate();
                System.out.println("Rows affected: " + rowsInserted);

            }
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
            throw new SQLException("Error while updating data", e);
        }
    }

    public static void createData(String description) throws SQLException {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }
        System.out.println("Creating data with description: " + description);
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO notes (description) VALUES (?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, description);
                int rowsInserted = statement.executeUpdate();
                if (rowsInserted == 0) {
                    throw new SQLException("Insertion failed, no rows affected.");
                }
                System.out.println("Rows affected: " + rowsInserted);
            }
        } catch (SQLException e) {
            System.err.println("Failed to create data: " + e.getMessage());
            throw new SQLException("Error while creating data", e);
        }
    }

    public static void readData() throws SQLException {
        System.out.println("Reading data...");
        try (Connection connection = DatabaseManager.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, description FROM notes")) {
                ResultSet resultSet = statement.executeQuery();
                List<JsonObject> dataList = new ArrayList<>();
                while (resultSet.next()) {
                    int id = resultSet.getInt(1);
                    String description = resultSet.getString(2);
                    System.out.println("ID: " + id + ", Description: " + description);
                    // Créer un objet JSON pour chaque enregistrement
                    JsonObject note = new JsonObject();
                    note.addProperty("id", id);
                    note.addProperty("description", description);

                    dataList.add(note);
                }
                Gson gson = new Gson();
                resultData = gson.toJson(dataList);
            } catch (SQLException e) {
                System.err.println("SQL Exception: " + e.getMessage());
                throw new SQLException("Error while reading data", e);
            }
        }
    }
}
