package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public abstract class AbstractNoteExporter implements NoteExporter
{
    @Override
    public String exportAll(Note[] notes, int count)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++)
        {
            if (i > 0)
            {
                sb.append("\n");
            }
            sb.append(export(notes[i]));
        }
        return sb.toString();
    }
}
