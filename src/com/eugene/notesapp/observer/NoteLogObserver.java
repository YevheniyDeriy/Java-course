package com.eugene.notesapp.observer;

import com.eugene.notesapp.model.Note;

public class NoteLogObserver implements NoteObserver
{
    @Override
    public void onNoteAdded(Note note, int totalNotes)
    {
        System.out.println("[ЖУРНАЛ] Додано нотатку «" + note.getTitle() + "».");
    }
}
