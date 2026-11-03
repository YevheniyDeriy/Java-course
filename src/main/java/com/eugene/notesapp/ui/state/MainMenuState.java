package com.eugene.notesapp.ui.state;

import com.eugene.notesapp.Main;

public class MainMenuState implements MenuState
{
    @Override
    public void handle(MenuContext context)
    {
        IO.println();
        IO.println("=== ГОЛОВНЕ МЕНЮ ===");
        IO.println("1. Операції з нотатками");
        System.out.printf("2. Перегляд нотаток (на разі " + Main.count + " нотаток)%n");
        IO.println("3. Сортування нотаток ");
        IO.println("4. Пошук нотаток");
        IO.println("5. Експорт нотаток");
        IO.println("0. Вихід");
        IO.print("Оберіть дію: ");
        int choice = Main.scanner.nextInt();
        Main.scanner.nextLine();

        switch (choice)
        {
            case 1:
            {
                context.setState(new OperationNotesState());
                break;
            }
            case 2:
            {
                if(Main.count == 0)
                {
                    IO.println("Немає нотаток для перегляду! Спершу створіть нотатку!");
                    break;
                }
                context.setState(new ViewNotesState());
                break;
            }
            case 3:
            {
                if(Main.count == 0)
                {
                    IO.println("Немає нотаток для сортування! Спершу створіть нотатку!");
                    break;
                }
                context.setState(new SortNotesState());
                break;
            }
            case 4:
            {
                if(Main.count == 0)
                {
                    IO.println("Немає нотаток для пошуку! Спершу створіть нотатку!");
                    break;
                }
                context.setState(new SearchNotesState());
                break;
            }
            case 5:
            {
                if(Main.count == 0)
                {
                    IO.println("Немає нотаток для експорту! Спершу створіть нотатку!");
                    break;
                }
                context.setState(new ExportNotesState());
                break;
            }
            case 0:
            {
                IO.println("До побачення!");
                context.stop();
                break;
            }
            default:
            {
                IO.println("Невірне число! Введіть коректне число.");

            }

        }
    }
}
