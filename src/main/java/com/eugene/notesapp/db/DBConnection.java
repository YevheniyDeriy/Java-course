package com.eugene.notesapp.db;

import com.eugene.notesapp.exception.DataAccessException;
import com.eugene.notesapp.model.BaseEntity;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/** A thread-safe singleton of the pool, never of a shared Connection. */
public final class DBConnection implements AutoCloseable {
    private final HikariDataSource dataSource;

    private DBConnection() {
        Properties properties = new Properties();
        try (InputStream input = resource("application.properties")) {
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read application.properties", e);
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(setting(properties, "db.url", "NOTES_DB_URL"));
        config.setUsername(setting(properties, "db.user", "NOTES_DB_USER"));
        config.setPassword(setting(properties, "db.password", "NOTES_DB_PASSWORD"));
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.poolSize")));
        config.setMinimumIdle(1);
        config.setPoolName("notes-pool");
        dataSource = new HikariDataSource(config);
        try {
            initialize(dataSource);
            // Preserve the immutable-id contract of LR7 after restarting the application.
            try (Connection connection = getConnection();
                 PreparedStatement statement = connection.prepareStatement(
                         "SELECT MAX(id) FROM (SELECT id FROM notes UNION ALL SELECT id FROM categories)");
                 ResultSet rows = statement.executeQuery()) {
                rows.next();
                BaseEntity.reserveIdsThrough(rows.getLong(1));
            }
        } catch (RuntimeException | SQLException e) {
            dataSource.close();
            throw new IllegalStateException("Cannot initialize database", e);
        }
    }

    private static final class Holder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() { return Holder.INSTANCE; }

    public Connection getConnection() throws SQLException { return dataSource.getConnection(); }

    public DataSource getDataSource() { return dataSource; }

    private static String setting(Properties properties, String key, String environment) {
        String value = System.getenv(environment);
        return value == null ? properties.getProperty(key) : value;
    }

    private static InputStream resource(String name) {
        InputStream input = DBConnection.class.getClassLoader().getResourceAsStream(name);
        if (input == null) throw new IllegalStateException("Missing resource: " + name);
        return input;
    }

    /** This application's DDL has no semicolons inside strings or comments. */
    public static void initialize(DataSource source) {
        try (InputStream input = resource("init.sql"); Connection connection = source.getConnection()) {
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) {
                    try (PreparedStatement statement = connection.prepareStatement(sql)) {
                        statement.execute();
                    }
                }
            }
        } catch (IOException | SQLException e) {
            throw new DataAccessException("Cannot execute init.sql", e);
        }
    }

    @Override
    public void close() { dataSource.close(); }
}
