package com.eugene.notesapp.ui.state;

import com.eugene.notesapp.Main;

public class SortNotesState implements MenuState
{
    @Override
    public void handle(MenuContext context)
    {
        IO.println();
        IO.println("=== СОРТУВАННЯ НОТАТОК ===");
        IO.println("1. Сортування за пріоритетом (бульбашка) ");
        IO.println("0. Повернутися до головного меню");
        IO.print("Оберіть дію: ");
        int choice = Main.scanner.nextInt();
        Main.scanner.nextLine();

        switch(choice)
        {
            case 1:
            {
                Main.sortNotes();
                break;
            }
            case 0:
            {
                context.setState(new MainMenuState());
                break;
            }
            default:
            {
                IO.println("Невірне число! Введіть коректне число.");
            }
        }

    }
}
