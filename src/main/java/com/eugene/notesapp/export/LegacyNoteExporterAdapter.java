package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public class LegacyNoteExporterAdapter implements NoteExporter
{
    private final LegacyNoteFormatter formatter;

    public LegacyNoteExporterAdapter(LegacyNoteFormatter formatter)
    {
        if (formatter == null)
        {
            throw new IllegalArgumentException("Форматер не може бути null.");
        }
        this.formatter = formatter;
    }

    @Override
    public String export(Note note)
    {
        return formatter.formatRecord(note);
    }

    @Override
    public String exportAll(Note[] notes, int count)
    {
        int safeCount = Math.max(0, Math.min(count, notes.length));
        Note[] selectedNotes = new Note[safeCount];
        System.arraycopy(notes, 0, selectedNotes, 0, safeCount);
        return formatter.formatRecords(selectedNotes);
    }

    @Override
    public String getFormatName()
    {
        return "Legacy";
    }
}
