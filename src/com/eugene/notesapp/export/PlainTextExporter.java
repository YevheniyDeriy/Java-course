package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public class PlainTextExporter extends AbstractNoteExporter
{
    @Override
    public String export(Note note)
    {
        String reminderText = (note.getReminderInHours() == 0)
                ? "Без нагадування"
                : "Нагадування через: " + note.getReminderInHours() + " год.";

        return "Нотатка: " + note.getTitle() + "\n" +
               "Категорія: " + note.getCategory() + "\n" +
               "Пріоритет: " + note.getPriority() + "\n" +
               "Текст: " + note.getContent() + "\n" +
               "Кількість слів: " + note.getWordCount() + "\n" +
               reminderText + "\n" +
               "---";
    }

    @Override
    public String getFormatName()
    {
        return "Plain Text";
    }
}
