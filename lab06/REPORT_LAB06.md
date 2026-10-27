ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 6
Дисциплина: Современные технологии объектно-ориентированного программирования
Тема: Файлы, потоки, NIO и кодировки. Потоки ввода-вывода и персистентность данных

1. Концепция ввода-вывода в Java
1.1. Байтовые и символьные потоки
Каркас ввода-вывода Java (I/O) разделен на две параллельные ветви:
1. **Байтовые потоки (`InputStream` / `OutputStream`)**: оперируют сырыми 8-битными байтами. Предназначены для любых двоичных данных: изображений (PNG, JPEG), аудиофайлов, скомпилированного байт-кода (`.class`), архивов (`.zip`) и сетевых пакетов.
2. **Символьные потоки (`Reader` / `Writer`)**: оперируют 16-битными символами Unicode (`char`). Предназначены исключительно для текстовых данных. Они используют таблицу кодировки (`Charset`) для преобразования потока байтов в символы и обратно.

Контрольный вопрос: В чем различие между java.io.File и java.nio.file.Path / Files?
Класс `File` (Java 1.0) устарел: многие его методы возвращают `false` при сбоях вместо выброса `IOException`, он плохо поддерживает символические ссылки, метаданные POSIX, не атомарен и работает в блокирующем режиме.
Интерфейс `Path` и утилитный класс `Files` (Java 7 NIO.2) представляют собой современный стандарт: они выбрасывают точные информативные исключения (`NoSuchFileException`, `AccessDeniedException`), поддерживают атомарные операции файловой системы, работу с атрибутами и позволяют явно указывать кодировку `Charset`.

1.2. Роль буферизированных потоков (BufferedReader / BufferedWriter)
Чтение или запись по одному байту/символу приводит к тому, что каждый вызов `read()` или `write()` инициирует дорогостоящий системный вызов к ядру операционной системы (Syscall) и физическое обращение к диску.
Буферизированные классы (`BufferedReader`, `BufferedWriter`, `BufferedInputStream`) создают внутренний массив в оперативной памяти (обычно 8192 байта). Они считывают данные с диска крупными блоками за один системный вызов, уменьшая накладные расходы процессора в сотни раз.

2. Экспериментальные исследования
2.1. Эксперимент 1: «Байты или символы» (копирование бинарного файла)
В ходе эксперимента проводилось копирование бинарного файла с сигнатурой PNG-изображения (магические байты `0x89`, `'P'`, `'N'`, `'G'`, `0x0D`, `0x0A`, `0x1A`, `0x0A`):
- Копирование через `InputStream` / `OutputStream`: файл скопирован байт в байт идентично оригиналу (20 байт).
- Копирование через `Reader` / `Writer` (символьные потоки): файл необратимо поврежден (размер увеличился с 20 до 30 байт).
**Причина сбоя**: Символьный поток интерпретирует сырые байты как символы в кодировке UTF-8. Невалидные байтовые последовательности (которыми изобилуют двоичные форматы) не имеют соответствий в Unicode и принудительно заменяются символом замены `\uFFFD` (Replacement Character: `EF BF BD`), разрушая бинарные заголовки и структуру файла.

2.2. Эксперимент 2: Несоответствие кодировок (UTF-8 vs ISO-8859-1)
Тестовая кириллическая строка: `Доход;Расход;Кофе;Счет`
- Запись произведена в кодировке `UTF-8`.
- При попытке прочитать файл в кодировке `ISO-8859-1` (Latin-1) получен искаженный текст («кракозябры» / mojibake): `Р”РѕС…РѕРґ;Р Р°СЃС…РѕРґ;РљРѕС„Рµ;РЎС‡РµС‚`.
- При явном указании `StandardCharsets.UTF_8` текст прочитан корректно: `Доход;Расход;Кофе;Счет`.
**Причина искажения**: В UTF-8 каждый кириллический символ кодируется 2 байтами (например, буква 'Д' кодируется байтами `0xD0 0x94`). Однобайтовая кодировка `ISO-8859-1` рассматривает каждый из этих двух байтов как отдельный западноевропейский символ ('Р' и '”'), порождая кракозябры.

3. Современный API работы с файловой системой (NIO.2)
3.1. Шесть ключевых операций с Path и Files:
1. `Files.createDirectories(path)` — создание директорий со всеми промежуточными уровнями.
2. `Files.exists(path)` — проверка физического присутствия файла на накопителе.
3. `Files.writeString(path, text, charset)` — удобная запись текстовой строки.
4. `Files.size(path)` — определение точного размера файла в байтах.
5. `Files.readString(path, charset)` — быстрое чтение содержимого текстового файла.
6. `Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)` — атомарное копирование файла.

3.2. Сравнительный анализ File и Path/Files

