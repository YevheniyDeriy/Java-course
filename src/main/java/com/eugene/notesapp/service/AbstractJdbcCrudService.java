package com.eugene.notesapp.service;

import com.eugene.notesapp.db.JdbcTransaction;
import com.eugene.notesapp.exception.DataAccessException;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.BaseEntity;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Template Method: resource handling and CRUD stay here; SQL and mapping vary. */
public abstract class AbstractJdbcCrudService<T extends BaseEntity> implements CRUDService<T> {
    protected final DataSource dataSource;

    protected AbstractJdbcCrudService(DataSource source) {
        dataSource = Objects.requireNonNull(source, "source");
    }

    protected abstract String selectSql();
    protected abstract String saveSql();
    protected abstract String deleteSql();
    protected abstract T mapRow(ResultSet row) throws SQLException;
    protected abstract void setParameters(PreparedStatement statement, T entity) throws SQLException;

    protected void beforeSave(Connection connection, T entity) throws SQLException { }

    @FunctionalInterface
    protected interface Parameters {
        void bind(PreparedStatement statement) throws SQLException;
    }

    protected final List<T> query(String sql, Parameters parameters) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            parameters.bind(statement);
            try (ResultSet rows = statement.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rows.next()) result.add(mapRow(rows));
                return result;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Cannot read entities", e);
        }
    }

    @Override
    public final T findById(long id) {
        List<T> rows = query(selectSql() + " WHERE e.id = ?", s -> s.setLong(1, id));
        if (rows.isEmpty()) throw new EntityNotFoundException(id);
        return rows.getFirst();
    }

    @Override
    public final void save(T entity) {
        Objects.requireNonNull(entity, "entity");
        JdbcTransaction.run(dataSource, connection -> {
            beforeSave(connection, entity);
            try (PreparedStatement statement = connection.prepareStatement(saveSql())) {
                setParameters(statement, entity);
                statement.executeUpdate();
            }
        });
    }

    @Override
    public final boolean delete(long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(deleteSql())) {
            statement.setLong(1, id);
            return statement.executeUpdate() != 0;
        } catch (SQLException e) {
            throw new DataAccessException("Cannot delete entity " + id, e);
        }
    }

    @Override
    public final List<T> findAll() {
        return query(selectSql() + " ORDER BY e.id", s -> { });
    }

    @Override
    public final List<T> findAllSortedByName() {
        return query(selectSql() + " ORDER BY e.name, e.id", s -> { });
    }

    @Override
    public final List<T> findAllSorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        List<T> result = findAll();
        result.sort(comparator);
        return result;
    }

    public final List<T> findPage(int limit, long offset) {
        if (limit < 1 || limit > 1000 || offset < 0) {
            throw new IllegalArgumentException("limit must be 1..1000; offset must be nonnegative");
        }
        return query(selectSql() + " ORDER BY e.name, e.id LIMIT ? OFFSET ?", s -> {
            s.setInt(1, limit);
            s.setLong(2, offset);
        });
    }

    /** A single transaction, with bounded driver batches. Nothing remains on failure. */
    public final void saveBatch(Collection<? extends T> entities) {
        List<T> snapshot = new ArrayList<>(Objects.requireNonNull(entities, "entities"));
        snapshot.forEach(entity -> Objects.requireNonNull(entity, "entity"));
        JdbcTransaction.run(dataSource, connection -> {
            try (PreparedStatement statement = connection.prepareStatement(saveSql())) {
                int count = 0;
                for (T entity : snapshot) {
                    beforeSave(connection, entity);
                    setParameters(statement, entity);
                    statement.addBatch();
                    if (++count % 500 == 0) {
                        statement.executeBatch();
                        statement.clearBatch();
                    }
                }
                if (count % 500 != 0) statement.executeBatch();
            }
        });
    }
}
