package com.eugene.notesapp.export;

import com.eugene.notesapp.model.Note;

public class ExportContext
{
    private NoteExporter exporter;

    public ExportContext(NoteExporter exporter)
    {
        this.exporter = exporter;
    }

    public void setExporter(NoteExporter exporter)
    {
        this.exporter = exporter;
    }

    public NoteExporter getExporter()
    {
        return exporter;
    }

    public String executeExportAll(Note[] notes, int count)
    {
        return exporter.exportAll(notes, count);
    }
}
