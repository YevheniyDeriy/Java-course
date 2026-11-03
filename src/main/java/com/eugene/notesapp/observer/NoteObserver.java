package com.eugene.notesapp.observer;

import com.eugene.notesapp.model.Note;

public interface NoteObserver
{
    void onNoteAdded(Note note, int totalNotes);
}