| Характеристика | Классический `java.io.File` | Современный `java.nio.file.Path / Files` |
| :--- | :--- | :--- |
| Версия появления | Java 1.0 (устаревший API) | Java 7 (NIO.2, современный стандарт) |
| Обработка ошибок | Возвращает `false` (причина сбоя неизвестна) | Выбрасывает точные типы `IOException` |
| Атомарность операций | Отсутствует | Поддерживает атомарное перемещение и копирование |
| Кодировки текста | Использует платформозависимый default | Позволяет явно указывать `StandardCharsets.UTF_8` |
| Производительность | Блокирующий ввод-вывод | Масштабируемый буферизированный ввод-вывод |

4. Практикум: Сериализация и десериализация CSV (TransactionFileRepository)
4.1. Описание формата хранения
Для сохранения транзакций выбран текстовый формат CSV с разделителем точка с запятой (`;`).
Формат заголовка: `id;date;type;amount;category;description`
Пример строки:
`550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;Питание;Бизнес-ланч в кафе`
*Важное замечание*: В рамках учебной лабораторной работы символ `;` внутри строковых значений полей запрещен. Полноценный CSV-парсер корпоративного уровня требует обработки кавычек, экранирования символов и многострочных полей (например, библиотеки Apache Commons CSV).

4.2. Автоматическое управление ресурсами: try-with-resources
Вся работа с файловыми потоками реализована через конструкцию `try-with-resources`:
```java
try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
    // чтение строк
}
```
Виртуальная машина JVM гарантирует закрытие потока методом `close()` даже при возникновении непредвиденных исключений, исключая утечку дескрипторов файлов в операционной системе.

5. Программная реализация
5.1. Интерфейс TransactionFileRepository.java
```java
package edu.course.lab06.repository;

import edu.course.lab06.model.Transaction;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface TransactionFileRepository {
    List<Transaction> load(Path path) throws IOException;
    void save(Path path, List<Transaction> transactions) throws IOException;
}
```

5.2. Реализация CsvTransactionFileRepository.java
```java
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

public class CsvTransactionFileRepository implements TransactionFileRepository {

    private static final String HEADER = "id;date;type;amount;category;description";
    private static final String DELIMITER = ";";

    @Override
    public void save(Path path, List<Transaction> transactions) throws IOException {
        if (path == null) throw new IllegalArgumentException("Путь не может быть null");
        if (transactions == null) throw new IllegalArgumentException("Список не может быть null");

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

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
        if (path == null) throw new IllegalArgumentException("Путь не может быть null");
        if (!Files.exists(path)) {
            throw new NoSuchFileException("Файл не найден: " + path.toAbsolutePath());
        }

        List<Transaction> result = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) return result;

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] tokens = line.split(DELIMITER, -1);
                if (tokens.length < 6) {
                    throw new IOException(String.format("Ошибка CSV в строке %d: ожидалось 6 полей", lineNum));
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
                    throw new IOException("Некорректные данные в строке " + lineNum, ex);
                }
            }
        }
        return result;
    }
}
```

5.3. Главный класс приложения FileApp.java и консольный вывод
```text
==================================================================
 ЛАБОРАТОРНАЯ РАБОТА № 6: ВВОД-ВЫВОД В JAVA, NIO И КОДИРОВКИ      
==================================================================

--- 1. Сохранение транзакций в CSV файл: workspace_lab06\data\transactions.csv ---
Успешно записано строк в файл (включая заголовок): 4
Размер созданного файла: 391 байт

Содержимое CSV файла на диске:
  id;date;type;amount;category;description
  550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;Питание;Бизнес-ланч в кафе
  660e8400-e29b-41d4-a716-446655440001;2026-09-02;INCOME;85000.00;Зарплата;Аванс за сентябрь
  770e8400-e29b-41d4-a716-446655440002;2026-09-03;TRANSFER;12500.00;Переводы;Оплата аренды жилья

--- 2. Загрузка транзакций из CSV файла (Round-trip) ---
Успешно прочитано 3 транзакций:
  [550e8400 | 2026-09-01 | Расход   |     750.00 руб. | Питание      | Бизнес-ланч в кафе]
  [660e8400 | 2026-09-02 | Доход    |   85000.00 руб. | Зарплата     | Аванс за сентябрь]
  [770e8400 | 2026-09-03 | Перевод  |   12500.00 руб. | Переводы     | Оплата аренды жилья]
Проверка совпадения (original.equals(loaded)): true

==================================================================
 ДЕМОНСТРАЦИЯ: 6 ключевых операций с современным NIO (Path/Files) 
==================================================================
1. Создание директории:            C:\Users\sanya\.gemini\antigravity\scratch\lab06\workspace_lab06\data
2. Проверка существования файла до записи: Files.exists = false
3. Запись в файл:                  transactions_sample.txt
4. Проверка существования после записи:   Files.exists = true
   Размер файла:                   69 байт
5. Чтение данных (Files.readString): 'Пример данных транзакции в формате NIO'
6. Копирование файла:              transactions_sample.bak (существует: true)

==================================================================
 ЭКСПЕРИМЕНТ 1: Байты или символы (Stream vs Reader/Writer)       
==================================================================
1. Копирование бинарного файла через InputStream / OutputStream:
   * Байт в байт идентичен оригиналу: true (байты не искажены)

2. Попытка скопировать бинарный файл через Reader / Writer (символьные потоки):
   * Данные повреждены (corrupted):   true
   * Исходный размер: 20 байт -> После Reader/Writer: 30 байт
   * Объяснение: Символьные потоки пытаются декодировать произвольные байты в Unicode-символы.
     Невалидные байтовые последовательности заменяются на символ замены (\uFFFD), разрушая бинарный формат.

==================================================================
 ЭКСПЕРИМЕНТ 2: Несоответствие кодировок (UTF-8 vs ISO-8859-1)    
==================================================================
Исходная строка:                 Доход;Расход;Кофе;Счет
Чтение через ISO-8859-1 (ошибка): Р”РѕС…РѕРґ;Р Р°СЃС…РѕРґ;РљРѕС„Рµ;РЎС‡РµС‚ [Кракозябры / Mojibake]
Чтение через UTF-8 (исправлено):  Доход;Расход;Кофе;Счет [Успешное декодирование]
Причина: В UTF-8 кириллические буквы кодируются 2 байтами. Однобайтовая кодировка
ISO-8859-1 интерпретирует каждый из двух байтов как отдельный западноевропейский символ.
```

