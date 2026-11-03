package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public interface NoteExporter
{
    String export(Note note);

    String exportAll(Note[] notes, int count);

    default String getFormatName()
    {
        return "Unknown";
    }
}
