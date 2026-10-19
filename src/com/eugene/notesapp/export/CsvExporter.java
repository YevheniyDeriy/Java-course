package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;
import com.eugene.notesapp.model.RecurringReminderNote;

public class CsvExporter extends AbstractNoteExporter
{
    @Override
    public String export(Note note)
    {
        return escape(note.getTitle()) + "," +
               escape(note.getTypeDescription()) + "," +
               escape(note.getCategory()) + "," +
               note.getPriority() + "," +
               escape(note.getContent()) + "," +
               note.getWordCount() + "," +
               note.getReminderInHours() + "," +
               (note instanceof RecurringReminderNote recurring
                       ? recurring.getRepeatEveryHours()
                       : "");
    }

    @Override
    public String exportAll(Note[] notes, int count)
    {
        String header = "title,type,category,priority,content,wordCount,reminderInHours,repeatEveryHours";
        return header + "\n" + super.exportAll(notes, count);
    }

    @Override
    public String getFormatName()
    {
        return "CSV";
    }

    private String escape(String value)
    {
        if (value == null)
        {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r"))
        {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
