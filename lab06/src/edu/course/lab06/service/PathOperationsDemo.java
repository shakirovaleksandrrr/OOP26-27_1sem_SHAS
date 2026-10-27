package edu.course.lab06.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Демонстрация ключевых операций с Path и Files:
 * 1. Проверка существования;
 * 2. Создание каталога;
 * 3. Запись;
 * 4. Чтение;
 * 5. Копирование;
 * 6. Получение размера.
 */
public class PathOperationsDemo {

    public static void demonstratePathOperations(Path baseDir) throws IOException {
        System.out.println("\n==================================================================");
        System.out.println(" ДЕМОНСТРАЦИЯ: 6 ключевых операций с современным NIO (Path/Files) ");
        System.out.println("==================================================================");

        Path dataDir = baseDir.resolve("data");
        Path originalFile = dataDir.resolve("transactions_sample.txt");
        Path backupFile = dataDir.resolve("transactions_sample.bak");

        // 1. Создание каталогов
        Files.createDirectories(dataDir);
        System.out.println("1. Создание директории:            " + dataDir.toAbsolutePath());

        // 2. Проверка существования
        System.out.println("2. Проверка существования файла до записи: Files.exists = " + Files.exists(originalFile));

        // 3. Запись текстовых данных
        Files.writeString(originalFile, "Пример данных транзакции в формате NIO\n");
        System.out.println("3. Запись в файл:                  " + originalFile.getFileName());

        // 4. Проверка существования и получение размера
        System.out.println("4. Проверка существования после записи:   Files.exists = " + Files.exists(originalFile));
        System.out.println("   Размер файла:                   " + Files.size(originalFile) + " байт");

        // 5. Чтение данных
        String content = Files.readString(originalFile).trim();
        System.out.println("5. Чтение данных (Files.readString): '" + content + "'");

        // 6. Копирование файла
        Files.copy(originalFile, backupFile, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("6. Копирование файла:              " + backupFile.getFileName() + " (существует: " + Files.exists(backupFile) + ")");
    }
}
