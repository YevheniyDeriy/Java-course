package com.eugene.notesapp.observer;

import com.eugene.notesapp.model.Note;

public class NoteCountObserver implements NoteObserver
{
    @Override
    public void onNoteAdded(Note note, int totalNotes)
    {
        System.out.println("[СТАТИСТИКА] У списку нотаток: " + totalNotes + ".");
    }
}
