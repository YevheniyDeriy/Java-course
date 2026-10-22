package com.eugene.notesapp.model;

public final class TextNote extends Note
{
    public TextNote(String title, String content, String category, int priority)
    {
        super(title, content, category, priority);
        System.out.println("[constructor] TextNote");
    }

    @Override
    public String getTypeDescription()
    {
        return "Звичайна нотатка";
    }

    @Override
    public String toString()
    {
        return super.toString() + "\nНагадування: відсутнє";
    }
}
