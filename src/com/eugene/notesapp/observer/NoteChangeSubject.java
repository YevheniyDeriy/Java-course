package com.eugene.notesapp.observer;

import com.eugene.notesapp.model.Note;

import java.util.ArrayList;
import java.util.List;

public class NoteChangeSubject
{
    private final List<NoteObserver> observers = new ArrayList<>();

    public void subscribe(NoteObserver observer)
    {
        if (observer == null)
        {
            throw new IllegalArgumentException("Спостерігач не може бути null.");
        }
        if (!observers.contains(observer))
        {
            observers.add(observer);
        }
    }

    public void unsubscribe(NoteObserver observer)
    {
        observers.remove(observer);
    }

    public void notifyNoteAdded(Note note, int totalNotes)
    {
        for (NoteObserver observer : new ArrayList<>(observers))
        {
            observer.onNoteAdded(note, totalNotes);
        }
    }
}
