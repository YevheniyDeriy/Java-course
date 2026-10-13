package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public class CsvExporter extends AbstractNoteExporter
{
    @Override
    public String export(Note note)
    {
        return escape(note.getTitle()) + "," +
               escape(note.getCategory()) + "," +
               note.getPriority() + "," +
               escape(note.getContent()) + "," +
               note.getWordCount() + "," +
               note.getReminderInHours();
    }

    @Override
    public String exportAll(Note[] notes, int count)
    {
        String header = "title,category,priority,content,wordCount,reminderInHours";
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
