package com.eugene.notesapp.service;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.model.*;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public final class JdbcNoteService extends AbstractJdbcCrudService<Note> implements NoteService {
    public JdbcNoteService() { this(DBConnection.getInstance().getDataSource()); }
    public JdbcNoteService(DataSource source) { super(source); }

    @Override
    protected String selectSql() {
        return "SELECT e.*, c.name AS category_name FROM notes e JOIN categories c ON c.id = e.category_id";
    }
    @Override
    protected String saveSql() {
        return """
                MERGE INTO notes (id, name, content, created_on, category_id, priority,
                                  note_type, reminder_hours, repeat_hours) KEY(id)
                VALUES (?, ?, ?, ?, (SELECT id FROM categories WHERE name = ?), ?, ?, ?, ?)
                """;
    }
    @Override
    protected String deleteSql() { return "DELETE FROM notes WHERE id = ?"; }

    @Override
    protected void beforeSave(Connection connection, Note note) throws SQLException {
        try (PreparedStatement find = connection.prepareStatement("SELECT id FROM categories WHERE name = ?")) {
            find.setString(1, note.getCategory());
            try (ResultSet rows = find.executeQuery()) {
                if (rows.next()) return;
            }
        }
        Category category = new Category(note.getCategory());
        try (PreparedStatement insert = connection.prepareStatement("INSERT INTO categories (id, name) VALUES (?, ?)")) {
            insert.setLong(1, category.getId());
            insert.setString(2, category.getName());
            insert.executeUpdate();
        }
    }

    @Override
    protected void setParameters(PreparedStatement statement, Note note) throws SQLException {
        String type = switch (note) {
            case RecurringReminderNote ignored -> "RECURRING";
            case ReminderNote ignored -> "REMINDER";
            case TextNote ignored -> "TEXT";
        };
        statement.setLong(1, note.getId());
        statement.setString(2, note.getName());
        statement.setString(3, note.getContent());
        statement.setObject(4, note.getDate());
        statement.setString(5, note.getCategory());
        statement.setInt(6, note.getPriority());
        statement.setString(7, type);
        statement.setDouble(8, note.getReminderInHours());
        statement.setDouble(9, note instanceof RecurringReminderNote recurring ? recurring.getRepeatEveryHours() : 0);
    }

    @Override
    protected Note mapRow(ResultSet row) throws SQLException {
        long id = row.getLong("id");
        String name = row.getString("name");
        String content = row.getString("content");
        String category = row.getString("category_name");
        int priority = row.getInt("priority");
        LocalDate date = row.getObject("created_on", LocalDate.class);
        return switch (row.getString("note_type")) {
            case "TEXT" -> new TextNote(id, name, content, category, priority, date);
            case "REMINDER" -> new ReminderNote(id, name, content, category, priority,
                    row.getDouble("reminder_hours"), date);
            case "RECURRING" -> new RecurringReminderNote(id, name, content, category, priority,
                    row.getDouble("reminder_hours"), row.getDouble("repeat_hours"), date);
            default -> throw new SQLException("Unknown note type");
        };
    }

    @Override
    public List<Note> findAllSortedByNameAndByPriority() {
        return query(selectSql() + " ORDER BY e.name, e.priority, e.id", s -> { });
    }

    public List<Note> findByName(String name) {
        return query(selectSql() + " WHERE e.name = ? ORDER BY e.id", s -> s.setString(1, name));
    }
}
