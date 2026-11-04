package com.eugene.notesapp.orm.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity(name = "NoteRecord")
@Table(name = "notes")
public class NoteRecord extends BaseEntity {
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "created_on", nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notes_category"))
    private CategoryRecord category;

    @Column(name = "priority", nullable = false)
    private int priority;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "note_type", nullable = false, length = 16)
    private NoteType type;

    @Column(name = "reminder_hours", nullable = false)
    private double reminderHours;

    @Column(name = "repeat_hours", nullable = false)
    private double repeatHours;

    protected NoteRecord() { }
    public NoteRecord(String name) { super(name); }
    public NoteRecord(long id, String name) { super(id, name); }

    public String getContent() { return content; }
    public LocalDate getDate() { return date; }
    public CategoryRecord getCategory() { return category; }
    public int getPriority() { return priority; }
    public NoteType getType() { return type; }
    public double getReminderHours() { return reminderHours; }
    public double getRepeatHours() { return repeatHours; }

    public void setCategory(CategoryRecord category) {
        if (this.category == category) return;
        CategoryRecord previous = this.category;
        this.category = category;
        if (previous != null) previous.detach(this);
        if (category != null) category.attach(this);
    }

    public void setDetails(String content, LocalDate date, int priority, NoteType type,
                           double reminderHours, double repeatHours) {
        this.content = content;
        this.date = date;
        this.priority = priority;
        this.type = type;
        this.reminderHours = reminderHours;
        this.repeatHours = repeatHours;
    }

    @Transient
    public int getWordCount() {
        return content == null || content.isBlank() ? 0 : content.trim().split("\\s+").length;
    }

    @Override
    public String toString() { return "NoteRecord[id=" + getId() + ", name=" + getName() + "]"; }
}
