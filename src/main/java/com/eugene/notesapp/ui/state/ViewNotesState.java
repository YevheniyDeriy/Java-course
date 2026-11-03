package com.eugene.notesapp.ui.state;

import com.eugene.notesapp.Main;

public class ViewNotesState implements MenuState
{
    @Override
    public void handle(MenuContext context)
    {
        IO.println();
        IO.println("=== ПЕРЕГЛЯД НОТАТОК ===");
        IO.println("1. Вивести всі нотатки");
        IO.println("2. Вивести нотатки з даним пріорітетом");
        IO.println("0. Повернутися до попереднього меню");
        IO.print("Оберіть дію: ");
        int choice = Main.scanner.nextInt();
        Main.scanner.nextLine();

        switch(choice)
        {
            case 1:
            {
                Main.printNotes();
                break;
            }
            case 2:
            {
                Main.printNotesByPriority();
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
