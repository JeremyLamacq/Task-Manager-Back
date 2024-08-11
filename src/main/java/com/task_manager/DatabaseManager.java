package com.task_manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    public static void init() {
        Dotenv dotenv = Dotenv.load();

        String url = dotenv.get("DB_URL");
        String username = dotenv.get("DB_USERNAME");
        String password = dotenv.get("DB_PASSWORD");

        if (Objects.isNull(url) || url.isEmpty() ||
                Objects.isNull(username) || username.isEmpty() ||
                Objects.isNull(password) || password.isEmpty()) {
            throw new IllegalStateException("Database credentials are missing or invalid");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(20);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);

        dataSource = new HikariDataSource(config);
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource not initialized");
        }
        return dataSource.getConnection();
    }
}
