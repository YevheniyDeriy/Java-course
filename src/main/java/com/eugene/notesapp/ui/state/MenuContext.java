package com.eugene.notesapp.ui.state;

public class MenuContext
{
    private MenuState currentState;
    private boolean isRunning = true;

    public MenuContext(MenuState initialState)
    {
        this.currentState = initialState;
    }

    public void setState(MenuState state)
    {
        this.currentState = state;
    }

    public void stop()
    {
        isRunning = false;
    }

    public void run()
    {
        while (isRunning)
        {
            currentState.handle(this);
        }
    }


}
