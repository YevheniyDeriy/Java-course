package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public class LegacyNoteFormatter
{
    public String formatRecord(Note note)
    {
        return "[LEGACY NOTE] " + note.getTitle() + " | " + note.getCategory() + " | " + note.getContent();
    }

    public String formatRecords(Note[] notes)
    {
        StringBuilder result = new StringBuilder();
        for (Note note : notes)
        {
            if (note != null)
            {
                if (result.length() > 0)
                {
                    result.append("\n");
                }
                result.append(formatRecord(note));
            }
        }
        return result.toString();
    }
}
