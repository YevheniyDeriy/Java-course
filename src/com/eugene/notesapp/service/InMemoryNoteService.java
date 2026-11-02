package com.eugene.notesapp.service;

import com.eugene.notesapp.model.Note;
import java.util.Comparator;
import java.util.List;

public class InMemoryNoteService extends InMemoryCrudService<Note> implements NoteService {
    @Override
    public List<Note> findAllSortedByNameAndByPriority() {
        return findAllSorted(Comparator.comparing(Note::getName)
                .thenComparingInt(Note::getPriority));
    }
}
