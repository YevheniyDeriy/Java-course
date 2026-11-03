package com.eugene.notesapp.model;

import java.time.LocalDate;


public abstract sealed class Note extends BaseEntity permits TextNote, ReminderNote
{
    private final String content;
    private final LocalDate date;
    private final String category;
    private final int priority;
    private final int wordCount;

    protected Note(String title, String content, String category, int priority)
    {
        this(title, content, category, priority, countWords(content));
    }

    protected Note(String title, String content, String category, int priority, int wordCount)
    {
        super(title);
        this.content = content;
        this.date = LocalDate.now();
        this.category = category;
        this.priority = priority;
        this.wordCount = wordCount;
    }

    protected Note(long id, String title, String content, String category, int priority, int wordCount)
    {
        this(id, title, content, category, priority, wordCount, LocalDate.now());
    }

    protected Note(long id, String title, String content, String category, int priority,
                   int wordCount, LocalDate date)
    {
        super(id, title);
        this.content = content;
        this.date = java.util.Objects.requireNonNull(date, "date");
        this.category = category;
        this.priority = priority;
        this.wordCount = wordCount;
    }

    protected static int countWords(String content)
    {
        return content == null || content.trim().isEmpty()
                ? 0
                : content.trim().split("\\s+").length;
    }

    public String getTitle() { return getName(); }
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
        return getTypeDescription() + ": " + getName() + "\n" +
                "Категорія: " + category + "\n" +
                "Пріоритет: " + priority + "\n" +
                "Дата: " + date + "\n" +
                "{\n " + content + "\n}\n" +
                "Кількість слів: " + wordCount;
    }

}
