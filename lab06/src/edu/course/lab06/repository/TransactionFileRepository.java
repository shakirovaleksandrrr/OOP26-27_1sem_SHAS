package edu.course.lab06.repository;

import edu.course.lab06.model.Transaction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Интерфейс файлового репозитория транзакций.
 * В соответствии с принципом единственной ответственности (SRP) метод пробрасывает
 * проверяемое исключение IOException, не превращая его внутри репозитория в текстовые заглушки.
 */
public interface TransactionFileRepository {

    /**
     * Загружает список транзакций из файла.
     *
     * @param path путь к файлу
     * @return список транзакций
     * @throws IOException при ошибках ввода-вывода или отсутствии файла
     */
    List<Transaction> load(Path path) throws IOException;

    /**
     * Сохраняет список транзакций в файл.
     *
     * @param path         путь к файлу
     * @param transactions сохраняемый список
     * @throws IOException при сбоях записи или создании директорий
     */
    void save(Path path, List<Transaction> transactions) throws IOException;
}
