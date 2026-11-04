package com.eugene.notesapp.orm;

import com.eugene.notesapp.model.*;
import com.eugene.notesapp.orm.entity.NoteRecord;
import com.eugene.notesapp.orm.entity.NoteType;

/** Immutable LR7 domain values stay independent of Hibernate's managed lifecycle. */
public final class NoteMapper {
    private NoteMapper() { }

    public static void copyToRecord(Note note, NoteRecord record) {
        NoteType type = switch (note) {
            case RecurringReminderNote ignored -> NoteType.RECURRING;
            case ReminderNote ignored -> NoteType.REMINDER;
            case TextNote ignored -> NoteType.TEXT;
        };
        record.setName(note.getName());
        record.setDetails(note.getContent(), note.getDate(), note.getPriority(), type,
                note.getReminderInHours(), note instanceof RecurringReminderNote recurring
                        ? recurring.getRepeatEveryHours() : 0);
    }

    public static Note toDomain(NoteRecord record) {
        long id = record.getId();
        String category = record.getCategory().getName();
        return switch (record.getType()) {
            case TEXT -> new TextNote(id, record.getName(), record.getContent(), category,
                    record.getPriority(), record.getDate());
            case REMINDER -> new ReminderNote(id, record.getName(), record.getContent(), category,
                    record.getPriority(), record.getReminderHours(), record.getDate());
            case RECURRING -> new RecurringReminderNote(id, record.getName(), record.getContent(), category,
                    record.getPriority(), record.getReminderHours(), record.getRepeatHours(), record.getDate());
        };
    }
}
