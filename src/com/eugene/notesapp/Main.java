package com.eugene.notesapp;

import com.eugene.notesapp.model.Note;
import com.eugene.notesapp.exception.DomainException;
import com.eugene.notesapp.exception.InvalidPriorityException;
import com.eugene.notesapp.exception.InvalidReminderException;
import com.eugene.notesapp.ui.state.MainMenuState;
import com.eugene.notesapp.ui.state.MenuContext;

import java.util.Scanner;
import java.util.InputMismatchException;

public class Main
{
    public static Scanner scanner = new Scanner(System.in);
    public static int count = 0;
    public static Note[] notes = new Note[10];

    public static void main(String[] args)
    {
        IO.println("Вітаємо у програмі ведення персональних нотаток!");

        MenuContext context = new MenuContext(new MainMenuState());
        context.run();

        scanner.close();
    }

    // FUNCTIONS  ↓
    private static void validatePriority(int priority) throws InvalidPriorityException
    {
        if (priority < 1 || priority > 5)
        {
            throw new InvalidPriorityException(
                    "Пріоритет має бути в діапазоні від 1 до 5.", priority);
        }
    }
    private static void validateReminder(double reminderInHours) throws InvalidReminderException
    {
        if (reminderInHours < 0)
        {
            throw new InvalidReminderException(
                    "Час нагадування не може бути від'ємним.", reminderInHours);
        }
    }
    private static Note buildNote(String title, String content, String category,
                                  int priority, double reminderInHours) throws DomainException
    {
        try
        {
            validatePriority(priority);
            validateReminder(reminderInHours);

            int wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;
            return new Note(title, content, category, priority, reminderInHours, wordCount);
        }
        catch (DomainException e)
        {
            System.err.println("[LOG] Порушення доменного правила при створенні нотатки: " + e.getMessage());
            throw e; 
        }
    }

    public static boolean addNote()
    {
        IO.println();
        IO.print("Напишіть назву нотатки: ");
        String title = scanner.nextLine();
        IO.println("Створена нотатка: " + title);

        IO.print("Введіть категорію: ");
        String category = scanner.nextLine();

        IO.print("Введіть текст:");
        String content = scanner.nextLine();

        boolean success = false;

        try
        {
            IO.print("Вкажіть пріоритет (1-5): ");
            int priority = scanner.nextInt();

            if (priority < 1 || priority > 5)
            {
                throw new InvalidPriorityException(
                        "Пріоритет має бути в діапазоні від 1 до 5.", priority);
            }

            IO.print("Через скільки годин нагадати про нотатку(Введіть 0, без нагадування): ");
            double reminderInHours = scanner.nextDouble();
            scanner.nextLine();

            int wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;

            notes[count] = new Note(title, content, category, priority, reminderInHours, wordCount);
            success = true;
        }
        catch (InvalidPriorityException e)
        {
            IO.println("Помилка: " + e.getMessage() +
                    " (введене значення: " + e.getInvalidPriority() + ")");
        }
        catch (InputMismatchException e)
        {
            IO.println("Помилка: пріоритет і час нагадування мають бути числами.");
            scanner.nextLine();
        }
        catch (ArrayIndexOutOfBoundsException e)
        {
            IO.println("Помилка: масив нотаток заповнений (максимум " + notes.length +
                    "). Видаліть стару нотатку, щоб додати нову.");
        }
        finally
        {
            if (success)
            {
                IO.println("Нотатка успішно створена.");
            }
            else
            {
                IO.println("Нотатку не створено.");
            }
        }

        return success;
    }



    public static void printNotes()
    {
        IO.println("\n Нотатки");
        int printed = 0;
        for (Note note : notes) {
            if (printed >= count) {
                break;
            }
            IO.println(note);
            printed++;
        }
    }

    public static void printNotesByPriority()
    {
        IO.print("Введіть пріорітет для підрахунку");
        try
        {
            int priority = scanner.nextInt();
            scanner.nextLine();
            int maches = 0;
            for (int i = 0; i < count; i++)
            {
                if (notes[i].getPriority() == priority)
                {
                    IO.println(notes[i]);
                    maches++;
                }
            }
            System.out.printf("Всього знайдено " + maches + " нотаток з пріорітетом: " + priority + "\n");
        }
        catch (InputMismatchException e)
        {
            IO.println("Помилка: пріоритет має бути цілим числом. Спробуйте ще раз.");
            scanner.nextLine();
        }
    }

    public static void sortNotes()
    {
        IO.println();
        IO.println("МАСИВ ДО СОРТУВАННЯ: ");
        printNotes();
        IO.println();
        IO.println("МАСИВ ПІСЛЯ СОРТУВАННЯ: ");
        bubbleSortPriority();
        printNotes();
    }

    public static void bubbleSortPriority()
    {
        for (int i = 0; i < count - 1; i++)
        {
            for (int j = 0; j < count - i - 1; j++)
            {
                if (notes[j].getPriority() > notes[j + 1].getPriority())
                {
                    Note temp = notes[j];
                    notes[j] = notes[j + 1];
                    notes[j + 1] = temp;
                }
            }
        }
    }

    public static void searchObject()
    {
        IO.println();
        IO.println("Введіть дані нотатки-зразка для пошуку (мають повністю збігатися з існуючою):");

        IO.print("Назва: ");
        String title = scanner.nextLine();

        IO.print("Категорія: ");
        String category = scanner.nextLine();

        IO.print("Текст: ");
        String content = scanner.nextLine();

        IO.print("Пріоритет (1-5): ");
        int priority = scanner.nextInt();

        IO.print("Нагадування через (0 — без нагадування): ");
        double reminderInHours = scanner.nextDouble();
        scanner.nextLine();

        int wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;

        Note sample = new Note(title, content, category, priority, reminderInHours, wordCount);

        int foundIndex = -1;
        for (int i = 0; i < count; i++)
        {
            if (notes[i].equals(sample))
            {
                foundIndex = i;
                break;
            }
        }

        if (foundIndex != -1)
        {
            IO.println("Знайдено! Індекс у масиві: " + foundIndex);
            IO.println(notes[foundIndex]);
        }
        else
        {
            IO.println("Такої нотатки в масиві немає.");
        }
    }

}
