package com.eugene.notesapp.db;

import com.eugene.notesapp.exception.DataAccessException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcTransaction {
    private JdbcTransaction() { }

    @FunctionalInterface
    public interface Work {
        void execute(Connection connection) throws SQLException;
    }

    public static void run(DataSource source, Work work) {
        try (Connection connection = source.getConnection()) {
            connection.setAutoCommit(false);
            try {
                work.execute(connection);
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                try { connection.rollback(); }
                catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database transaction failed", e);
        }
    }
}
