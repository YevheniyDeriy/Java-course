package com.eugene.notesapp;

import com.eugene.notesapp.db.DBConnection;
import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.NoteMapper;
import com.eugene.notesapp.orm.demo.OrmPitfalls;
import com.eugene.notesapp.orm.entity.CategoryRecord;
import com.eugene.notesapp.orm.entity.NoteRecord;
import com.eugene.notesapp.orm.entity.NoteType;
import com.eugene.notesapp.service.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class HibernateServiceTest {
    private HikariDataSource source;
    private JpaUtil jpa;
    private HibernateNoteService notes;
    private HibernateCategoryService categories;

    private static HikariDataSource open(String url) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(3);
        return new HikariDataSource(config);
    }

    @BeforeEach
    void setUp() {
        source = open("jdbc:h2:mem:orm-test-" + UUID.randomUUID());
        DBConnection.initialize(source);
        jpa = JpaUtil.open(source);
        notes = new HibernateNoteService(jpa);
        categories = new HibernateCategoryService(jpa);
    }

    @AfterEach
    void tearDown() {
        if (jpa != null) jpa.close();
        if (source != null) source.close();
    }

    @Test
    void crudKeepsOriginalInterfaceAndMissingIdContract() {
        NoteService service = notes;
        TextNote first = new TextNote("Перша", "old", "test", 5);
        service.save(first);
        assertEquals(first.getId(), service.findById(first.getId()).getId());
        service.save(new ReminderNote(first.getId(), "Оновлена", "new", "other", 1, 3));
        assertEquals(1, service.findAll().size());
        Note loaded = service.findById(first.getId());
        assertInstanceOf(ReminderNote.class, loaded);
        assertEquals("new", loaded.getContent());
        assertEquals("other", loaded.getCategory());
        assertTrue(service.delete(first.getId()));
        assertFalse(service.delete(first.getId()));
        assertEquals(first.getId(), assertThrows(EntityNotFoundException.class,
                () -> service.findById(first.getId())).getId());
        assertThrows(NullPointerException.class, () -> service.save(null));
    }

    @Test
    void jdbcAndHibernateReadAndUpdateTheSameRows() {
        JdbcNoteService jdbc = new JdbcNoteService(source);
        TextNote original = new TextNote("From JDBC", "JDBC text", "shared", 2);
        jdbc.save(original);
        assertEquals("JDBC text", notes.findById(original.getId()).getContent());
        notes.save(new TextNote(original.getId(), "From ORM", "Hibernate text", "shared", 3));
        assertEquals("Hibernate text", jdbc.findById(original.getId()).getContent());
        jdbc.save(new TextNote(original.getId(), "Updated JDBC", "again", "shared", 1));
        assertEquals("again", notes.findById(original.getId()).getContent());
        assertTrue(notes.delete(original.getId()));
        assertTrue(jdbc.findAll().isEmpty());
    }

    @Test
    void restoresSubtypesUnicodeAndOriginalDate() {
        LocalDate date = LocalDate.of(2024, 2, 29);
        TextNote text = new TextNote(BaseEntity.nextId(), "Їжак", "Привіт світе", "Україна", 1, date);
        ReminderNote reminder = new ReminderNote(BaseEntity.nextId(), "Зустріч", "", "Україна", 2, 1.5, date);
        RecurringReminderNote recurring = new RecurringReminderNote(BaseEntity.nextId(), "Повтор", "три різні слова", "Україна", 3, 2.5, 12, date);
        for (Note note : List.of(text, reminder, recurring)) notes.save(note);
        assertInstanceOf(TextNote.class, notes.findById(text.getId()));
        assertEquals("Привіт світе", notes.findById(text.getId()).getContent());
        assertEquals(date, notes.findById(text.getId()).getDate());
        assertEquals(1.5, notes.findById(reminder.getId()).getReminderInHours());
        RecurringReminderNote loaded = assertInstanceOf(RecurringReminderNote.class, notes.findById(recurring.getId()));
        assertEquals(12, loaded.getRepeatEveryHours());
        assertEquals(3, loaded.getWordCount());
        assertEquals(date, loaded.getDate());
    }

    @Test
    void jpqlSortingAndPagesAreDeterministic() {
        TextNote b5 = new TextNote("B", "", "test", 5);
        TextNote a = new TextNote("A", "", "test", 3);
        TextNote b1 = new TextNote("B", "", "test", 1);
        for (Note note : List.of(b5, a, b1)) notes.save(note);
        assertEquals(List.of(a, b5, b1), notes.findAllSortedByName());
        assertEquals(List.of(a, b1, b5), notes.findAllSortedByNameAndByPriority());
        assertEquals(List.of(a, b5), notes.findPage(2, 0));
        assertEquals(List.of(b1), notes.findPage(2, 2));
        assertTrue(notes.findPage(2, 99).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(0, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1001, 0));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1, -1));
        assertThrows(IllegalArgumentException.class, () -> notes.findPage(1, 1L + Integer.MAX_VALUE));
        Comparator<BaseEntity> byId = Comparator.comparingLong(BaseEntity::getId);
        assertEquals(List.of(b5, a, b1), notes.findAllSorted(byId));
        notes.findAllSortedByName().clear();
        assertEquals(3, notes.findAll().size());
    }

    @Test
    void namedParametersTreatPayloadAsData() {
        String payload = "' OR '1'='1' --";
        TextNote safe = new TextNote("Safe", "", "normal", 1);
        notes.save(safe);
        assertTrue(notes.findByCategory(payload).isEmpty());
        assertTrue(notes.findByName(payload).isEmpty());
        TextNote literal = new TextNote(payload, "", payload, 1);
        notes.save(literal);
        assertEquals(List.of(literal), notes.findByCategory(payload));
        assertEquals(List.of(literal), notes.findByName(payload));
        assertEquals(List.of(safe), notes.findByCategory("normal"));
    }

    @Test
    void generatedIdsCascadeAndHelpersMaintainBothSides() {
        CategoryRecord first = new CategoryRecord("first");
        CategoryRecord second = new CategoryRecord("second");
        NoteRecord note = record("note");
        assertNull(first.getId());
        first.addNote(note);
        assertSame(first, note.getCategory());
        first.removeNote(note);
        assertNull(note.getCategory());
        assertTrue(first.getNotes().isEmpty());
        first.addNote(note);
        second.addNote(note);
        assertTrue(first.getNotes().isEmpty());
        assertEquals(List.of(note), second.getNotes());
        jpa.inTransaction(manager -> { manager.persist(first); manager.persist(second); return null; });
        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotNull(note.getId());
        assertNotEquals(second.getId(), note.getId());
        assertEquals("second", notes.findById(note.getId()).getCategory());
        // Removing from a collection is not deletion: transfer before flush because FK is NOT NULL.
        jpa.inTransaction(manager -> {
            CategoryRecord managedFirst = manager.find(CategoryRecord.class, first.getId());
            NoteRecord managedNote = manager.find(NoteRecord.class, note.getId());
            managedNote.getCategory().removeNote(managedNote);
            managedFirst.addNote(managedNote);
            return null;
        });
        assertEquals("first", notes.findById(note.getId()).getCategory());
        assertEquals(1, notes.findAll().size());
    }

    @Test
    void categoryCrudRenamesWithoutDeletingChildrenAndHonorsRestrict() {
        Category category = new Category("category");
        categories.save(category);
        TextNote note = new TextNote("note", "", category.getName(), 1);
        notes.save(note);
        assertThrows(RuntimeException.class, () -> categories.delete(category.getId()));
        assertEquals("category", categories.findById(category.getId()).getName());
        categories.save(new Category(category.getId(), "renamed"));
        assertEquals("renamed", notes.findById(note.getId()).getCategory());
        assertEquals(1, categories.findAllWithNotes().getFirst().getNotes().size());
        notes.delete(note.getId());
        assertTrue(categories.delete(category.getId()));
        assertFalse(categories.delete(category.getId()));
        assertThrows(EntityNotFoundException.class, () -> categories.findById(category.getId()));
    }

    @Test
    void equalityIsStableAcrossGeneratedIdAndDetachedProxy() {
        CategoryRecord first = new CategoryRecord("first");
        CategoryRecord second = new CategoryRecord("second");
        assertNotEquals(first, second);
        Set<CategoryRecord> set = new HashSet<>();
        set.add(first);
        int originalHash = first.hashCode();
        jpa.inTransaction(manager -> { manager.persist(first); return null; });
        assertEquals(originalHash, first.hashCode());
        assertTrue(set.contains(first));
        CategoryRecord proxy = jpa.read(manager -> manager.getReference(CategoryRecord.class, first.getId()));
        assertFalse(Hibernate.isInitialized(proxy));
        assertEquals(first, proxy);
        assertEquals(proxy, first);
        assertEquals(first.hashCode(), proxy.hashCode());
        assertFalse(Hibernate.isInitialized(proxy));
    }

    @Test
    void failedSaveRollsBackNewCategoryAndNote() {
        assertThrows(RuntimeException.class,
                () -> notes.save(new TextNote("invalid", "", "new category", 6)));
        assertTrue(notes.findAll().isEmpty());
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void managerIsClosedAfterReadWriteAndException() {
        AtomicReference<EntityManager> first = new AtomicReference<>();
        jpa.read(manager -> { first.set(manager); return null; });
        assertFalse(first.get().isOpen());
        AtomicReference<EntityManager> second = new AtomicReference<>();
        jpa.inTransaction(manager -> { second.set(manager); return null; });
        assertNotSame(first.get(), second.get());
        assertFalse(second.get().isOpen());
        AtomicReference<EntityManager> failed = new AtomicReference<>();
        assertThrows(IllegalStateException.class, () -> jpa.inTransaction(manager -> {
            failed.set(manager);
            manager.persist(new CategoryRecord("rolled back"));
            manager.flush();
            throw new IllegalStateException("deliberate rollback");
        }));
        assertFalse(failed.get().isOpen());
        assertTrue(categories.findAll().isEmpty());
    }

    @Test
    void detachedChangesNeedMergeAndMergeReturnsManagedCopy() {
        CategoryRecord detached = jpa.inTransaction(manager -> {
            CategoryRecord created = new CategoryRecord("original");
            manager.persist(created);
            assertTrue(manager.contains(created));
            return created;
        });
        detached.setName("detached change");
        assertEquals("original", categories.findById(detached.getId()).getName());
        jpa.inTransaction(manager -> {
            CategoryRecord managed = manager.merge(detached);
            assertNotSame(detached, managed);
            assertTrue(manager.contains(managed));
            assertFalse(manager.contains(detached));
            return null;
        });
        assertEquals("detached change", categories.findById(detached.getId()).getName());
    }

    @Test
    void reproducesLazyFailureAndMeasuresFourQueriesVersusOne() {
        OrmPitfalls.Result result = OrmPitfalls.demonstrate(jpa, "pitfall-");
        assertTrue(result.lazyFailureObserved());
        assertEquals(6, result.noteCount());
        assertEquals(4, result.before());
        assertEquals(1, result.after());
    }

    @Test
    void validateDetectsChangedColumnWithoutChangingSchema() {
        assertTrue(SchemaValidationDemo.demonstrateMismatch(source).contains("name_typo"));
        TextNote note = new TextNote("Still works", "", "test", 1);
        notes.save(note);
        DBConnection.initialize(source); // Idempotent LR10 script remains compatible.
        assertEquals("Still works", notes.findById(note.getId()).getName());
    }

    @Test
    void unchangedFileSchemaWorksAcrossOrmAndJdbcRestarts(@TempDir Path directory) {
        String url = "jdbc:h2:file:" + directory.resolve("shared").toString().replace('\\', '/');
        TextNote note = new TextNote("Persistent", "from JDBC", "shared", 2);
        try (HikariDataSource initial = open(url)) {
            DBConnection.initialize(initial);
            new JdbcNoteService(initial).save(note);
        }
        try (HikariDataSource reopened = open(url); JpaUtil orm = JpaUtil.open(reopened)) {
            HibernateNoteService service = new HibernateNoteService(orm);
            assertEquals("from JDBC", service.findById(note.getId()).getContent());
            service.save(new TextNote(note.getId(), "Persistent", "from Hibernate", "shared", 1));
        }
        try (HikariDataSource last = open(url)) {
            assertEquals("from Hibernate", new JdbcNoteService(last).findById(note.getId()).getContent());
        }
    }

    private static NoteRecord record(String name) {
        NoteRecord record = new NoteRecord(name);
        record.setDetails("Two words", LocalDate.now(), 1, NoteType.TEXT, 0, 0);
        return record;
    }
}
