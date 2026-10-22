package com.eugene.notesapp.export;

import java.util.Locale;

public class NoteExporterFactory
{
    public NoteExporter create(String formatCode)
    {
        if (formatCode == null)
        {
            throw new IllegalArgumentException("Код формату не може бути null.");
        }

        switch (formatCode.trim().toUpperCase(Locale.ROOT))
        {
            case "TEXT":
                return new PlainTextExporter();
            case "CSV":
                return new CsvExporter();
            case "LEGACY":
                return new LegacyNoteExporterAdapter(new LegacyNoteFormatter());
            default:
                throw new IllegalArgumentException("Невідомий формат експорту: " + formatCode);
        }
    }

    public NoteExporter[] createAll()
    {
        return new NoteExporter[] { create("TEXT"), create("CSV"), create("LEGACY") };
    }
}
