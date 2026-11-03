package com.eugene.notesapp.ui.state;

import com.eugene.notesapp.Main;
import com.eugene.notesapp.export.ExportContext;
import com.eugene.notesapp.export.NoteExporter;
import com.eugene.notesapp.export.NoteExporterFactory;

import java.util.InputMismatchException;

public class ExportNotesState implements MenuState
{
    private final String[] formatCodes = { "TEXT", "CSV", "LEGACY" };
    private final NoteExporterFactory exporterFactory = new NoteExporterFactory();
    private final NoteExporter[] exporters = exporterFactory.createAll();
    private final ExportContext exportContext = new ExportContext(exporters[0]);

    @Override
    public void handle(MenuContext context)
    {
        IO.println();
        IO.println("=== ЕКСПОРТ НОТАТОК ===");
        IO.println("Оберіть формат експорту:");

        for (int i = 0; i < exporters.length; i++)
        {
            IO.println((i + 1) + ". " + exporters[i].getFormatName());
        }
        int allFormatsChoice = exporters.length + 1;
        IO.println(allFormatsChoice + ". Експортувати в усіх форматах");
        IO.println("0. Повернутися до головного меню");
        IO.print("Оберіть дію: ");

        int choice;
        try
        {
            choice = Main.scanner.nextInt();
            Main.scanner.nextLine();
        }
        catch (InputMismatchException e)
        {
            IO.println("Помилка: введіть число.");
            Main.scanner.nextLine();
            return;
        }

        if (choice == 0)
        {
            context.setState(new MainMenuState());
            return;
        }

        if (choice == allFormatsChoice)
        {
            for (NoteExporter exporter : exporterFactory.createAll())
            {
                IO.println();
                IO.println("--- Формат: " + exporter.getFormatName() + " ---");
                IO.println(exporter.exportAll(Main.notes, Main.count));
            }
            return;
        }

        if (choice < 1 || choice > exporters.length)
        {
            IO.println("Невірне число! Введіть коректне число.");
            return;
        }

        exportContext.setExporter(exporterFactory.create(formatCodes[choice - 1]));

        IO.println();
        IO.println("--- Результат експорту (" + exportContext.getExporter().getFormatName() + ") ---");
        IO.println(exportContext.executeExportAll(Main.notes, Main.count));
    }
}
