package edu.course.lab06;

import edu.course.lab06.model.Transaction;
import edu.course.lab06.model.TransactionType;
import edu.course.lab06.repository.CsvTransactionFileRepository;
import edu.course.lab06.repository.TransactionFileRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Автоматические верификационные тесты для проверки требований Лабораторной работы № 6:
 * 1. Round-trip: объекты -> CSV -> объекты (полная эквивалентность).
 * 2. Сохранение и корректное чтение русских символов (UTF-8).
 * 3. Автоматическое создание каталогов при отсутствии.
 * 4. Корректная обработка несуществующего файла (NoSuchFileException).
 * 5. Закрытие ресурсов: файл не заблокирован после save/load.
 */
public class FileIoVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) throws IOException {
        System.out.println(">>> Запуск проверочных тестов Лабораторной работы № 6 <<<");

        Path testDir = Files.createTempDirectory("test_lab06_");

        try {
            testRoundTripAndCyrillic(testDir);
            testAutoCreateDirectory(testDir);
            testNonExistentFileThrowsException(testDir);
            testResourceClosingAllowsDeletion(testDir);

            System.out.println("---------------------------------------------------------");
            System.out.printf("Результаты тестов: Всего: %d, Успешно: %d, Провалено: %d%n",
                    testsRun, testsPassed, testsRun - testsPassed);

            if (testsRun != testsPassed) {
                System.err.println("!!! Есть проваленные тесты!");
                System.exit(1);
            } else {
                System.out.println(">>> Все требования лабораторной работы успешно подтверждены! <<<");
            }
        } finally {
            // Очистка временных файлов
            deleteRecursively(testDir);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testRoundTripAndCyrillic(Path tempDir) throws IOException {
        TransactionFileRepository repo = new CsvTransactionFileRepository();
        Path csvFile = tempDir.resolve("test_roundtrip.csv");

        List<Transaction> original = List.of(
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 1), TransactionType.EXPENSE,
                        new BigDecimal("1250.75"), "Рестораны и кофе", "Ужин с коллегами"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 2), TransactionType.INCOME,
                        new BigDecimal("90000.00"), "Зарплата", "Основной доход")
        );

        repo.save(csvFile, original);
        List<Transaction> loaded = repo.load(csvFile);

        assertTrue("Размер загруженного списка равен исходному", original.size() == loaded.size());
        assertTrue("Объекты эквивалентны (round-trip)", original.equals(loaded));
        assertTrue("Кириллические символы сохранены без искажений",
                loaded.get(0).getCategory().equals("Рестораны и кофе") &&
                        loaded.get(0).getDescription().equals("Ужин с коллегами"));
    }

    private static void testAutoCreateDirectory(Path tempDir) throws IOException {
        TransactionFileRepository repo = new CsvTransactionFileRepository();
        Path deeplyNested = tempDir.resolve("nested").resolve("subfolder").resolve("tx.csv");

        assertTrue("Директории до вызова save не существуют", !Files.exists(deeplyNested.getParent()));

        repo.save(deeplyNested, List.of());

        assertTrue("Каталоги автоматически созданы методом save", Files.exists(deeplyNested.getParent()));
        assertTrue("Файл создан", Files.exists(deeplyNested));
    }

    private static void testNonExistentFileThrowsException(Path tempDir) {
        TransactionFileRepository repo = new CsvTransactionFileRepository();
        Path missing = tempDir.resolve("not_found_file.csv");

        boolean threwNoSuchFile = false;
        try {
            repo.load(missing);
        } catch (NoSuchFileException e) {
            threwNoSuchFile = true;
        } catch (IOException e) {
            // Иное
        }

        assertTrue("Попытка загрузки несуществующего файла вызывает NoSuchFileException", threwNoSuchFile);
    }

    private static void testResourceClosingAllowsDeletion(Path tempDir) throws IOException {
        TransactionFileRepository repo = new CsvTransactionFileRepository();
        Path lockTest = tempDir.resolve("lock_test.csv");

        repo.save(lockTest, List.of(
                new Transaction(UUID.randomUUID(), LocalDate.now(), TransactionType.INCOME,
                        new BigDecimal("100.00"), "Категория", "Тест")
        ));
        repo.load(lockTest);

        // Если потоки не закрыты через try-with-resources, в Windows Files.delete вызовет FileSystemException (Sharing violation)
        boolean deletedWithoutException = false;
        try {
            Files.delete(lockTest);
            deletedWithoutException = true;
        } catch (Exception ignored) {}

        assertTrue("Ресурсы освобождены через try-with-resources: файл удаляется без блокировок", deletedWithoutException);
    }

    private static void deleteRecursively(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (var entries = Files.newDirectoryStream(path)) {
                    for (Path entry : entries) deleteRecursively(entry);
                }
            }
            Files.deleteIfExists(path);
        } catch (IOException ignored) {}
    }
}
