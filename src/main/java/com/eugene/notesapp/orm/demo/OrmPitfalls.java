package com.eugene.notesapp.orm.demo;

import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.entity.CategoryRecord;
import com.eugene.notesapp.orm.entity.NoteRecord;
import com.eugene.notesapp.orm.entity.NoteType;
import org.hibernate.LazyInitializationException;
import java.time.LocalDate;

/** Deliberately reproduces two ORM pitfalls in an isolated demonstration. */
public final class OrmPitfalls {
    private OrmPitfalls() { }

    public record Result(boolean lazyFailureObserved, int noteCount, long before, long after) { }

    public static Result demonstrate(JpaUtil jpa, String prefix) {
        jpa.inTransaction(manager -> {
            for (int i = 0; i < 3; i++) {
                CategoryRecord category = new CategoryRecord(prefix + i);
                for (int n = 0; n < 2; n++) {
                    NoteRecord note = new NoteRecord("Note " + i + ":" + n);
                    note.setDetails("Two words", LocalDate.now(), 1, NoteType.TEXT, 0, 0);
                    category.addNote(note);
                }
                manager.persist(category); // Cascade PERSIST and generated ids.
            }
            return null;
        });

        CategoryRecord detached = jpa.read(manager -> manager.createQuery(
                        "select c from CategoryRecord c where c.name = :name", CategoryRecord.class)
                .setParameter("name", prefix + 0).getSingleResult());
        boolean lazyFailure = false;
        try { detached.getNotes().size(); }
        catch (LazyInitializationException expected) {
            lazyFailure = true;
            System.out.println("LazyInitializationException reproduced after EntityManager.close().");
        }
        if (!lazyFailure) throw new AssertionError("Expected LazyInitializationException");

        CategoryRecord fetched = jpa.read(manager -> manager.createQuery(
                        "select c from CategoryRecord c left join fetch c.notes where c.name = :name",
                        CategoryRecord.class)
                .setParameter("name", prefix + 0).getSingleResult());
        if (fetched.getNotes().size() != 2) throw new AssertionError("JOIN FETCH did not initialize notes");
        System.out.println("Lazy fix: JOIN FETCH; detached category exposes 2 loaded notes.");

        jpa.statistics().clear();
        int notesBefore = jpa.read(manager -> manager.createQuery(
                        "select c from CategoryRecord c where c.name like :prefix order by c.id", CategoryRecord.class)
                .setParameter("prefix", prefix + "%").getResultList().stream()
                .mapToInt(category -> category.getNotes().size()).sum());
        long before = jpa.statistics().getPrepareStatementCount();

        jpa.statistics().clear();
        int notesAfter = jpa.read(manager -> manager.createQuery(
                        "select distinct c from CategoryRecord c left join fetch c.notes where c.name like :prefix order by c.id",
                        CategoryRecord.class)
                .setParameter("prefix", prefix + "%").getResultList().stream()
                .mapToInt(category -> category.getNotes().size()).sum());
        long after = jpa.statistics().getPrepareStatementCount();
        if (notesBefore != 6 || notesAfter != 6 || before != 4 || after != 1) {
            throw new AssertionError("Unexpected N+1 measurement: " + before + " -> " + after);
        }
        System.out.println("N+1: 3 categories, 6 notes; SQL statements: " + before + " -> " + after);
        return new Result(lazyFailure, notesAfter, before, after);
    }
}
