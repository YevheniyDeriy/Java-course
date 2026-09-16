import java.util.Scanner;

public class Main
{
    static Scanner scanner = new Scanner(System.in);
    static int count = 0;
    static Note[] notes = new Note[10];
    public static void main(String[] args)
    {
        IO.println("Вітаємо у програмі ведення персональних нотаток!");
        int choice;
        do
        {
            Menu(count);
            IO.print("Оберіть дію: ");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice)
            {
                case 1: {
                    AddNote(scanner);
                    count++;
                    break;
                }
                case 2: {
                    break;
                }
                case 3:
                {
                    IO.println();
                    if (count == 0)
                    {
                        IO.println("Нотаток для виводу немає, спершу створіть нотатку.");
                        break;
                    }
                    PrintNotes(notes, count);
                    break;
                }
                case 4:
                {
                    IO.println();
                    if (count == 0)
                    {
                        IO.println("Нотаток для виводу немає, спершу створіть нотатку.");
                        break;
                    }
                    PrintNotesByPriority(notes, count);
                    break;
                }
                case 5:
                {
                    BubbleSort();
                    break;
                }
                case 6:
                {
                    Search();
                    break;
                }
                case 7:
                {
                    IO.println("Гарного дня!");
                    break;
                }
                default:
                {
                    IO.println();
                    IO.println("Невірне число!Введіть коректне число.");
                }
            }
        } while (choice != 7);
        scanner.close();
    }

    public static void AddNote( Scanner scanner)
    {
    IO.println();
    IO.print("Напишіть назву нотатки: ");
    String title = scanner.nextLine();
    IO.println("Створена нотатка: " + title);

    IO.print("Введіть категорію: ");
    String category = scanner.nextLine();

    IO.print("Введіть текст:");
    String content = scanner.nextLine();

    IO.print("Вкажіть пріоритет (1-5): ");
    int priority = scanner.nextInt();

    IO.print("Через скільки годин нагадати про нотатку(Введіть 0, без нагадування): ");
    double reminderInHours = scanner.nextDouble();
    scanner.nextLine();

    int wordCount = content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length;

    notes[count] = new Note(title, content, category, priority, reminderInHours, wordCount);
    }

    public static void PrintNotes(Note[] notes, int count)
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
    public static void Menu(int count)
    {
    IO.println("=== Меню ===");
    IO.println("1. Створити нотатку");
    IO.println("2. Видалити нотатку");
    System.out.printf("3. Вивести всі нотатки(у вас " + count + " нотаток)%n" );
    IO.println("4. Перерахунок нотаток за пріорітетом");
    IO.println("5. Сортування бульбашкою");
    IO.println("6. Пошук");
    IO.println("7. Вийти");
    }
    public static void PrintNotesByPriority(Note[] notes, int count)
    {
        IO.print("Введіть пріорітет для підрахунку");
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
    public static void BubbleSort()
    {
        IO.println();
        IO.println("МАСИВ ДО СОРТУВАННЯ: ");
        PrintNotes(notes, count);
        IO.println();
        IO.println("МАСИВ ПІСЛЯ СОРТУВАННЯ: ");
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
        PrintNotes(notes, count);
    }
    public static void Search()
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
