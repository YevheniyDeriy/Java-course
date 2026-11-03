package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.exception.DataAccessException;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.service.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class JdbcServiceTest {
    private HikariDataSource source;
    private JdbcNoteService notes;
    private JdbcCategoryService categories;

    private static HikariDataSource open(String url) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(4);
        return new HikariDataSource(config);
    }

    @BeforeEach
    void setUp() {
        source = open("jdbc:h2:mem:" + UUID.randomUUID());
        DBConnection.initialize(source);
        notes = new JdbcNoteService(source);
        categories = new JdbcCategoryService(source);
    }

    @AfterEach
    void tearDown() { source.close(); }

    @Test
    void crudKeepsOriginalInterfaceAndUpdatesSameId() {
        NoteService service = notes;
        TextNote note = new TextNote("План", "старий текст", "Навчання", 5);
        service.save(note);
        Note loaded = service.findById(note.getId());
        assertEquals("старий текст", loaded.getContent());
        assertEquals("Навчання", loaded.getCategory());
        service.save(new ReminderNote(note.getId(), "План 2", "новий текст", "Архів", 1, 4));
        assertEquals(1, service.findAll().size());
        assertInstanceOf(ReminderNote.class, service.findById(note.getId()));
        assertEquals("Архів", service.findById(note.getId()).getCategory());
        assertTrue(service.delete(note.getId()));
        assertFalse(service.delete(note.getId()));
        assertEquals(note.getId(), assertThrows(EntityNotFoundException.class,
                () -> service.findById(note.getId())).getId());
        assertThrows(NullPointerException.class, () -> service.save(null));
    }

    @Test
    void restoresAllSubtypesDatesAndUnicode() {
        LocalDate old = LocalDate.of(2024, 2, 29);
        TextNote text = new TextNote(10001, "Їжак", "Привіт світе", "Україна", 1, old);
        ReminderNote reminder = new ReminderNote(10002, "Зустріч", "", "Україна", 2, 1.5, old);
        RecurringReminderNote recurring = new RecurringReminderNote(10003, "Повтор", "три різні слова", "Україна", 3, 2.5, 12, old);
        notes.saveBatch(List.of(text, reminder, recurring));
        assertInstanceOf(TextNote.class, notes.findById(text.getId()));
        assertEquals(old, notes.findById(text.getId()).getDate());
        assertEquals("Привіт світе", notes.findById(text.getId()).getContent());
        assertEquals(1.5, notes.findById(reminder.getId()).getReminderInHours());
        RecurringReminderNote loaded = assertInstanceOf(RecurringReminderNote.class, notes.findById(recurring.getId()));
        assertEquals(12, loaded.getRepeatEveryHours());
        assertEquals(3, loaded.getWordCount());
        assertEquals(old, loaded.getDate());
    }

    @Test
    void sortingAndPaginationHaveDeterministicTieBreakers() {
        TextNote b5 = new TextNote("B", "", "test", 5);
        TextNote a = new TextNote("A", "", "test", 3);
        TextNote b1 = new TextNote("B", "", "test", 1);
        notes.saveBatch(List.of(b5, a, b1));
        assertEquals(List.of(a, b5, b1), notes.findAllSortedByName());
        assertEquals(List.of(a, b1, b5), notes.findAllSortedByNameAndByPriority());
        assertEquals(List.of(a, b5), notes.findPage(2, 0));
        assertEquals(List.of(b1), notes.findPage(2, 2));
        assertTrue(notes.findPage(2, 100).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1001, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1, -1));
        assertEquals(List.of(b5, a, b1), notes.findAllSorted(Comparator.comparingLong(BaseEntity::getId)));
        notes.findAll().clear();
        assertEquals(3, notes.findAll().size());
    }

    @Test
    void preparedStatementTreatsInjectionAsLiteralData() {
        String payload = "' OR '1'='1' --";
        notes.save(new TextNote("Звичайна", "", "test", 1));
        assertTrue(notes.findByName(payload).isEmpty());
        TextNote literal = new TextNote(payload, "", "test", 1);
        notes.save(literal);
        assertEquals(List.of(literal), notes.findByName(payload));
        assertEquals(2, notes.findAll().size());
    }

    @Test
    void initIsIdempotentAndKeepsData() {
        notes.save(new TextNote("Не видаляти", "", "test", 1));
        DBConnection.initialize(source);
        DBConnection.initialize(source);
        assertEquals(1, notes.findAll().size());
        assertEquals(1, categories.findAll().size());
    }

    @Test
    void restrictProtectsNotesAndCategoryCrudUsesSameTemplate() {
        Category category = new Category("study");
        categories.save(category);
        notes.save(new TextNote("A", "", category.getName(), 1));
        assertThrows(DataAccessException.class, () -> categories.delete(category.getId()));
        categories.save(new Category(category.getId(), "renamed"));
        assertEquals("renamed", notes.findAll().getFirst().getCategory());
        assertEquals(1, categories.findAll().size());
        notes.delete(notes.findAll().getFirst().getId());
        assertTrue(categories.delete(category.getId()));
        assertFalse(categories.delete(category.getId()));
    }

    @Test
    void mergeCommitsChangesToBothTables() {
        Category from = new Category("from");
        Category to = new Category("to");
        categories.saveBatch(List.of(from, to));
        TextNote note = new TextNote("A", "", from.getName(), 1);
        notes.save(note);
        categories.mergeCategories(from.getId(), to.getId(), "merged");
        assertEquals("merged", notes.findById(note.getId()).getCategory());
        assertEquals("merged", categories.findById(to.getId()).getName());
        assertThrows(EntityNotFoundException.class, () -> categories.findById(from.getId()));
    }

    @Test
    void lateFailureRollsBackUpdatesAndDeletionAcrossBothTables() {
        Category from = new Category("from");
        Category to = new Category("to");
        Category occupied = new Category("occupied");
        categories.saveBatch(List.of(from, to, occupied));
        TextNote note = new TextNote("A", "", from.getName(), 1);
        notes.save(note);
        assertThrows(DataAccessException.class,
                () -> categories.mergeCategories(from.getId(), to.getId(), occupied.getName()));
        assertEquals("from", notes.findById(note.getId()).getCategory());
        assertEquals("from", categories.findById(from.getId()).getName());
        assertEquals("to", categories.findById(to.getId()).getName());
        assertEquals(3, categories.findAll().size());
        assertThrows(IllegalArgumentException.class, () -> categories.mergeCategories(from.getId(), from.getId(), "x"));
        assertThrows(EntityNotFoundException.class, () -> categories.mergeCategories(from.getId(), 999999, "x"));
    }

    @Test
    void invalidNoteRollsBackItsAutomaticallyCreatedCategory() {
        assertThrows(DataAccessException.class,
                () -> notes.save(new TextNote("Invalid", "", "new category", 6)));
        assertTrue(notes.findAll().isEmpty());
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void batchFailureRollsBackAlreadyExecutedChunks() {
        List<Category> batch = new java.util.ArrayList<>();
        for (int i = 0; i < 501; i++) batch.add(new Category("name " + i));
        batch.add(new Category("name 0")); // UNIQUE violation after the first 500 executeBatch entries.
        assertThrows(DataAccessException.class, () -> categories.saveBatch(batch));
        assertTrue(categories.findAll().isEmpty());
        categories.saveBatch(List.of());
        assertThrows(NullPointerException.class, () -> categories.saveBatch(java.util.Arrays.asList(batch.getFirst(), null)));
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void databaseEnforcesForeignKeyAndTypeConstraints() throws Exception {
        Category category = new Category("test");
        categories.save(category);
        TextNote text = new TextNote("A", "", category.getName(), 1);
        notes.save(text);
        try (Connection connection = source.getConnection();
             PreparedStatement invalidFk = connection.prepareStatement("UPDATE notes SET category_id = ? WHERE id = ?");
             PreparedStatement invalidType = connection.prepareStatement("UPDATE notes SET repeat_hours = ? WHERE id = ?")) {
            invalidFk.setLong(1, -1);
            invalidFk.setLong(2, text.getId());
            assertThrows(java.sql.SQLException.class, invalidFk::executeUpdate);
            invalidType.setDouble(1, 2);
            invalidType.setLong(2, text.getId());
            assertThrows(java.sql.SQLException.class, invalidType::executeUpdate);
        }
    }

    @Test
    void fileDatabaseSurvivesPoolRestart(@TempDir Path directory) {
        String url = "jdbc:h2:file:" + directory.resolve("notes").toString().replace('\\', '/');
        TextNote note = new TextNote("Persistent", "content", "test", 1);
        try (HikariDataSource first = open(url)) {
            DBConnection.initialize(first);
            new JdbcNoteService(first).save(note);
        }
        try (HikariDataSource second = open(url)) {
            DBConnection.initialize(second);
            assertEquals("content", new JdbcNoteService(second).findById(note.getId()).getContent());
        }
    }
}
