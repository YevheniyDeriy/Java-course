package com.eugene.notesapp.model;

public final class TextNote extends Note
{
    public TextNote(String title, String content, String category, int priority)
    {
        super(title, content, category, priority);
    }

    public TextNote(long id, String title, String content, String category, int priority)
    {
        super(id, title, content, category, priority, countWords(content));
    }

    @Override
    public String getTypeDescription()
    {
        return "Звичайна нотатка";
    }

    public TextNote(long id, String title, String content, String category, int priority,
                    java.time.LocalDate date)
    {
        super(id, title, content, category, priority, countWords(content), date);
    }

    @Override
    public String toString()
    {
        return super.toString() + "\nНагадування: відсутнє";
    }
}
