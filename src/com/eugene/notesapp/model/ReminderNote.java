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

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj) &&
                Double.compare(reminderInHours, ((ReminderNote) obj).reminderInHours) == 0;
    }

    @Override
    public int hashCode()
    {
        return 31 * super.hashCode() + Double.hashCode(reminderInHours);
    }
}
