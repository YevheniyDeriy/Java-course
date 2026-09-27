package com.eugene.notesapp.model;

import java.util.Objects;
import java.time.LocalDate;

public class Note
{
    private String title;
    private String content;
    private LocalDate date;
    private String category;
    private int priority;
    private double reminderInHours;
    private int wordCount;

    public Note(String title, String content, String category, int priority, double reminderInHours, int wordCount)
    {
        this.title = title;
        this.content = content;
        this.date = LocalDate.now();
        this.category = category;
        this.priority = priority;
        this.reminderInHours = reminderInHours;
        this.wordCount = wordCount;
    }

    public String getTitle()
    {
        return title;
    }

    public String getContent()
    {
        return content;
    }

    public LocalDate getDate()
    {
        return date;
    }

    public String getCategory()
    {
        return category;
    }

    public int getPriority()
    {
        return priority;
    }

    public double getReminderInHours()
    {
        return reminderInHours;
    }

    public int getWordCount()
    {
        return wordCount;
    }

    @Override
    public String toString()
    {
        String reminderText = (reminderInHours == 0) ? "Без нагадування" : "Нагадування через: " + reminderInHours + " годин";
        return "Нотатка: " + title + "\n" +
                "Категорія: " + category + "\n" +
                "Пріоритет: " + priority + "\n" +
                "{" + "\n" + " " + content + "\n" + "}" + "\n" +
                reminderText + "\n" +
                "Кількість слів: " + wordCount;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }
        Note other = (Note) obj;
        return priority == other.priority &&
                Double.compare(reminderInHours, other.reminderInHours) == 0 &&
                wordCount == other.wordCount &&
                Objects.equals(title, other.title) &&
                Objects.equals(content, other.content) &&
                Objects.equals(category, other.category);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(title, content, category, priority, reminderInHours, wordCount);
    }

}