package com.eugene.notesapp.service;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.db.JdbcTransaction;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.Category;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JdbcCategoryService extends AbstractJdbcCrudService<Category> {
    public JdbcCategoryService() { this(DBConnection.getInstance().getDataSource()); }
    public JdbcCategoryService(DataSource source) { super(source); }

    @Override
    protected String selectSql() { return "SELECT e.id, e.name FROM categories e"; }
    @Override
    protected String saveSql() { return "MERGE INTO categories (id, name) KEY(id) VALUES (?, ?)"; }
    @Override
    protected String deleteSql() { return "DELETE FROM categories WHERE id = ?"; }
    @Override
    protected Category mapRow(ResultSet row) throws SQLException {
        return new Category(row.getLong("id"), row.getString("name"));
    }
    @Override
    protected void setParameters(PreparedStatement statement, Category category) throws SQLException {
        statement.setLong(1, category.getId());
        statement.setString(2, category.getName());
    }

    /** Move notes, remove their old category and rename the destination atomically. */
    public void mergeCategories(long sourceId, long targetId, String targetName) {
        if (sourceId == targetId) throw new IllegalArgumentException("Categories must differ");
        JdbcTransaction.run(dataSource, connection -> {
            try (PreparedStatement lock = connection.prepareStatement(
                    "SELECT id FROM categories WHERE id IN (?, ?) ORDER BY id FOR UPDATE")) {
                lock.setLong(1, sourceId);
                lock.setLong(2, targetId);
                try (ResultSet rows = lock.executeQuery()) {
                    boolean sourceFound = false;
                    boolean targetFound = false;
                    while (rows.next()) {
                        sourceFound |= rows.getLong(1) == sourceId;
                        targetFound |= rows.getLong(1) == targetId;
                    }
                    if (!sourceFound) throw new EntityNotFoundException(sourceId);
                    if (!targetFound) throw new EntityNotFoundException(targetId);
                }
            }
            try (PreparedStatement move = connection.prepareStatement(
                    "UPDATE notes SET category_id = ? WHERE category_id = ?");
                 PreparedStatement delete = connection.prepareStatement(deleteSql());
                 PreparedStatement rename = connection.prepareStatement(
                         "UPDATE categories SET name = ? WHERE id = ?")) {
                move.setLong(1, targetId);
                move.setLong(2, sourceId);
                move.executeUpdate();
                delete.setLong(1, sourceId);
                delete.executeUpdate();
                // A duplicate/blank name fails AFTER both tables have changed: rollback restores both.
                rename.setString(1, targetName);
                rename.setLong(2, targetId);
                rename.executeUpdate();
            }
        });
    }
}
