package com.eugene.notesapp.service;

import com.eugene.notesapp.model.Note;
import java.util.List;

public interface NoteService extends CRUDService<Note> {
    List<Note> findAllSortedByNameAndByPriority();
}
