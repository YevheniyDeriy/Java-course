package com.eugene.notesapp.service;

import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.Note;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.NoteMapper;
import com.eugene.notesapp.orm.entity.CategoryRecord;
import com.eugene.notesapp.orm.entity.NoteRecord;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class HibernateNoteService implements NoteService {
    private final JpaUtil jpa;

    public HibernateNoteService() { this(JpaUtil.getInstance()); }
    public HibernateNoteService(JpaUtil jpa) { this.jpa = Objects.requireNonNull(jpa, "jpa"); }

    @Override
    public Note findById(long id) {
        return jpa.read(manager -> {
            NoteRecord record = manager.find(NoteRecord.class, id);
            if (record == null) throw new EntityNotFoundException(id);
            return NoteMapper.toDomain(record);
        });
    }

    @Override
    public void save(Note note) {
        Objects.requireNonNull(note, "note");
        jpa.inTransaction(manager -> {
            NoteRecord record = manager.find(NoteRecord.class, note.getId());
            boolean isNew = record == null;
            if (isNew) record = new NoteRecord(note.getId(), note.getName());
            NoteMapper.copyToRecord(note, record);
            record.setCategory(findOrCreateCategory(manager, note.getCategory()));
            if (isNew) manager.persist(record);
            // An existing record is managed: Hibernate dirty checking issues UPDATE on commit.
            return null;
        });
    }

    private static CategoryRecord findOrCreateCategory(EntityManager manager, String name) {
        List<CategoryRecord> found = manager.createQuery(
                        "select c from CategoryRecord c where c.name = :name", CategoryRecord.class)
                .setParameter("name", name).getResultList();
        if (!found.isEmpty()) return found.getFirst();
        CategoryRecord category = new CategoryRecord(name);
        manager.persist(category);
        return category;
    }

    @Override
    public boolean delete(long id) {
        return jpa.inTransaction(manager -> {
            NoteRecord record = manager.find(NoteRecord.class, id);
            if (record == null) return false; // Preserve the boolean delete contract of LR7/LR10.
            manager.remove(record);
            return true;
        });
    }

    @Override
    public List<Note> findAll() {
        return jpa.read(manager -> map(manager.createQuery(
                "select n from NoteRecord n join fetch n.category order by n.id", NoteRecord.class).getResultList()));
    }

    @Override
    public List<Note> findAllSortedByName() {
        return jpa.read(manager -> map(manager.createQuery(
                "select n from NoteRecord n join fetch n.category order by n.name, n.id", NoteRecord.class).getResultList()));
    }

    @Override
    public List<Note> findAllSortedByNameAndByPriority() {
        return jpa.read(manager -> map(manager.createQuery(
                "select n from NoteRecord n join fetch n.category order by n.name, n.priority, n.id",
                NoteRecord.class).getResultList()));
    }

    @Override
    public List<Note> findAllSorted(Comparator<? super Note> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        List<Note> result = new ArrayList<>(findAll());
        result.sort(comparator);
        return result;
    }

    public List<Note> findByCategory(String category) {
        return jpa.read(manager -> map(manager.createQuery(
                        "select n from NoteRecord n join fetch n.category c where c.name = :category order by n.name, n.id",
                        NoteRecord.class)
                .setParameter("category", category).getResultList()));
    }

    public List<Note> findByName(String name) {
        return jpa.read(manager -> map(manager.createQuery(
                        "select n from NoteRecord n join fetch n.category where n.name = :name order by n.id",
                        NoteRecord.class)
                .setParameter("name", name).getResultList()));
    }

    public List<Note> findPage(int limit, long offset) {
        if (limit < 1 || limit > 1000 || offset < 0 || offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("limit must be 1..1000; offset must be 0..Integer.MAX_VALUE");
        }
        return jpa.read(manager -> map(manager.createQuery(
                        "select n from NoteRecord n join fetch n.category order by n.name, n.id", NoteRecord.class)
                .setFirstResult((int) offset).setMaxResults(limit).getResultList()));
    }

    private static List<Note> map(List<NoteRecord> records) {
        return records.stream().map(NoteMapper::toDomain).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
