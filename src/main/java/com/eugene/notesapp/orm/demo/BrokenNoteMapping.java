package com.eugene.notesapp.orm.demo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Intentional bad @Column, excluded from notes-pu. Used only by SchemaValidationDemo/tests. */
@Entity
@Table(name = "notes")
public class BrokenNoteMapping {
    @Id
    private Long id;

    @Column(name = "name_typo", nullable = false, length = 255)
    private String name;

    protected BrokenNoteMapping() { }
}
