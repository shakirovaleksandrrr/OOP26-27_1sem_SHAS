package edu.course.lab06;

import edu.course.lab06.model.Transaction;
import edu.course.lab06.model.TransactionType;
import edu.course.lab06.repository.CsvTransactionFileRepository;
import edu.course.lab06.repository.TransactionFileRepository;
import edu.course.lab06.service.PathOperationsDemo;
import edu.course.lab06.service.StreamExperiments;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Главный класс приложения.
 * Демонстрация файлового ввода-вывода (Java NIO), сериализации в CSV,
 * кодировок и устойчивости к ошибкам ввода-вывода.
 */
public class FileApp {

    private static final Logger logger = Logger.getLogger(FileApp.class.getName());

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 6: ВВОД-ВЫВОД В JAVA, NIO И КОДИРОВКИ      ");
        System.out.println("==================================================================");

        Path appDir = Path.of("workspace_lab06");
        Path dataDir = appDir.resolve("data");
        Path csvFile = dataDir.resolve("transactions.csv");

        TransactionFileRepository repository = new CsvTransactionFileRepository();

        // 1. Создание тестового набора транзакций
        List<Transaction> originalList = List.of(
                new Transaction(
                        UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                        LocalDate.of(2026, 9, 1),
                        TransactionType.EXPENSE,
                        new BigDecimal("750.00"),
                        "Питание",
                        "Бизнес-ланч в кафе"
                ),
                new Transaction(
                        UUID.fromString("660e8400-e29b-41d4-a716-446655440001"),
                        LocalDate.of(2026, 9, 2),
                        TransactionType.INCOME,
                        new BigDecimal("85000.00"),
                        "Зарплата",
                        "Аванс за сентябрь"
                ),
                new Transaction(
                        UUID.fromString("770e8400-e29b-41d4-a716-446655440002"),
                        LocalDate.of(2026, 9, 3),
                        TransactionType.TRANSFER,
                        new BigDecimal("12500.00"),
                        "Переводы",
                        "Оплата аренды жилья"
                )
        );

        try {
            // 2. Сохранение в CSV файл (Save)
            System.out.printf("%n--- 1. Сохранение транзакций в CSV файл: %s ---%n", csvFile);
            repository.save(csvFile, originalList);
            System.out.println("Успешно записано строк в файл (включая заголовок): " + (originalList.size() + 1));
            System.out.println("Размер созданного файла: " + Files.size(csvFile) + " байт");

            // Выведем содержимое созданного CSV
            System.out.println("\nСодержимое CSV файла на диске:");
            List<String> lines = Files.readAllLines(csvFile);
            for (String l : lines) {
                System.out.println("  " + l);
            }

            // 3. Загрузка из CSV файла (Load)
            System.out.printf("%n--- 2. Загрузка транзакций из CSV файла (Round-trip) ---%n");
            List<Transaction> loadedList = repository.load(csvFile);
            System.out.printf("Успешно прочитано %d транзакций:%n", loadedList.size());
            for (Transaction tx : loadedList) {
                System.out.println("  " + tx);
            }

            // Проверка идентичности
            boolean match = originalList.equals(loadedList);
            System.out.println("Проверка совпадения (original.equals(loaded)): " + match);

            // 4. Демонстрация 6 операций Path и Files
            PathOperationsDemo.demonstratePathOperations(appDir);

            // 5. Запуск исследовательских экспериментов
            StreamExperiments.runBytesVsCharsExperiment(appDir);
            StreamExperiments.runEncodingExperiment(appDir);

        } catch (IOException e) {
            // Демонстрация SRP: низкоуровневая ошибка логируется, пользователю выводится понятный совет
            logger.log(Level.SEVERE, "Ошибка файловой системы при работе с Finance Tracker", e);
            System.err.println("  [ОШИБКА ДИСКА] Не удалось выполнить операцию с файлом: " + e.getMessage());
            System.err.println("  [СОВЕТ] Проверьте права доступа к директории и наличие свободного места.");
        }
    }
}
