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

    add_title("ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 7")
    add_subtitle(
        "Современные технологии объектно-ориентированного программирования",
        "Функциональное программирование в Java: лямбда-выражения и Stream API"
    )

    # 1. Концепция
    add_h1("1. Концепция функционального программирования в Java")
    add_h2("1.1. Лямбда-выражения и функциональные интерфейсы")
    add_p(
        "Лямбда-выражение в Java представляет собой лаконичную форму реализации функционального интерфейса (анонимная функция). "
        "Функциональным называется интерфейс, содержащий ровно один абстрактный метод (SAM — Single Abstract Method)."
    )

    add_p(
        "1. Байт-код: Анонимный класс создает отдельный файл App$1.class на диске. Лямбда компилируется через инструкцию invokedynamic "
        "и связывается во время выполнения через LambdaMetafactory, снижая потребление памяти Metaspace.\n"
        "2. Контекст this: В анонимном классе this ссылается на экземпляр самого внутреннего класса. В лямбда-выражении this "
        "ссылается на внешний окружающий класс (lexical scoping).",
        bold_prefix="Контрольный вопрос: Чем лямбда-выражение отличается от анонимного класса?\n"
    )

    add_h2("1.2. Стандартные функциональные интерфейсы Java")
    add_styled_table(
        ["Интерфейс", "Сигнатура метода", "Назначение", "Пример в Finance Tracker"],
        [
            ["Predicate<T>", "boolean test(T t)", "Проверка логического условия (фильтрация)", "tx -> tx.getType() == EXPENSE"],
            ["Function<T, R>", "R apply(T t)", "Трансформация объекта типа T в тип R", "Transaction::getAmount"],
            ["Consumer<T>", "void accept(T t)", "Потребитель: выполнение действия над объектом", "tx -> System.out.println(tx)"],
            ["Supplier<T>", "T get()", "Поставщик: создание объекта по запросу", "ArrayList::new"]
        ],
        col_widths=[1.5, 1.8, 1.9, 1.8]
    )

    # 2. Архитектура конвейера
    add_h1("2. Архитектура конвейера Stream API")
    add_p(
        "Конвейер Stream строится по формуле: [Источник] -> [Промежуточные операции] -> [Терминальная операция].\n"
        "• Источник: коллекция или массив (transactions.stream()).\n"
        "• Промежуточные операции (Intermediate): filter, map, sorted, limit. Они возвращают новый поток и являются строго ленивыми.\n"
        "• Терминальная операция (Terminal): toList, reduce, collect, count, max. Запускает поток данных и завершает конвейер."
    )

    # 3. 8 задач
    add_h1("3. Решение 8 аналитических задач Finance Tracker")
    add_styled_table(
        ["№", "Задача", "Конвейер Stream API (Источник -> Промежуточные -> Терминальная)"],
        [
            ["1", "Расходы за период", "transactions.stream() -> .filter(IS_EXPENSE).filter(date in range) -> .toList()"],
            ["2", "Операции > суммы", "transactions.stream() -> .filter(amount > threshold) -> .toList()"],
            ["3", "Сортировка по сумме", "transactions.stream() -> .sorted(comparing(amount).reversed()) -> .toList()"],
            ["4", "Список описаний", "transactions.stream() -> .map(Transaction::getDescription).filter(nonBlank) -> .toList()"],
            ["5", "Сумма расходов", "transactions.stream() -> .filter(IS_EXPENSE).map(amount) -> .reduce(ZERO, BigDecimal::add)"],
            ["6", "Группировка расходов", "transactions.stream() -> .filter(IS_EXPENSE) -> .collect(groupingBy(Category, reducing(sum)))"],
            ["7", "Категория с макс. расходом", "groupExpensesByCategory().entrySet().stream() -> max(comparingByValue())"],
            ["8", "5 крупнейших расходов", "transactions.stream() -> .filter(IS_EXPENSE).sorted(amount desc).limit(5) -> .toList()"]
        ],
        col_widths=[0.4, 2.2, 4.4]
    )

    # 4. Эксперименты
    add_h1("4. Исследовательские эксперименты")
    add_h2("4.1. Эксперимент 1: Ленивость вычислений (Stream Laziness)")
    add_p(
        "При объявлении конвейера с операцией .filter(...) ни одна операция фильтрации не была выполнена, пока не был вызван метод .count(). "
        "Это доказывает, что промежуточные операции Stream API строго ленивы и не производят вычислений без терминального вызова."
    )

    add_h2("4.2. Эксперимент 2: Устранение антипаттерна «Плохой Stream»")
    add_p(
        "Антипаттерн накопления суммы в массиве-аккумуляторе внутри forEach нарушает чистоту функций. При параллельной обработке "
        "возникает состояние гонки (Race Condition). Замена на чистую редукцию .reduce(BigDecimal.ZERO, BigDecimal::add) гарантирует потокобезопасность."
    )

    add_h2("4.3. Эксперимент 3: stream() vs parallelStream()")
    add_p(
        "Параллельный поток задействует ForkJoinPool.commonPool(), обрабатывая элементы в недетерминированном порядке. "
        "Правило 'parallel всегда быстрее' ошибочно: на объемах до сотен тысяч элементов накладные расходы на разбиение задач "
        "и синхронизацию потоков превышают выигрыш от параллелизма."
    )

    # 5. Сравнение подходов
    add_h1("5. Сравнение императивного и декларативного подходов")
    add_p(
        "• Императивный подход (циклы for, временные коллекции) фокусируется на механике обхода, создавая многословный код с изменяемым состоянием.\n"
        "• Декларативный подход (Stream API) выражает намерение бизнес-логики через композицию чистых функций, кардинально повышая читаемость."
    )

    # 6. Программная реализация
    add_h1("6. Программная реализация и консольный вывод")
    add_code_block(
        "==================================================================\n"
        " ЛАБОРАТОРНАЯ РАБОТА № 7: STREAM API И ФУНКЦИОНАЛЬНЫЕ ИНТЕРФЕЙСЫ  \n"
        "==================================================================\n\n"
        "--- 1. Сравнение подходов (Императивный vs Stream API) ---\n"
        "Сумма по категории 'Продукты':\n"
        "  * Императивно (цикл for + accumulator): 5050.50 руб.\n"
        "  * Декларативно (filter + collect/reduce): 5050.50 руб.\n\n"
        "--- 2. Выполнение 8 аналитических задач Stream API ---\n"
        "Задача 1: Все расходы с 2026-09-01 по 2026-09-10: 4 операции\n"
        "Задача 2: Операции на сумму более 10 000 руб.: 3 операции\n"
        "Задача 3: Топ операций по убыванию суммы: 95000.00, 45000.00, 12500.00 руб.\n"
        "Задача 4: Список описаний: [Супермаркет, Билет, Зарплата, Аренда, ...]\n"
        "Задача 5: Общая сумма расходов (reduce): 63750.50 руб.\n"
        "Задача 6: Группировка расходов: Жилье: 45000.00, Электроника: 12500.00, Продукты: 5050.50 руб.\n"
        "Задача 7: Категория с максимальными расходами: Жилье (45000.00 руб.)\n"
        "Задача 8: Пять крупнейших расходов: 45000.00, 12500.00, 3200.50, 1200.00, 850.00 руб."
    )

    # 7. Байт-код
    add_h1("7. Исследование байт-кода: invokedynamic и LambdaMetafactory")
    add_p("Команды сборки и дизассемблирования:")
    add_code_block(
        "mkdir -p out\n"
        "javac -encoding UTF-8 -d out src/edu/course/lab07/model/*.java src/edu/course/lab07/service/*.java src/edu/course/lab07/*.java\n"
        "javap -classpath out -c -p edu.course.lab07.service.StreamAnalytics\n"
        "jar --create --file lab07.jar --main-class edu.course.lab07.StreamApp -C out .\n"
        "java -jar lab07.jar"
    )
    add_p(
        "Инструкция invokedynamic вызывает bootstrap-метод LambdaMetafactory.metafactory(...), "
        "который динамически в рантайме генерирует класс функционального интерфейса:",
        bold_prefix="Ключевые инструкции байт-кода:\n"
    )
    add_code_block(
        "0: invokedynamic #183,  0  // InvokeDynamic #8:test:()Ljava/util/function/Predicate;\n"
        "5: putstatic     #17       // Field IS_EXPENSE:Ljava/util/function/Predicate;"
    )

    # 8. Вопросы
    add_h1("8. Ответы на контрольные вопросы к защите")
    add_p(
        "Лямбда-выражение — это анонимный метод для лаконичной реализации функционального интерфейса. "
        "Функциональный интерфейс содержит ровно один абстрактный метод (SAM).",
        bold_prefix="Что такое лямбда-выражение и функциональный интерфейс?\n"
    )
    add_p(
        "Predicate проверяет условие (фильтрация); Function преобразует объект типа T в тип R; "
        "Consumer выполняет действие над элементом; Supplier поставляет/генерирует объект.",
        bold_prefix="Какие задачи решают Predicate, Function, Consumer, Supplier?\n"
    )
    add_p(
        "Промежуточные операции (filter, map) возвращают новый поток и являются ленивыми. "
        "Терминальные операции (toList, reduce, collect) инициируют вычисления и возвращают результат.",
        bold_prefix="Как устроен конвейер Stream: промежуточные и терминальные операции?\n"
    )
    add_p(
        "Промежуточные операции не производят вычислений в момент объявления. Выполнение начинается только "
        "при вызове терминальной операции, что позволяет объединять шаги и обрабатывать данные по требованию.",
        bold_prefix="Что означает «ленивость» вычислений в Stream API?\n"
    )
    add_p(
        "parallelStream задействует ForkJoinPool.commonPool(). Риски: состояние гонки при мутабельном состоянии, "
        "недетерминированный порядок forEach и накладные расходы на разделение задач, делающие его медленнее на малых объемах.",
        bold_prefix="В чем особенности и риски parallelStream()?\n"
    )

    output_path = r"C:\Users\sanya\.gemini\antigravity\scratch\lab07\REPORT_LAB07.docx"
    doc.save(output_path)
    print(f"Report saved successfully to {output_path}")

if __name__ == "__main__":
    create_report()
