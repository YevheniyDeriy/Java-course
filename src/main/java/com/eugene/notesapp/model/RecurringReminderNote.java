package com.eugene.notesapp.model;

public final class RecurringReminderNote extends ReminderNote
{
    private final double repeatEveryHours;

    public RecurringReminderNote(String title, String content, String category, int priority,
                                 double reminderInHours, double repeatEveryHours)
    {
        this(title, content, category, priority, reminderInHours, repeatEveryHours,
                countWords(content));
    }

    private RecurringReminderNote(String title, String content, String category, int priority,
                                  double reminderInHours, double repeatEveryHours, int wordCount)
    {
        super(title, content, category, priority, reminderInHours, wordCount);
        if (repeatEveryHours <= 0)
        {
            throw new IllegalArgumentException("Інтервал повторення має бути більшим за 0.");
        }
        this.repeatEveryHours = repeatEveryHours;
    }

    public double getRepeatEveryHours()
    {
        return repeatEveryHours;
    }

    public RecurringReminderNote(long id, String title, String content, String category, int priority,
                                 double reminderInHours, double repeatEveryHours,
                                 java.time.LocalDate date)
    {
        super(id, title, content, category, priority, reminderInHours, date);
        if (repeatEveryHours <= 0) throw new IllegalArgumentException("Repeat interval must be positive");
        this.repeatEveryHours = repeatEveryHours;
    }

    @Override
    public String getTypeDescription()
    {
        return "Нотатка з повторюваним нагадуванням";
    }

    @Override
    public String toString()
    {
        return super.toString() + "\nПовторювати кожні: " + repeatEveryHours + " год.";
    }

}
