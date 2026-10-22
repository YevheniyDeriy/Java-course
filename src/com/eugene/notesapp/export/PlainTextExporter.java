package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;
import com.eugene.notesapp.model.RecurringReminderNote;

public class PlainTextExporter extends AbstractNoteExporter
{
    @Override
    public String export(Note note)
    {
        String reminderText = (note.getReminderInHours() == 0)
                ? "Без нагадування"
                : "Нагадування через: " + note.getReminderInHours() + " год.";

        return "Нотатка: " + note.getTitle() + "\n" +
               "Тип: " + note.getTypeDescription() + "\n" +
               "Категорія: " + note.getCategory() + "\n" +
               "Пріоритет: " + note.getPriority() + "\n" +
               "Текст: " + note.getContent() + "\n" +
               "Кількість слів: " + note.getWordCount() + "\n" +
               reminderText + "\n" +
               (note instanceof RecurringReminderNote recurring
                       ? "Повторювати кожні: " + recurring.getRepeatEveryHours() + " год.\n"
                       : "") +
               "---";
    }

    @Override
    public String getFormatName()
    {
        return "Plain Text";
    }
}
