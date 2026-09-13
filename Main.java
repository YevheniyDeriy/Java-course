import java.util.Scanner;


public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        IO.println("Вітаємо у програмі ведення персональних нотаток!");
        IO.println();

        IO.print("Напишіть назву нотатки: ");
        String noteTitle = scanner.nextLine();
        IO.println("Створена нотатка: " + noteTitle);
        
        IO.print("Введіть категорію: ");
        String noteCategory = scanner.nextLine();

        IO.print("Введіть текст:");
        String noteText = scanner.nextLine();

        IO.print("Вкажіть пріоритет (1-5): ");
        int priority = scanner.nextInt();

        IO.print("Через скільки годин нагадати про нотатку(Введіть 0, без нагадування): ");
        double reminderInHours = scanner.nextDouble();
        boolean isReminder = false;
        if (reminderInHours > 0)
        {
            isReminder = true;
        }
        if (reminderInHours < 0)
        {
            IO.println("Нагадування не встановлено.");
            reminderInHours = 0;
        }

        int wordCount = noteText.trim().isEmpty() ? 0 : noteText.trim().split("\\s+").length;    
        scanner.nextLine();
        IO.println();
        IO.println("========== Нотатка ==========");
        IO.println("Назва: " + noteTitle);
        IO.println("Категорія: " + noteCategory);
        IO.println("Текст: " + noteText);
        IO.println("Кількість слів: " + wordCount);
        IO.println("Пріоритет: " + priority);
        if (isReminder) {
            System.out.printf("Нагадати через: %.2f год.%n", reminderInHours);
        } else {
            IO.println("Нагадування не встановлено.");
        }


        scanner.close();
    }
