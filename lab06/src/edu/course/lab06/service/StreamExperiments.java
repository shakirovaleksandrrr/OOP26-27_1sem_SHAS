package edu.course.lab06.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Модуль проведения экспериментов по вводу-выводу:
 * 1. Эксперимент «Байты или символы»: копирование текста vs бинарного файла;
 * 2. Эксперимент с кодировками: возникновение кракозябр (mojibake) при несовпадении Charset.
 */
public class StreamExperiments {

    /**
     * Эксперимент 1: Байты или символы.
     * Показывает, почему произвольные бинарные данные (например, PNG) нельзя читать как текст.
     */
    public static void runBytesVsCharsExperiment(Path tempDir) throws IOException {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 1: Байты или символы (Stream vs Reader/Writer)       ");
        System.out.println("==================================================================");

        // Имитация заголовка PNG-файла (магические байты 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)
        byte[] originalBinaryData = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
                (byte) 0xFF, (byte) 0xFE, (byte) 0xFD, (byte) 0xFC
        };

        Path binaryFile = tempDir.resolve("sample.bin");
        Files.write(binaryFile, originalBinaryData);

        // 1. Корректное копирование через байтовые потоки InputStream / OutputStream
        Path binCopyValid = tempDir.resolve("sample_copy_stream.bin");
        try (InputStream in = Files.newInputStream(binaryFile);
             OutputStream out = Files.newOutputStream(binCopyValid)) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
        byte[] validCopiedBytes = Files.readAllBytes(binCopyValid);
        boolean streamCopyExact = Arrays.equals(originalBinaryData, validCopiedBytes);

        // 2. Некорректная попытка прочитать и скопировать бинарный файл как текст (Reader / Writer)
        Path binCopyCorrupted = tempDir.resolve("sample_copy_reader.bin");
        try (Reader reader = new InputStreamReader(Files.newInputStream(binaryFile), StandardCharsets.UTF_8);
             Writer writer = new OutputStreamWriter(Files.newOutputStream(binCopyCorrupted), StandardCharsets.UTF_8)) {
            char[] cbuf = new char[1024];
            int read;
            while ((read = reader.read(cbuf)) != -1) {
                writer.write(cbuf, 0, read);
            }
        }
        byte[] corruptedBytes = Files.readAllBytes(binCopyCorrupted);
        boolean readerCorrupted = !Arrays.equals(originalBinaryData, corruptedBytes);

        System.out.println("1. Копирование бинарного файла через InputStream / OutputStream:");
        System.out.println("   * Байт в байт идентичен оригиналу: " + streamCopyExact + " (байты не искажены)");

        System.out.println("\n2. Попытка скопировать бинарный файл через Reader / Writer (символьные потоки):");
        System.out.println("   * Данные повреждены (corrupted):   " + readerCorrupted);
        System.out.printf("   * Исходный размер: %d байт -> После Reader/Writer: %d байт%n",
                originalBinaryData.length, corruptedBytes.length);
        System.out.println("   * Объяснение: Символьные потоки пытаются декодировать произвольные байты в Unicode-символы.");
        System.out.println("     Невалидные байтовые последовательности заменяются на символ замены (\\uFFFD), разрушая бинарный формат.");
    }

    /**
     * Эксперимент 2: Эксперимент с кодировками (UTF-8 vs ISO-8859-1).
     */
    public static void runEncodingExperiment(Path tempDir) throws IOException {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 2: Несоответствие кодировок (UTF-8 vs ISO-8859-1)    ");
        System.out.println("==================================================================");

        String sampleText = "Доход;Расход;Кофе;Счет";
        Path utf8File = tempDir.resolve("encoding_test.txt");

        // Записываем файл строго в UTF-8
        Files.writeString(utf8File, sampleText, StandardCharsets.UTF_8);

        // 1. Читаем с неверной кодировкой (ISO-8859-1 / Latin-1)
        String readWithIso;
        try (BufferedReader reader = Files.newBufferedReader(utf8File, StandardCharsets.ISO_8859_1)) {
            readWithIso = reader.readLine();
        }

        // 2. Читаем с правильной кодировкой (UTF-8)
        String readWithUtf8;
        try (BufferedReader reader = Files.newBufferedReader(utf8File, StandardCharsets.UTF_8)) {
            readWithUtf8 = reader.readLine();
        }

        System.out.println("Исходная строка:                 " + sampleText);
        System.out.println("Чтение через ISO-8859-1 (ошибка): " + readWithIso + " [Кракозябры / Mojibake]");
        System.out.println("Чтение через UTF-8 (исправлено):  " + readWithUtf8 + " [Успешное декодирование]");
        System.out.println("Причина: В UTF-8 кириллические буквы кодируются 2 байтами. Однобайтовая кодировка");
        System.out.println("ISO-8859-1 интерпретирует каждый из двух байтов как отдельный западноевропейский символ.");
    }
}
