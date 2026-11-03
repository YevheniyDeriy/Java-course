package com.eugene.notesapp.exception;

public class InvalidPriorityException extends DomainException
{
    private final int invalidPriority;

    public InvalidPriorityException(String message, int invalidPriority)
    {
        super(message);
        this.invalidPriority = invalidPriority;
    }

    public int getInvalidPriority()
    {
        return invalidPriority;
    }

}
