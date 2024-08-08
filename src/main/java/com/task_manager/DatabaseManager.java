package com.task_manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static HikariDataSource dataSource;

    public static void init() {
        String url = System.getenv("DATABASE_URL");

        if (Objects.isNull(url) || url.isEmpty()) {
            throw new IllegalStateException("Database URL is missing or invalid");
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);

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
