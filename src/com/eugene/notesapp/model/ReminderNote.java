package com.eugene.notesapp.model;

public sealed class ReminderNote extends Note permits RecurringReminderNote
{
    private final double reminderInHours;

    public ReminderNote(String title, String content, String category, int priority,
                        double reminderInHours)
    {
        this(title, content, category, priority, reminderInHours, countWords(content));
        System.out.println("[constructor] ReminderNote (this-delegation)");
    }

    protected ReminderNote(String title, String content, String category, int priority,
                           double reminderInHours, int wordCount)
    {
        super(title, content, category, priority, wordCount);
        this.reminderInHours = reminderInHours;
        System.out.println("[constructor] ReminderNote");
    }

    public ReminderNote(long id, String title, String content, String category, int priority,
                        double reminderInHours)
    {
        super(id, title, content, category, priority, countWords(content));
        this.reminderInHours = reminderInHours;
    }

    @Override
    public double getReminderInHours()
    {
        return reminderInHours;
    }

    @Override
    public String getTypeDescription()
    {
        return "Нотатка-нагадування";
    }

    @Override
    public String toString()
    {
        return super.toString() + "\nНагадування через: " + reminderInHours + " год.";
    }

}
