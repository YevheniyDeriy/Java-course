package com.eugene.notesapp;

import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.service.*;
import java.util.*;

public final class CrudServiceTest {
    @org.junit.jupiter.api.Test
    void previousLaboratoryContract() { main(new String[0]); }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(Class<? extends Throwable> type, Runnable action) {
        try { action.run(); }
        catch (Throwable e) {
            if (type.isInstance(e)) return;
            throw new AssertionError("Unexpected exception", e);
        }
        throw new AssertionError("Expected " + type.getName());
    }
    public static void main(String[] args) {
        NoteService service = new InMemoryNoteService();
        TextNote first = new TextNote(201, "B", "old", "test", 5);
        ReminderNote second = new ReminderNote(202, "A", "test", "test", 2, 4);
        TextNote third = new TextNote(203, "B", "test", "test", 1);
        service.save(first); service.save(second); service.save(third);
        check(service.findById(201) == first, "findById");
        List<Note> before = service.findAll();
        check(service.findAllSortedByName().equals(List.of(second, first, third)), "name order");
        List<Note> sorted = service.findAllSortedByNameAndByPriority();
        check(sorted.equals(List.of(second, third, first)), "secondary order");
        Comparator<BaseEntity> byId = Comparator.comparingLong(BaseEntity::getId);
        check(service.findAllSorted(byId).equals(before), "super comparator");
        sorted.clear();
        check(service.findAll().equals(before), "sorting copy and storage order");
        before.clear();
        check(service.findAll().size() == 3, "findAll defensive copy");
        TextNote updated = new TextNote(201, "Changed", "new", "test", 3);
        service.save(updated);
        check(service.findAll().size() == 3 && service.findById(201) == updated, "upsert");
        check(service.findAll().get(0) == updated, "update preserves position");
        check(first.equals(updated) && first.hashCode() == updated.hashCode(), "identity equality");
        ReminderNote sameId = new ReminderNote(201, "other", "", "test", 1, 2);
        check(first.equals(sameId) && sameId.equals(first), "cross-type identity symmetry");
        check(!first.equals(third) && !first.equals(null), "different identity");
        check(service.delete(202) && !service.delete(202), "delete result");
        try { service.findById(202); throw new AssertionError("missing id"); }
        catch (EntityNotFoundException e) {
            check(e.getId() == 202 && e.getMessage().contains("202"), "exception details");
        }
        expect(NullPointerException.class, () -> service.save(null));
        expect(NullPointerException.class, () -> service.findAllSorted(null));
        expect(NullPointerException.class, () -> new TextNote(null, "", "test", 1));
        CRUDService<TextNote> texts = new InMemoryCrudService<>();
        CRUDService<ReminderNote> reminders = new InMemoryCrudService<>();
        texts.save(third); reminders.save(second);
        check(texts.findById(203) == third && reminders.findById(202) == second, "two generic types");
        check(new TextNote("Auto", "", "test", 1).getId() > 203, "generated id");
        check(new InMemoryCrudService<TextNote>().findAllSortedByName().isEmpty(), "empty sort");
        System.out.println("PASS: CRUD, sorting, copies, equality, exceptions, generic types");
    }
}
