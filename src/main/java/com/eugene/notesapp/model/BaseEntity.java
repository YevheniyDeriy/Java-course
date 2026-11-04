package com.eugene.notesapp.model;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseEntity {
    private static final AtomicLong NEXT_ID = new AtomicLong(1);
    private final long id;
    private final String name;

    protected BaseEntity(String name) { this(nextId(), name); }

    /** Shared by the domain constructors and the schema-compatible ORM generator. */
    public static long nextId() {
        return NEXT_ID.getAndUpdate(value -> {
            if (value <= 0 || value == Long.MAX_VALUE) throw new IllegalStateException("Id limit reached");
            return value + 1;
        });
    }

    protected BaseEntity(long id, String name) {
        if (id <= 0 || id == Long.MAX_VALUE) throw new IllegalArgumentException("Invalid id: " + id);
        this.name = Objects.requireNonNull(name, "name");
        this.id = id;
        NEXT_ID.accumulateAndGet(id + 1, Math::max);
    }

    public final long getId() { return id; }
    public final String getName() { return name; }

    /** Called once at database startup, before clients construct new entities. */
    public static void reserveIdsThrough(long id) {
        if (id < 0 || id >= Long.MAX_VALUE - 1) throw new IllegalArgumentException("Id limit reached");
        NEXT_ID.accumulateAndGet(id + 1, Math::max);
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || obj instanceof BaseEntity other && id == other.id;
    }

    @Override
    public final int hashCode() { return Long.hashCode(id); }
}
