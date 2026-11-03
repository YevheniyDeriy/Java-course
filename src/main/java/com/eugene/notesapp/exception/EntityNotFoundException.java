package com.eugene.notesapp.exception;

public class EntityNotFoundException extends RuntimeException {
    private final long id;

    public EntityNotFoundException(long id) {
        super("Сутність з id=" + id + " не знайдено.");
        this.id = id;
    }

    public long getId() { return id; }
}
