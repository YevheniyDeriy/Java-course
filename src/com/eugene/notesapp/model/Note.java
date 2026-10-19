package com.eugene.notesapp.model;

import java.time.LocalDate;
import java.util.Objects;

public abstract sealed class Note permits TextNote, ReminderNote
{
    private final String title;
    private final String content;
    private final LocalDate date;
    private final String category;
    private final int priority;
    private final int wordCount;

    protected Note(String title, String content, String category, int priority)
    {
        this(title, content, category, priority, countWords(content));
        System.out.println("[constructor] Note (this-delegation)");
    }

    protected Note(String title, String content, String category, int priority, int wordCount)
    {
        this.title = title;
        this.content = content;
        this.date = LocalDate.now();
        this.category = category;
        this.priority = priority;
        this.wordCount = wordCount;
        System.out.println("[constructor] Note");
    }

    protected static int countWords(String content)
    {
        return content == null || content.trim().isEmpty()
                ? 0
                : content.trim().split("\\s+").length;
    }

    public String getTitle() { return title; }
    public String getContent() { return content; }
    public final LocalDate getDate() { return date; }
    public String getCategory() { return category; }
    public int getPriority() { return priority; }
    public int getWordCount() { return wordCount; }

    /** Returns the number of hours until a reminder, or zero when there is none. */
    public double getReminderInHours() { return 0; }

    public abstract String getTypeDescription();

    @Override
    public String toString()
    {
        return getTypeDescription() + ": " + title + "\n" +
                "Категорія: " + category + "\n" +
                "Пріоритет: " + priority + "\n" +
                "Дата: " + date + "\n" +
                "{\n " + content + "\n}\n" +
                "Кількість слів: " + wordCount;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Note other = (Note) obj;
        return priority == other.priority && wordCount == other.wordCount &&
                Objects.equals(title, other.title) &&
                Objects.equals(content, other.content) &&
                Objects.equals(category, other.category);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getClass(), title, content, category, priority, wordCount);
    }
}
