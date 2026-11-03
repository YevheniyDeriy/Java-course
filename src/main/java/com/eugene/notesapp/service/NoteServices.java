package com.eugene.notesapp.service;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.orm.JpaUtil;

/** Select storage once at the composition root; all menu operations use NoteService. */
public final class NoteServices {
    private NoteServices() { }

    public record Selection(NoteService service, Runnable cleanup) implements AutoCloseable {
        @Override
        public void close() { cleanup.run(); }
    }

    public static Selection open(String backend) {
        return switch (backend) {
            case "memory" -> new Selection(new InMemoryNoteService(), () -> { });
            case "jdbc" -> new Selection(new JdbcNoteService(), () -> DBConnection.getInstance().close());
            case "hibernate" -> {
                try {
                    JpaUtil jpa = JpaUtil.getInstance();
                    yield new Selection(new HibernateNoteService(jpa), () -> {
                        try { jpa.close(); }
                        finally { DBConnection.getInstance().close(); }
                    });
                } catch (RuntimeException | Error failure) {
                    DBConnection.getInstance().close();
                    throw failure;
                }
            }
            default -> throw new IllegalArgumentException("notes.storage must be memory, jdbc, or hibernate");
        };
    }
}
