import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def create_report():
    doc = Document()

    for section in doc.sections:
        section.top_margin = Inches(0.79)
        section.bottom_margin = Inches(0.79)
        section.left_margin = Inches(0.79)
        section.right_margin = Inches(0.79)

    normal_style = doc.styles['Normal']
    normal_style.font.name = 'Times New Roman'
    normal_style.font.size = Pt(12)
    normal_style.font.color.rgb = RGBColor(30, 30, 30)

    def set_cell_background(cell, fill_hex):
        shading_elm = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
        cell._tc.get_or_add_tcPr().append(shading_elm)

    def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
        tcPr = cell._tc.get_or_add_tcPr()
        tcMar = OxmlElement('w:tcMar')
        for margin, value in [('w:top', top), ('w:bottom', bottom), ('w:left', left), ('w:right', right)]:
            node = OxmlElement(margin)
            node.set(qn('w:w'), str(value))
            node.set(qn('w:type'), 'dxa')
            tcMar.append(node)
        tcPr.append(tcMar)

    def add_title(text):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(4)
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(16)
        run.font.color.rgb = RGBColor(15, 32, 67)

    def add_subtitle(discipline, topic):
        p = doc.add_paragraph()
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p.paragraph_format.space_after = Pt(14)
        r1 = p.add_run("Дисциплина: ")
        r1.bold = True
        p.add_run(discipline + "\n")
        r2 = p.add_run("Тема: ")
        r2.bold = True
        p.add_run(topic)

    def add_h1(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(14)
        p.paragraph_format.space_after = Pt(4)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(14)
        run.font.color.rgb = RGBColor(16, 44, 87)

    def add_h2(text):
        p = doc.add_paragraph()
        p.paragraph_format.space_before = Pt(10)
        p.paragraph_format.space_after = Pt(3)
        p.paragraph_format.keep_with_next = True
        run = p.add_run(text)
        run.bold = True
        run.font.size = Pt(12.5)
        run.font.color.rgb = RGBColor(30, 70, 130)

    def add_p(text, bold_prefix=None, space_after=4):
        p = doc.add_paragraph()
        p.paragraph_format.space_after = Pt(space_after)
        p.paragraph_format.line_spacing = 1.15
        if bold_prefix:
            r_bold = p.add_run(bold_prefix)
            r_bold.bold = True
        p.add_run(text)
        return p

    def add_code_block(code_text):
        table = doc.add_table(rows=1, cols=1)
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        cell = table.cell(0, 0)
        cell.width = Inches(6.8)
        set_cell_background(cell, "F6F8FA")
        set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
        
        tcPr = cell._tc.get_or_add_tcPr()
        borders = parse_xml(
            f'<w:tcBorders {nsdecls("w")}>\n'
            f'  <w:top w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>\n'
            f'  <w:left w:val="single" w:sz="18" w:space="0" w:color="2563EB"/>\n'
            f'  <w:bottom w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>\n'
            f'  <w:right w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>\n'
            f'</w:tcBorders>'
        )
        tcPr.append(borders)
        
        p = cell.paragraphs[0]
        p.paragraph_format.space_before = Pt(2)
        p.paragraph_format.space_after = Pt(2)
        p.paragraph_format.line_spacing = 1.05
        
        run = p.add_run(code_text.strip())
        run.font.name = 'Consolas'
        run.font.size = Pt(9.5)
        run.font.color.rgb = RGBColor(31, 35, 40)
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    def add_styled_table(headers, rows_data, col_widths=None):
        table = doc.add_table(rows=len(rows_data) + 1, cols=len(headers))
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        
        hdr_cells = table.rows[0].cells
        for i, header_text in enumerate(headers):
            cell = hdr_cells[i]
            set_cell_background(cell, "1E3A8A")
            set_cell_margins(cell, top=120, bottom=120, left=150, right=150)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.CENTER
            p.paragraph_format.space_before = Pt(2)
            p.paragraph_format.space_after = Pt(2)
            run = p.add_run(header_text)
            run.bold = True
            run.font.name = 'Times New Roman'
            run.font.size = Pt(10.5)
            run.font.color.rgb = RGBColor(255, 255, 255)
            
        for r_idx, row_values in enumerate(rows_data):
            row_cells = table.rows[r_idx + 1].cells
            bg_color = "F8FAFC" if r_idx % 2 == 1 else "FFFFFF"
            for c_idx, val in enumerate(row_values):
                cell = row_cells[c_idx]
                set_cell_background(cell, bg_color)
                set_cell_margins(cell, top=100, bottom=100, left=150, right=150)
                p = cell.paragraphs[0]
                p.paragraph_format.space_before = Pt(2)
                p.paragraph_format.space_after = Pt(2)
                p.paragraph_format.line_spacing = 1.1
                run = p.add_run(val)
                run.font.name = 'Times New Roman'
                run.font.size = Pt(10)
                
        if col_widths:
            for row in table.rows:
                for idx, w in enumerate(col_widths):
                    row.cells[idx].width = Inches(w)
                    
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ==================== ТЕЛО ДОКУМЕНТА ====================

    add_title("ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 6")
    add_subtitle(
        "Современные технологии объектно-ориентированного программирования",
        "Файлы, потоки, NIO и кодировки. Потоки ввода-вывода и персистентность данных"
    )

    # 1. Концепция
    add_h1("1. Концепция ввода-вывода в Java")
    add_h2("1.1. Байтовые и символьные потоки")
    add_p(
        "Каркас ввода-вывода Java (I/O) делится на две параллельные ветви:\n"
        "1. Байтовые потоки (InputStream / OutputStream) — оперируют 8-битными байтами. Предназначены для двоичных данных: изображений, видео, скомпилированного байт-кода, архивов.\n"
        "2. Символьные потоки (Reader / Writer) — оперируют 16-битными символами Unicode (char). Предназначены для текстовых файлов и преобразуют байты в символы на основе Charset."
    )

    add_p(
        "Класс File (Java 1.0) устарел: многие его методы возвращают false при сбоях вместо выброса исключений, он не поддерживает атомарность и символические ссылки.\n"
        "Интерфейс Path и утилитный класс Files (Java 7 NIO.2) представляют собой современный стандарт: они выбрасывают точные исключения (NoSuchFileException), поддерживают атомарные операции и позволяют явно указывать кодировку Charset.",
        bold_prefix="Контрольный вопрос: В чем различие между File и Path/Files?\n"
    )

    add_h2("1.2. Роль буферизированных потоков (BufferedReader / BufferedWriter)")
    add_p(
        "Побайтное чтение инициирует системный вызов к ядру ОС и физическое обращение к диску на каждый байт.\n"
        "Буферизированные классы накапливают данные в памяти (буфер 8 КБ), выполняя чтение/запись крупными блоками за один системный вызов."
    )

    # 2. Эксперименты
    add_h1("2. Экспериментальные исследования")
    add_h2("2.1. Эксперимент 1: «Байты или символы» (копирование бинарного файла)")
    add_p(
        "В ходе эксперимента копировался бинарный файл с сигнатурой PNG-файла:\n"
        "• Копирование через InputStream / OutputStream: данные скопированы байт в байт идентично оригиналу (20 байт).\n"
        "• Копирование через Reader / Writer (символьные потоки): файл необратимо поврежден (размер увеличился до 30 байт).\n"
        "Причина: Символьный поток интерпретирует сырые байты как символы UTF-8. Невалидные байтовые комбинации заменяются на символ замены \\uFFFD, разрушая бинарный файл."
    )

    add_h2("2.2. Эксперимент 2: Несоответствие кодировок (UTF-8 vs ISO-8859-1)")
    add_p(
        "Тестовая строка: 'Доход;Расход;Кофе;Счет' записана в UTF-8.\n"
        "• Чтение в ISO-8859-1 порождает искажения (кракозябры / mojibake): 'Р”РѕС…РѕРґ;Р Р°СЃС…РѕРґ;РљРѕС„Рµ;РЎС‡РµС‚'.\n"
        "• Чтение в UTF-8: текст декодируется идеально.\n"
        "Причина: В UTF-8 кириллические буквы занимают 2 байта. Однобайтовая кодировка ISO-8859-1 читает каждый байт как отдельный западноевропейский символ."
    )

    # 3. NIO.2
    add_h1("3. Современный API работы с файловой системой (NIO.2)")
    add_styled_table(
        ["Характеристика", "Классический java.io.File", "Современный java.nio.file.Path / Files"],
        [
            ["Версия появления", "Java 1.0 (устаревший API)", "Java 7 (NIO.2, современный стандарт)"],
            ["Обработка ошибок", "Возвращает false (причина сбоя неизвестна)", "Выбрасывает точные типы NoSuchFileException / AccessDeniedException"],
            ["Атомарность", "Отсутствует", "Поддерживает атомарные операции переноса и копирования"],
            ["Кодировки текста", "Использует системный default", "Позволяет явно указывать StandardCharsets.UTF_8"],
            ["Производительность", "Блокирующий ввод-вывод", "Масштабируемый буферизированный ввод-вывод"]
        ],
        col_widths=[1.8, 2.5, 2.5]
    )

    # 4. Практикум
    add_h1("4. Практикум: Сериализация и десериализация CSV (TransactionFileRepository)")
    add_p(
        "Формат CSV: id;date;type;amount;category;description\n"
        "Пример: 550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;Питание;Бизнес-ланч в кафе\n"
        "Упрощенный формат: символ ';' внутри значений полей запрещен. Промышленный парсинг требует экранирования кавычек."
    )

    add_p(
        "Конструкция try-with-resources автоматически вызывает метод close() у ресурсов, реализующих AutoCloseable, "
        "гарантируя своевременное освобождение дескрипторов файлов ОС даже при возникновении сбоев.",
        bold_prefix="Автоматическое управление ресурсами: try-with-resources\n"
    )

    # 5. Реализация
    add_h1("5. Программная реализация")
    add_h2("5.1. Интерфейс TransactionFileRepository.java")
    add_code_block(
        "package edu.course.lab06.repository;\n\n"
        "import edu.course.lab06.model.Transaction;\nimport java.io.IOException;\nimport java.nio.file.Path;\nimport java.util.List;\n\n"
        "public interface TransactionFileRepository {\n"
        "    List<Transaction> load(Path path) throws IOException;\n"
        "    void save(Path path, List<Transaction> transactions) throws IOException;\n"
        "}"
    )

    add_h2("5.2. Реализация CsvTransactionFileRepository.java")
    add_code_block(
        "package edu.course.lab06.repository;\n\n"
        "import edu.course.lab06.model.*;\nimport java.io.*;\nimport java.math.BigDecimal;\n"
        "import java.nio.charset.StandardCharsets;\nimport java.nio.file.*;\nimport java.time.LocalDate;\nimport java.util.*;\n\n"
        "public class CsvTransactionFileRepository implements TransactionFileRepository {\n"
        "    private static final String HEADER = \"id;date;type;amount;category;description\";\n\n"
        "    @Override public void save(Path path, List<Transaction> transactions) throws IOException {\n"
        "        Path parent = path.getParent();\n"
        "        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);\n\n"
        "        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {\n"
        "            writer.write(HEADER);\n"
        "            writer.newLine();\n"
        "            for (Transaction tx : transactions) {\n"
        "                writer.write(String.format(\"%s;%s;%s;%s;%s;%s\",\n"
        "                        tx.getId(), tx.getDate(), tx.getType().name(), tx.getAmount(), tx.getCategory(), tx.getDescription()));\n"
        "                writer.newLine();\n"
        "            }\n"
        "        }\n"
        "    }\n\n"
        "    @Override public List<Transaction> load(Path path) throws IOException {\n"
        "        if (!Files.exists(path)) throw new NoSuchFileException(\"Файл не найден: \" + path);\n"
        "        List<Transaction> result = new ArrayList<>();\n"
        "        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {\n"
        "            String header = reader.readLine();\n"
        "            String line;\n"
        "            while ((line = reader.readLine()) != null) {\n"
        "                if (line.isBlank()) continue;\n"
        "                String[] t = line.split(\";\", -1);\n"
        "                result.add(new Transaction(UUID.fromString(t[0]), LocalDate.parse(t[1]),\n"
        "                        TransactionType.valueOf(t[2]), new BigDecimal(t[3]), t[4], t[5]));\n"
        "            }\n"
        "        }\n"
        "        return result;\n"
        "    }\n"
        "}"
    )

    add_h2("5.3. Главный класс FileApp.java и консольный вывод")
    add_code_block(
        "==================================================================\n"
        " ЛАБОРАТОРНАЯ РАБОТА № 6: ВВОД-ВЫВОД В JAVA, NIO И КОДИРОВКИ      \n"
        "==================================================================\n\n"
        "--- 1. Сохранение транзакций в CSV файл: workspace_lab06\\data\\transactions.csv ---\n"
        "Успешно записано строк в файл (включая заголовок): 4\n"
        "Размер созданного файла: 391 байт\n\n"
        "Содержимое CSV файла на диске:\n"
        "  id;date;type;amount;category;description\n"
        "  550e8400-e29b-41d4-a716-446655440000;2026-09-01;EXPENSE;750.00;Питание;Бизнес-ланч в кафе\n"
        "  660e8400-e29b-41d4-a716-446655440001;2026-09-02;INCOME;85000.00;Зарплата;Аванс за сентябрь\n"
        "  770e8400-e29b-41d4-a716-446655440002;2026-09-03;TRANSFER;12500.00;Переводы;Оплата аренды жилья\n\n"
        "--- 2. Загрузка транзакций из CSV файла (Round-trip) ---\n"
        "Успешно прочитано 3 транзакций:\n"
        "  [550e8400 | 2026-09-01 | Расход   |     750.00 руб. | Питание      | Бизнес-ланч в кафе]\n"
        "  [660e8400 | 2026-09-02 | Доход    |   85000.00 руб. | Зарплата     | Аванс за сентябрь]\n"
        "  [770e8400 | 2026-09-03 | Перевод  |   12500.00 руб. | Переводы     | Оплата аренды жилья]\n"
        "Проверка совпадения (original.equals(loaded)): true\n\n"
        "==================================================================\n"
        " ДЕМОНСТРАЦИЯ: 6 ключевых операций с современным NIO (Path/Files) \n"
        "==================================================================\n"
        "1. Создание директории:            C:\\workspace_lab06\\data\n"
        "2. Проверка существования файла до записи: Files.exists = false\n"
        "3. Запись в файл:                  transactions_sample.txt\n"
        "4. Проверка существования после записи:   Files.exists = true\n"
        "   Размер файла:                   69 байт\n"
        "5. Чтение данных (Files.readString): 'Пример данных транзакции в формате NIO'\n"
        "6. Копирование файла:              transactions_sample.bak (существует: true)\n\n"
        "==================================================================\n"
        " ЭКСПЕРИМЕНТ 1: Байты или символы (Stream vs Reader/Writer)       \n"
        "==================================================================\n"
        "1. Копирование бинарного файла через InputStream / OutputStream:\n"
        "   * Байт в байт идентичен оригиналу: true (байты не искажены)\n"
        "2. Попытка скопировать бинарный файл через Reader / Writer (символьные потоки):\n"
        "   * Данные повреждены (corrupted):   true\n"
        "   * Исходный размер: 20 байт -> После Reader/Writer: 30 байт\n\n"
        "==================================================================\n"
        " ЭКСПЕРИМЕНТ 2: Несоответствие кодировок (UTF-8 vs ISO-8859-1)    \n"
        "==================================================================\n"
        "Исходная строка:                 Доход;Расход;Кофе;Счет\n"
        "Чтение через ISO-8859-1 (ошибка): Р”РѕС…РѕРґ;Р Р°СЃС…РѕРґ;РљРѕС„Рµ;РЎС‡РµС‚ [Кракозябры / Mojibake]\n"
        "Чтение через UTF-8 (исправлено):  Доход;Расход;Кофе;Счет [Успешное декодирование]"
    )

    # 6. SRP
    add_h1("6. Принцип единственной ответственности (SRP) и обработка IOException")
    add_p(
        "Репозиторий не перехватывает IOException и не глушит его, а объявляет throws IOException. "
        "UI-контроллер приложения перехватывает исключение, регистрирует сбой через Logger.log(Level.SEVERE, ...) "
        "и предоставляет пользователю полезный совет (проверить наличие свободного места и права доступа)."
    )

    # 7. Байт-код
    add_h1("7. Исследование байт-кода и механизм подавленных исключений (addSuppressed)")
    add_p("Команды сборки и дизассемблирования:")
    add_code_block(
        "mkdir -p out\n"
        "javac -encoding UTF-8 -d out src/edu/course/lab06/model/*.java src/edu/course/lab06/repository/*.java src/edu/course/lab06/service/*.java src/edu/course/lab06/*.java\n"
        "javap -classpath out -c -p edu.course.lab06.repository.CsvTransactionFileRepository\n"
        "jar --create --file lab06.jar --main-class edu.course.lab06.FileApp -C out .\n"
        "java -jar lab06.jar"
    )

    add_p(
        "В байт-коде try-with-resources автоматически генерирует вызов Throwable.addSuppressed(Throwable), "
        "чтобы исключение закрытия close() не скрывало основное исключение блока try:",
        bold_prefix="Ключевые инструкции байт-кода:\n"
    )
    add_code_block(
        "302: invokevirtual #151  // Method java/io/BufferedReader.close:()V\n"
        "327: invokevirtual #121  // Method java/lang/Throwable.addSuppressed:(Ljava/lang/Throwable;)V"
    )

    # 8. Вопросы
    add_h1("8. Ответы на контрольные вопросы к защите")
    add_p(
        "Байтовые потоки (InputStream/OutputStream) работают с 8-битными байтами для любых двоичных данных (PNG, ZIP, сетевые пакеты). "
        "Символьные потоки (Reader/Writer) работают с 16-битными символами Unicode для текстовых файлов с преобразованием через Charset.",
        bold_prefix="Байтовые и символьные потоки: в чем различия и что выбрать?\n"
    )
    add_p(
        "Они накапливают данные в промежуточном массиве памяти (буфере), заменяя тысячи медленных обращений к ядру ОС "
        "несколькими крупными блочными дисковыми операциями.",
        bold_prefix="Зачем используются буферизированные потоки (Buffered*)?\n"
    )
    add_p(
        "File — устаревший блокирующий класс, методы которого возвращают false при сбоях. "
        "Path и Files (NIO.2) — современный API, поддерживающий атомарность, явные кодировки и точные типы исключений.",
        bold_prefix="Как работают File/Paths/Files и чем подход NIO отличается от классического I/O?\n"
    )
    add_p(
        "Кодировка (Charset) — таблица сопоставления между последовательностями байтов и символами текста. "
        "Несовпадение кодировок приводит к появлению кракозябр (mojibake) или потере данных. Стандартом является UTF-8.",
        bold_prefix="Что такое кодировка и какие проблемы она вызывает при чтении/записи текстов?\n"
    )
    add_p(
        "Не глушить исключение в пустых catch, освобождать дескрипторы через try-with-resources, "
        "логировать сбой через Logger и информировать пользователя на уровне UI.",
        bold_prefix="Как корректно обрабатывать IOException?\n"
    )

    output_path = r"C:\Users\sanya\.gemini\antigravity\scratch\lab06\REPORT_LAB06.docx"
    doc.save(output_path)
    print(f"Report saved successfully to {output_path}")

if __name__ == "__main__":
    create_report()
