package com.eugene.notesapp.orm.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity(name = "CategoryRecord")
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class CategoryRecord extends BaseEntity {
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    private List<NoteRecord> notes = new ArrayList<>();

    protected CategoryRecord() { }
    public CategoryRecord(String name) { super(name); }
    public CategoryRecord(long id, String name) { super(id, name); }

    public List<NoteRecord> getNotes() { return Collections.unmodifiableList(notes); }
    public void addNote(NoteRecord note) { Objects.requireNonNull(note, "note").setCategory(this); }
    public void removeNote(NoteRecord note) {
        if (note.getCategory() == this) note.setCategory(null);
    }

    void attach(NoteRecord note) { if (!notes.contains(note)) notes.add(note); }
    void detach(NoteRecord note) { notes.remove(note); }

    @Override
    public String toString() { return "CategoryRecord[id=" + getId() + ", name=" + getName() + "]"; }
}
