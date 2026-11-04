package com.eugene.notesapp.orm.entity;

import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;
import java.util.Objects;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue
    @CourseGeneratedId
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    protected BaseEntity() { }
    protected BaseEntity(String name) { this.name = Objects.requireNonNull(name, "name"); }
    protected BaseEntity(long id, String name) {
        this(name);
        if (id <= 0 || id == Long.MAX_VALUE) throw new IllegalArgumentException("Invalid id");
        this.id = id;
        com.eugene.notesapp.model.BaseEntity.reserveIdsThrough(id);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Objects.requireNonNull(name, "name"); }

    private static Class<?> entityClass(Object entity) {
        return entity instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getPersistentClass() : entity.getClass();
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || entityClass(this) != entityClass(other)) return false;
        BaseEntity entity = (BaseEntity) other;
        return getId() != null && getId().equals(entity.getId());
    }

    @Override
    public final int hashCode() { return entityClass(this).hashCode(); }
}
