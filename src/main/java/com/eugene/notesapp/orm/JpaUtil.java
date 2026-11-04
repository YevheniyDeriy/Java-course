package com.eugene.notesapp.orm;

import com.eugene.notesapp.db.DBConnection;
import jakarta.persistence.*;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** One production factory, one short-lived EntityManager per operation. */
public final class JpaUtil implements AutoCloseable {
    private final EntityManagerFactory factory;

    private JpaUtil(DataSource source) {
        factory = Persistence.createEntityManagerFactory("notes-pu", Map.of(
                "jakarta.persistence.nonJtaDataSource", Objects.requireNonNull(source, "source")));
    }

    private static final class Holder {
        private static final JpaUtil INSTANCE = new JpaUtil(DBConnection.getInstance().getDataSource());
    }

    public static JpaUtil getInstance() { return Holder.INSTANCE; }

    /** Scoped factory for isolated tests/demos; caller owns both this factory and its DataSource. */
    public static JpaUtil open(DataSource source) { return new JpaUtil(source); }

    public <T> T inTransaction(Function<EntityManager, T> work) {
        Objects.requireNonNull(work, "work");
        try (EntityManager manager = factory.createEntityManager()) {
            EntityTransaction transaction = manager.getTransaction();
            transaction.begin();
            try {
                T result = work.apply(manager);
                transaction.commit();
                return result;
            } catch (RuntimeException | Error failure) {
                try { if (transaction.isActive()) transaction.rollback(); }
                catch (RuntimeException rollbackFailure) { failure.addSuppressed(rollbackFailure); }
                throw failure;
            }
        }
    }

    public <T> T read(Function<EntityManager, T> work) {
        return inTransaction(work);
    }

    public Statistics statistics() { return factory.unwrap(SessionFactory.class).getStatistics(); }
    public boolean isOpen() { return factory.isOpen(); }
    @Override
    public void close() { factory.close(); }
}
