package com.eugene.notesapp.exception;

public class InvalidReminderException extends DomainException
{
    private final double invalidReminder;

    public InvalidReminderException(String message, double invalidReminder)
    {
        super(message);
        this.invalidReminder = invalidReminder;
    }

    public double getInvalidReminder()
    {
        return invalidReminder;
    }
}
