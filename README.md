# Personal Notes

Консольний Java-застосунок для ведення персональних нотаток.

## Предметна область

Застосунок призначений для створення та роботи з персональними нотатками. Користувач може створювати нотатки, переглядати їх, знаходити потрібну нотатку, сортувати за пріоритетом та експортувати у різних форматах.

## Сутність

Основною сутністю програми є **Note (Нотатка)**.

| Характеристика    | Тип         | Опис                         |
| ----------------- | ----------- | ---------------------------- |
| `title`           | `String`    | Назва нотатки                |
| `content`         | `String`    | Текст нотатки                |
| `date`            | `LocalDate` | Дата створення нотатки       |
| `category`        | `String`    | Категорія нотатки            |
| `priority`        | `int`       | Пріоритет нотатки від 1 до 5 |
| `reminderInHours` | `double`    | Через скільки годин нагадати |
| `wordCount`       | `int`       | Кількість слів у тексті      |

## Структура проєкту

```text
src/com/eugene/notesapp/
├── Main.java                          — точка входу, статичні методи CRUD
├── model/
│   └── Note.java                      — модель нотатки
├── exception/
│   ├── DomainException.java           — базовий checked exception
│   ├── InvalidPriorityException.java  — некоректний пріоритет
│   └── InvalidReminderException.java  — некоректне нагадування
├── export/                            — ЛР4: інтерфейси та Strategy
│   ├── NoteExporter.java              — інтерфейс з default-методом getFormatName()
│   ├── AbstractNoteExporter.java      — абстрактний клас, спільна логіка exportAll()
│   ├── PlainTextExporter.java         — реалізація №1: текстовий формат
│   ├── CsvExporter.java              — реалізація №2: CSV формат з escape
│   └── ExportContext.java             — Strategy: контекст із setExporter()
└── ui/state/                          — ЛР3: State pattern для меню
    ├── MenuState.java                 — інтерфейс стану меню
    ├── MenuContext.java               — контекст State-паттерну
    ├── MainMenuState.java             — головне меню
    ├── OperationNotesState.java       — додавання / видалення нотаток
    ├── ViewNotesState.java            — перегляд нотаток
    ├── SearchNotesState.java          — пошук нотаток
    ├── SortNotesState.java            — сортування нотаток
    └── ExportNotesState.java          — ЛР4: експорт із вибором формату
```

## ЛР4: Інтерфейси та паттерн Strategy

### Рівень 1 — Базовий

**Варіативна поведінка:** експорт нотаток у різних форматах (Plain Text, CSV).

- `NoteExporter` — інтерфейс з методами `export(Note)` та `exportAll(Note[], int)`.
- `PlainTextExporter` — реалізація №1: людиночитабельний текстовий формат.
- `CsvExporter` — реалізація №2: CSV формат із простим escape для полів.
- **Поліморфна демонстрація:** у `ExportNotesState` опція «Експортувати в усіх форматах» проходить у циклі по масиву `NoteExporter[]` і викликає як `getFormatName()`, так і `exportAll(notes, count)` для кожного експортера, демонструючи відмінність результатів роботи різних реалізацій одного інтерфейсу.

### Рівень 2 — Середній

- **Default-метод** `getFormatName()` в інтерфейсі `NoteExporter` — за замовчуванням повертає `"Unknown"`, перевизначається у `PlainTextExporter` (`"Plain Text"`) та `CsvExporter` (`"CSV"`).
- **Абстрактний клас** `AbstractNoteExporter` — частково реалізує `NoteExporter`: містить спільну логіку `exportAll()` (цикл по масиву), а `export(Note)` залишається абстрактним. `CsvExporter` перевизначає `exportAll()` для додавання CSV-заголовку, але делегує формування рядків у `super.exportAll()`.

### Рівень 3 — Високий

- **Паттерн Strategy:** клас `ExportContext` приймає `NoteExporter` через конструктор і делегує йому виконання. Метод `setExporter()` дозволяє змінити стратегію експорту під час виконання програми (runtime), без створення нового об'єкта контексту.
- В `ExportNotesState` користувач обирає формат → `setExporter()` підставляє обрану стратегію на об'єкті `exportContext` → `executeExportAll()` виводить результат. Стан залишається відкритим для можливості повторної зміни стратегії на льоту.

## Запуск

### Вимоги

- Java 25+

### З командного рядка

```bash
cd src
javac -encoding UTF-8 com/eugene/notesapp/Main.java
java -Dfile.encoding=UTF-8 com.eugene.notesapp.Main
```

### Через run.bat

```bat
@echo off
chcp 65001 > nul
cd src
javac -encoding UTF-8 com/eugene/notesapp/Main.java
java -Dfile.encoding=UTF-8 com.eugene.notesapp.Main
pause
```
