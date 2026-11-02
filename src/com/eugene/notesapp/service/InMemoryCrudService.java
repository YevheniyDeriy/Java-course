package com.eugene.notesapp.service;

import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.BaseEntity;
import java.util.*;

public class InMemoryCrudService<T extends BaseEntity> implements CRUDService<T> {
    private final Map<Long, T> storage = new LinkedHashMap<>();

    @Override
    public T findById(long id) {
        T entity = storage.get(id);
        if (entity == null) throw new EntityNotFoundException(id);
        return entity;
    }

    @Override
    public void save(T entity) {
        Objects.requireNonNull(entity, "entity");
        storage.put(entity.getId(), entity);
    }

    @Override
    public boolean delete(long id) { return storage.remove(id) != null; }

    @Override
    public List<T> findAll() { return new ArrayList<>(storage.values()); }

    @Override
    public List<T> findAllSorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator");
        List<T> result = findAll();
        result.sort(comparator);
        return result;
    }
}
