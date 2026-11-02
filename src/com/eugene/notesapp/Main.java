package com.eugene.notesapp;

import com.eugene.notesapp.model.Note;
import com.eugene.notesapp.model.TextNote;
import com.eugene.notesapp.model.ReminderNote;
import com.eugene.notesapp.model.RecurringReminderNote;
import com.eugene.notesapp.exception.DomainException;
import com.eugene.notesapp.exception.InvalidPriorityException;
import com.eugene.notesapp.exception.InvalidReminderException;
import com.eugene.notesapp.ui.state.MainMenuState;
import com.eugene.notesapp.ui.state.MenuContext;
import com.eugene.notesapp.observer.NoteChangeSubject;
import com.eugene.notesapp.observer.NoteCountObserver;
import com.eugene.notesapp.observer.NoteLogObserver;

import java.util.Scanner;
import java.util.InputMismatchException;

public class Main
{
    public static Scanner scanner = new Scanner(System.in);
    public static int count = 0;
    public static Note[] notes = new Note[0];
    public static final com.eugene.notesapp.service.NoteService noteService =
            new com.eugene.notesapp.service.InMemoryNoteService();

    public static void refreshSnapshot() {
        notes = noteService.findAll().toArray(Note[]::new);
        count = notes.length;
    }
    public static final NoteChangeSubject noteChanges = new NoteChangeSubject();

    public static void main(String[] args)
    {
        noteChanges.subscribe(new NoteLogObserver());
        noteChanges.subscribe(new NoteCountObserver());
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
                                  int priority, double reminderInHours,
                                  double repeatEveryHours) throws DomainException
    {
        try
        {
            validatePriority(priority);
            validateReminder(reminderInHours);
            validateReminder(repeatEveryHours);

            if (repeatEveryHours > 0 && reminderInHours == 0)
            {
                throw new InvalidReminderException(
                        "Для повторюваного нагадування спочатку вкажіть час першого нагадування.", repeatEveryHours);
            }
            if (reminderInHours == 0)
            {
                return new TextNote(title, content, category, priority);
            }
            if (repeatEveryHours > 0)
            {
                return new RecurringReminderNote(title, content, category, priority,
                        reminderInHours, repeatEveryHours);
            }
            return new ReminderNote(title, content, category, priority, reminderInHours);
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

            IO.print("Через скільки годин нагадати про нотатку(Введіть 0, без нагадування): ");
            double reminderInHours = scanner.nextDouble();
            double repeatEveryHours = 0;
            if (reminderInHours > 0)
            {
                IO.print("Повторювати нагадування кожні скільки годин? (0 — одноразове): ");
                repeatEveryHours = scanner.nextDouble();
            }
            scanner.nextLine();

            Note created = buildNote(title, content, category, priority,
                    reminderInHours, repeatEveryHours);
            noteService.save(created);
            refreshSnapshot();
            noteChanges.notifyNoteAdded(created, count);
            success = true;
        }
        catch (InvalidPriorityException e)
        {
            IO.println("Помилка: " + e.getMessage() +
                    " (введене значення: " + e.getInvalidPriority() + ")");
        }
        catch (InvalidReminderException e)
        {
            IO.println("Помилка: " + e.getMessage() +
                    " (введене значення: " + e.getInvalidReminder() + ")");
        }
        catch (DomainException e)
        {
            IO.println("Помилка: " + e.getMessage());
        }
        catch (InputMismatchException e)
        {
            IO.println("Помилка: пріоритет і час нагадування мають бути числами.");
            scanner.nextLine();
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
            IO.println("id=" + note.getId());
            IO.println(note);
            IO.println("Поліморфний опис: " + describeNote(note));
            if (note instanceof RecurringReminderNote recurring)
            {
                IO.println("Інтервал повторення через базове посилання: " +
                        recurring.getRepeatEveryHours() + " год.");
            }
            printed++;
        }
    }

    private static String describeNote(Note note)
    {
        return switch (note)
        {
            case TextNote textNote -> textNote.getTypeDescription();
            case RecurringReminderNote recurring -> recurring.getTypeDescription();
            case ReminderNote reminder -> reminder.getTypeDescription();
        };
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
        IO.println("Сховище до сортування:");
        com.eugene.notesapp.util.EntityPrinter.printAll(noteService.findAll());
        IO.println("За назвою:");
        com.eugene.notesapp.util.EntityPrinter.printAll(noteService.findAllSortedByName());
        IO.println("За назвою та пріоритетом:");
        com.eugene.notesapp.util.EntityPrinter.printAll(noteService.findAllSortedByNameAndByPriority());
        IO.println("Сховище після сортування:");
        com.eugene.notesapp.util.EntityPrinter.printAll(noteService.findAll());
    }

    public static void searchObject()
    {
        IO.print("Введіть id нотатки: ");
        try {
            long id = scanner.nextLong();
            scanner.nextLine();
            IO.println(noteService.findById(id));
        } catch (com.eugene.notesapp.exception.EntityNotFoundException e) {
            IO.println(e.getMessage());
        } catch (InputMismatchException e) {
            IO.println("id має бути цілим числом.");
            scanner.nextLine();
        }
    }
}
