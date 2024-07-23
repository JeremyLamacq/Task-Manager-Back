package com.task_manager;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    // Initialiser le pool de connexions
    public static void init() {
        dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:mariadb://localhost:3306/task_manager");
        dataSource.setUsername("lamacqdev");
        dataSource.setPassword("mymdpdev44");
    }

    // Fermer le pool de connexions
    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    // Obtenir une connexion à partir du pool
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }
}
