package com.eugene.notesapp.ui.state;

import com.eugene.notesapp.Main;
import java.util.InputMismatchException;
public class OperationNotesState implements MenuState
{
    @Override
    public void handle(MenuContext context)
    {
        IO.println();
        IO.println("=== ОПЕРАЦІЇ З НОТАТКАМИ ===");
        IO.println("1. Додати нотатку");
        IO.println("2. Видалити нотатку");
        IO.println("0. Повернутися в головне меню");
        IO.print("Оберіть дію: ");
        int choice;
        try
        {
            choice = Main.scanner.nextInt();
            Main.scanner.nextLine();
        }
        catch (InputMismatchException e)
        {
            IO.println("Помилка: введіть число від 0 до 4.");
            Main.scanner.nextLine();
            return;
        }
        switch(choice)
        {
            case 1:
            {
                boolean added = Main.addNote();
                if (added)
                {
                    Main.count++;
                    Main.noteChanges.notifyNoteAdded(Main.notes[Main.count - 1], Main.count);
                }
                break;
            }
            case 2:
            {
                IO.println("Функціонал нажаль ще не реалізований...");
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
