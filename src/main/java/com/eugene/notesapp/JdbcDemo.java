package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.exception.DataAccessException;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.service.*;
import java.util.List;
import java.util.UUID;

/** Noninteractive demo; uses the file database and cleans up only its own records. */
public final class JdbcDemo {
    private JdbcDemo() { }

    public static void main(String[] args) {
        try (DBConnection database = DBConnection.getInstance()) {
            JdbcNoteService jdbc = new JdbcNoteService(database.getDataSource());
            NoteService notes = jdbc; // The unchanged LR7 interface.
            JdbcCategoryService categories = new JdbcCategoryService(database.getDataSource());
            String suffix = UUID.randomUUID().toString();
            Category source = new Category("Навчання " + suffix);
            Category target = new Category("Архів " + suffix);
            Category occupied = new Category("Зайнята назва " + suffix);
            TextNote first = new TextNote("План", "Вивчити JDBC", source.getName(), 5);
            ReminderNote second = new ReminderNote("Захист", "Показати транзакцію", source.getName(), 2, 24);
            TextNote third = new TextNote("План", "Повторити SQL", source.getName(), 1);
            try {
                categories.saveBatch(List.of(source, target, occupied));
                jdbc.saveBatch(List.of(first, second, third));
                System.out.println("CRUD і ORDER BY name, priority (1 перед 5):");
                for (Note note : notes.findAllSortedByNameAndByPriority()) {
                    if (note.getCategory().equals(source.getName())) {
                        System.out.printf("id=%d, %s, priority=%d%n", note.getId(), note.getName(), note.getPriority());
                    }
                }
                notes.save(new TextNote(first.getId(), first.getName(), "Оновлено через save", source.getName(), 3));
                System.out.println("Оновлення: " + notes.findById(first.getId()).getContent());
                System.out.println("Пагінація: " + jdbc.findPage(2, 0).size() + " записів на першій сторінці");
                String attack = "' OR '1'='1' --";
                System.out.println("PreparedStatement, спроба SQL-ін'єкції: " + jdbc.findByName(attack).size() + " збігів");

                try {
                    categories.mergeCategories(source.getId(), target.getId(), occupied.getName());
                    throw new AssertionError("Expected UNIQUE violation");
                } catch (DataAccessException expected) {
                    require(notes.findById(first.getId()).getCategory().equals(source.getName()), "Notes rollback");
                    categories.findById(source.getId());
                    require(categories.findById(target.getId()).getName().equals(target.getName()), "Category rollback");
                    System.out.println("ROLLBACK: конфлікт UNIQUE; перенесення нотаток і видалення категорії скасовано.");
                }
                categories.mergeCategories(source.getId(), target.getId(), "Об'єднано " + suffix);
                require(notes.findById(first.getId()).getCategory().equals("Об'єднано " + suffix), "Commit");
                System.out.println("COMMIT: нотатки перенесено; вихідну категорію видалено; цільову перейменовано.");
                System.out.println("Видалення: " + notes.delete(second.getId()) + "; повторне: " + notes.delete(second.getId()));
                try { notes.findById(second.getId()); }
                catch (EntityNotFoundException expected) { System.out.println("EntityNotFoundException: id=" + expected.getId()); }
            } finally {
                for (Note note : List.of(first, second, third)) notes.delete(note.getId());
                for (Category category : List.of(source, target, occupied)) categories.delete(category.getId());
            }
            System.out.println("PASS: JDBC demo; демонстраційні записи прибрано.");
        }
    }

    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
