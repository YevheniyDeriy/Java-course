package com.eugene.notesapp.model;

public final class RecurringReminderNote extends ReminderNote
{
    private final double repeatEveryHours;

    public RecurringReminderNote(String title, String content, String category, int priority,
                                 double reminderInHours, double repeatEveryHours)
    {
        this(title, content, category, priority, reminderInHours, repeatEveryHours,
                countWords(content));
        System.out.println("[constructor] RecurringReminderNote (this-delegation)");
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
        System.out.println("[constructor] RecurringReminderNote");
    }

    public double getRepeatEveryHours()
    {
        return repeatEveryHours;
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