6. Принцип единственной ответственности (SRP) и обработка IOException
В соответствии с принципом единственной ответственности:
- Репозиторий `CsvTransactionFileRepository` не подавляет исключения и не выводит пользователю текст ошибок через `System.out.println`. Он объявляет `throws IOException`, передавая управление вызывающей стороне.
- Пользовательский слой (`FileApp`) перехватывает `IOException`, логирует техническую информацию через `Logger.log(Level.SEVERE, ...)` и предоставляет пользователю понятную инструкцию (проверить наличие свободного места на диске и права доступа).

7. Исследование байт-кода и механизм подавленных исключений (Suppressed Exceptions)
Команды компиляции и сборки:
```powershell
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java src).FullName
javap -classpath out -c -p edu.course.lab06.repository.CsvTransactionFileRepository
jar --create --file lab06.jar --main-class edu.course.lab06.FileApp -C out .
java -jar lab06.jar
```

Ключевые инструкции байт-кода:
`Throwable.addSuppressed(Throwable)` — специальный механизм Java 7, используемый компилятором для разворачивания конструкции `try-with-resources`. Если исключение возникло и в теле блока `try`, и при вызове `close()`, закрывающее исключение не теряется, а добавляется в список «подавленных» (`Suppressed`) к основному исключению.
Фрагмент байт-кода метода `load`:
```text
301: aload_3
302: invokevirtual #151  // Method java/io/BufferedReader.close:()V
...
327: invokevirtual #121  // Method java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V
```

8. Ответы на контрольные вопросы к защите
Байтовые и символьные потоки: в чем различия и что выбрать?
Байтовые потоки (`InputStream`/`OutputStream`) передают необработанные 8-битные байты и используются для бинарных файлов (PNG, PDF, ZIP, сетевой сокет). Символьные потоки (`Reader`/`Writer`) предназначены для текста и выполняют перекодирование байтов в 16-битные символы `char` по заданной кодировке.

Зачем используются буферизированные потоки (Buffered*)?
Они накапливают данные во внутреннем массиве памяти (буфере), заменяя тысячи медленных обращений к ядру ОС и накопителю несколькими крупными блочными операциями ввода-вывода.

Как работают File/Paths/Files и чем подход NIO отличается от классического I/O?
`File` — устаревший блокирующий класс, методы которого возвращают `false` при сбоях. `Path` и `Files` (NIO.2) — современный API, поддерживающий атомарные операции, символические ссылки, гибкую работу с атрибутами файлов и выброс точных исключений при ошибках.

Что такое кодировка и какие проблемы она вызывает при чтении/записи текстов?
Кодировка (`Charset`) — это правило сопоставления между последовательностью байтов и символами текста. Несоответствие кодировок при записи и чтении приводит к «кракозябрам» (mojibake) или потере данных из-за подстановки знаков `?` или `\uFFFD`. Стандартом де-факто в современном ПО является кодировка `UTF-8`.

Как корректно обрабатывать IOException?
Не глушить исключение в пустых блоках `catch`, освобождать дескрипторы файлов с помощью `try-with-resources`, регистрировать сбой через `Logger` и преобразовывать техническую ошибку в понятное для пользователя сообщение на уровне интерфейса.
