package com.eugene.notesapp.service;

import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.Category;
import com.eugene.notesapp.orm.JpaUtil;
import com.eugene.notesapp.orm.entity.CategoryRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class HibernateCategoryService implements CRUDService<Category> {
    private final JpaUtil jpa;

    public HibernateCategoryService() { this(JpaUtil.getInstance()); }
    public HibernateCategoryService(JpaUtil jpa) { this.jpa = Objects.requireNonNull(jpa, "jpa"); }

    @Override
    public Category findById(long id) {
        return jpa.read(manager -> {
            CategoryRecord record = manager.find(CategoryRecord.class, id);
            if (record == null) throw new EntityNotFoundException(id);
            return toDomain(record);
        });
    }

    @Override
    public void save(Category category) {
        Objects.requireNonNull(category, "category");
        jpa.inTransaction(manager -> {
            CategoryRecord record = manager.find(CategoryRecord.class, category.getId());
            if (record == null) manager.persist(new CategoryRecord(category.getId(), category.getName()));
            else record.setName(category.getName());
            return null;
        });
    }

    @Override
    public boolean delete(long id) {
        return jpa.inTransaction(manager -> {
            CategoryRecord record = manager.find(CategoryRecord.class, id);
            if (record == null) return false;
            manager.remove(record); // No cascade REMOVE: FK RESTRICT protects children.
            return true;
        });
    }

    @Override
    public List<Category> findAll() {
        return jpa.read(manager -> map(manager.createQuery(
                "select c from CategoryRecord c order by c.id", CategoryRecord.class).getResultList()));
    }

    @Override
    public List<Category> findAllSortedByName() {
        return jpa.read(manager -> map(manager.createQuery(
                "select c from CategoryRecord c order by c.name, c.id", CategoryRecord.class).getResultList()));
    }

    @Override
    public List<Category> findAllSorted(Comparator<? super Category> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        List<Category> result = findAll();
        result.sort(comparator);
        return result;
    }

    public List<CategoryRecord> findAllWithNotes() {
        return jpa.read(manager -> manager.createQuery(
                "select distinct c from CategoryRecord c left join fetch c.notes order by c.id",
                CategoryRecord.class).getResultList());
    }

    private static Category toDomain(CategoryRecord record) { return new Category(record.getId(), record.getName()); }
    private static List<Category> map(List<CategoryRecord> records) {
        return records.stream().map(HibernateCategoryService::toDomain)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
    }
}
