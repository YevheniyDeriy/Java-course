package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.service.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** The same service contract, now backed by JPA; cleans up only its own demo records. */
public final class HibernateDemo {
    private HibernateDemo() { }

    public static void main(String[] args) {
        try (DBConnection database = DBConnection.getInstance(); JpaUtil jpa = JpaUtil.getInstance()) {
            HibernateNoteService hibernate = new HibernateNoteService(jpa);
            NoteService notes = hibernate;
            JdbcNoteService jdbc = new JdbcNoteService(database.getDataSource());
            HibernateCategoryService categories = new HibernateCategoryService(jpa);
            String categoryName = "ORM demo " + UUID.randomUUID();
            TextNote first = new TextNote("План", "Збережено через Hibernate", categoryName, 5);
            ReminderNote second = new ReminderNote("Захист", "Показати JPQL", categoryName, 2, 24);
            RecurringReminderNote third = new RecurringReminderNote(
                    com.eugene.notesapp.model.BaseEntity.nextId(), "План", "Повторити JPA", categoryName,
                    1, 2, 12, LocalDate.of(2024, 2, 29));
            try {
                notes.save(first);
                notes.save(second);
                notes.save(third);
                System.out.println("CREATE/READ: " + notes.findById(first.getId()).getContent());
                notes.save(new TextNote(first.getId(), "План", "Оновлено dirty checking", categoryName, 3));
                require(notes.findById(first.getId()).getContent().equals("Оновлено dirty checking"), "Update");
                System.out.println("UPDATE: " + jdbc.findById(first.getId()).getContent() + " (прочитано через JDBC)");
                for (Note note : notes.findAllSortedByNameAndByPriority()) {
                    if (note.getCategory().equals(categoryName)) {
                        System.out.printf("JPQL ORDER BY: %s, priority=%d%n", note.getName(), note.getPriority());
                    }
                }
                require(hibernate.findByCategory(categoryName).size() == 3, "Named category parameter");
                require(hibernate.findByName("' OR '1'='1' --").isEmpty(), "Injection is data");
                System.out.println("JPQL category: 3 records; pagination: " + hibernate.findPage(2, 0).size());
                require(notes.findById(third.getId()).getDate().equals(LocalDate.of(2024, 2, 29)), "Date");
                require(notes.delete(second.getId()) && !notes.delete(second.getId()), "Delete boolean contract");
                try { notes.findById(second.getId()); throw new AssertionError("Expected missing id"); }
                catch (EntityNotFoundException expected) { System.out.println("DELETE: EntityNotFoundException id=" + expected.getId()); }
            } finally {
                for (Note note : List.of(first, second, third)) notes.delete(note.getId());
                for (Category category : categories.findAll()) {
                    if (category.getName().equals(categoryName)) categories.delete(category.getId());
                }
            }
            System.out.println("PASS: Hibernate CRUD, JPQL, pagination, JDBC interoperability, unchanged init.sql.");
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
