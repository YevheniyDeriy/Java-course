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
                Main.addNote();
                break;
            }
            case 2:
            {
                IO.print("Введіть id для видалення: ");
                try {
                    long id = Main.scanner.nextLong();
                    Main.scanner.nextLine();
                    IO.println(Main.noteService.delete(id) ? "Видалено." : "Такого id немає.");
                    Main.refreshSnapshot();
                } catch (InputMismatchException e) {
                    IO.println("id має бути цілим числом.");
                    Main.scanner.nextLine();
                }
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
