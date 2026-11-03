package com.eugene.notesapp;

import com.eugene.notesapp.exception.EntityNotFoundException;
import com.eugene.notesapp.model.*;
import com.eugene.notesapp.service.*;
import static com.eugene.notesapp.util.EntityPrinter.printAll;

public final class CrudDemo {
    public static void main(String[] args) {
        NoteService service = new InMemoryNoteService();
        service.save(new TextNote(101, "План", "Перша версія", "Навчання", 5));
        service.save(new ReminderNote(102, "Зустріч", "Захист лабораторної", "Навчання", 2, 24));
        service.save(new TextNote(103, "План", "Повторити Java", "Навчання", 1));
        System.out.println("До сортування:");
        printAll(service.findAll());
        System.out.println("За назвою:");
        printAll(service.findAllSortedByName());
        System.out.println("За назвою та пріоритетом (1 перед 5):");
        printAll(service.findAllSortedByNameAndByPriority());
        System.out.println("Сховище після сортування (порядок 101, 102, 103):");
        printAll(service.findAll());
        service.save(new TextNote(101, "План", "Оновлена версія", "Навчання", 3));
        System.out.println("Після оновлення: кількість=" + service.findAll().size());
        System.out.println(service.findById(101));
        System.out.println("Успішне видалення: " + service.delete(102));
        System.out.println("Повторне видалення: " + service.delete(102));
        try { service.findById(999); }
        catch (EntityNotFoundException e) { System.out.println("Оброблено: " + e.getMessage()); }

        CRUDService<TextNote> texts = new InMemoryCrudService<>();
        texts.save(new TextNote("Текст", "Приклад", "Демо", 1));
        CRUDService<ReminderNote> reminders = new InMemoryCrudService<>();
        reminders.save(new ReminderNote("Нагадування", "Приклад", "Демо", 2, 1));
        System.out.println("Той самий generic-клас для двох типів:");
        printAll(texts.findAll());
        printAll(reminders.findAll());
    }
}
