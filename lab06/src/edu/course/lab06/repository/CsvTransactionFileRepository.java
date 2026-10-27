package edu.course.lab06.repository;

import edu.course.lab06.model.Transaction;
import edu.course.lab06.model.TransactionType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация файлового репозитория для формата CSV с разделителем ';'.
 * Использует современный Java NIO API (Files / Path), явную кодировку UTF-8
 * и автоматическое закрытие ресурсов через try-with-resources.
 */
public class CsvTransactionFileRepository implements TransactionFileRepository {

    private static final String HEADER = "id;date;type;amount;category;description";
    private static final String DELIMITER = ";";

    @Override
    public void save(Path path, List<Transaction> transactions) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Путь к файлу не может быть null");
        }
        if (transactions == null) {
            throw new IllegalArgumentException("Список транзакций не может быть null");
        }

        // Создаем родительские каталоги при необходимости
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        // Автоматическое управление буферизированным потоком через try-with-resources
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();

            for (Transaction tx : transactions) {
                String line = String.format("%s%s%s%s%s%s%s%s%s%s%s",
                        tx.getId(), DELIMITER,
                        tx.getDate(), DELIMITER,
                        tx.getType().name(), DELIMITER,
                        tx.getAmount(), DELIMITER,
                        tx.getCategory(), DELIMITER,
                        tx.getDescription());
                writer.write(line);
                writer.newLine();
            }
        }
    }

    @Override
    public List<Transaction> load(Path path) throws IOException {
        if (path == null) {
            throw new IllegalArgumentException("Путь к файлу не может быть null");
        }
        if (!Files.exists(path)) {
            throw new NoSuchFileException("Файл транзакций не существует: " + path.toAbsolutePath());
        }

        List<Transaction> result = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return result; // Пустой файл
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue; // Пропуск пустых строк
                }

                String[] tokens = line.split(DELIMITER, -1);
                if (tokens.length < 6) {
                    throw new IOException(String.format("Ошибка парсинга CSV в строке %d: ожидалось 6 полей, получено %d (%s)",
                            lineNumber, tokens.length, line));
                }

                try {
                    UUID id = UUID.fromString(tokens[0]);
                    LocalDate date = LocalDate.parse(tokens[1]);
                    TransactionType type = TransactionType.valueOf(tokens[2]);
                    BigDecimal amount = new BigDecimal(tokens[3]);
                    String category = tokens[4];
                    String description = tokens[5];

                    result.add(new Transaction(id, date, type, amount, category, description));
                } catch (Exception ex) {
                    throw new IOException(String.format("Некорректный формат данных в строке %d: %s", lineNumber, line), ex);
                }
            }
        }
        return result;
    }
}
