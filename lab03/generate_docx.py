import os
import docx
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def create_report():
    doc = Document()

    # Поля страницы 2 см со всех сторон
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(0.79)
        section.bottom_margin = Inches(0.79)
        section.left_margin = Inches(0.79)
        section.right_margin = Inches(0.79)

    # Настройка базового стиля
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
        
        # Border: тонкая серая рамка
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
        
        # Заголовок таблицы
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
            
        # Данные
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
                
        # Настройка ширины колонок если задана
        if col_widths:
            for row in table.rows:
                for idx, w in enumerate(col_widths):
                    row.cells[idx].width = Inches(w)
                    
        doc.add_paragraph().paragraph_format.space_after = Pt(4)

    # ==================== НАПОЛНЕНИЕ ДОКУМЕНТА ====================

    add_title("ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 3")
    add_subtitle(
        "Современные технологии объектно-ориентированного программирования",
        "Наследование, интерфейсы и полиморфизм. Позднее связывание и контракт Object"
    )

    # 1. Концепция наследования и абстракции
    add_h1("1. Концепция наследования и абстракции в Java")
    add_h2("1.1. Наследование (extends) и отношение IS-A")
    add_p(
        "Наследование позволяет создать новый класс на базе уже существующего, перенимая его поля и методы. "
        "Отношение между классами выражается принципом «является» (IS-A): IncomeTransaction является Transaction. "
        "Ключевое слово extends задает прямого предка. В Java поддерживается только одиночное наследование классов "
        "(у класса может быть только один непосредственный родитель), но множественная реализация интерфейсов (implements)."
    )

    add_p(
        "Абстрактный класс задает общую сущность («кто я?», состояние и частичное поведение), определяет защищенные конструкторы, "
        "хранит поля (id, date, amount) и служит корневым типом родственной иерархии.\n"
        "Интерфейс задает отдельный контракт («что я умею делать?», роль/возможность), не может иметь состояния (полей экземпляра) "
        "и может быть реализован любым классом (например, Commissionable для операций с комиссией).",
        bold_prefix="Контрольный вопрос: В чем различие между абстрактным классом и интерфейсом?\n"
    )

    add_h2("1.2. Проверка версий инструментов JDK")
    add_code_block("java -version\njavac -version\njavap -version")

    add_h2("1.3. Ответы на вопросы к Заданию")
    add_p(
        "Класс Transaction объявлен как abstract. Он представляет собой незавершенную концепцию транзакции без конкретного правила влияния на баланс (метод balanceImpact() не имеет тела). Попытка вызвать new Transaction(...) пресекается компилятором Java.",
        bold_prefix="Почему нельзя создать экземпляр класса Transaction напрямую через new Transaction(...)?\n"
    )
    add_p(
        "Оно вызывает защищенный конструктор базового класса Transaction, гарантируя инициализацию и валидацию общих полей (id, date, amount) до выполнения логики наследника.",
        bold_prefix="Какую роль играет ключевое слово super в конструкторах наследников?\n"
    )
    add_p(
        "Компилятор выдаст ошибку: класс-наследник обязан либо реализовать все абстрактные методы предка, либо сам быть объявлен как abstract.",
        bold_prefix="Что произойдет, если в наследнике не реализовать абстрактный метод balanceImpact()?\n"
    )

    # 2. Архитектура иерархии
    add_h1("2. Архитектура иерархии типов и позднее связывание (Late Binding)")
    add_h2("2.1. Схема работы программы")
    add_code_block(
        "                     ┌───────────────────────┐\n"
        "                     │   java.lang.Object    │\n"
        "                     └───────────▲───────────┘\n"
        "                                 │\n"
        "                     ┌───────────┴───────────┐\n"
        "                     │ Transaction (abstract)│\n"
        "                     │  - id, date, amount   │\n"
        "                     │  + balanceImpact()*   │\n"
        "                     │  + equals/hashCode()  │\n"
        "                     └───────────▲───────────┘\n"
        "                                 │\n"
        "          ┌──────────────────────┼──────────────────────┬──────────────────────┐\n"
        "          │                      │                      │                      │\n"
        "┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐\n"
        "│ IncomeTransaction │  │ExpenseTransaction │  │TransferTransaction│  │CashbackTransaction│\n"
        "│ (баланс: +amount) │  │ (баланс: -amount) │  │(баланс: -(a + c)) │  │(баланс: +cashback)│\n"
        "└───────────────────┘  └───────────────────┘  └─────────▲─────────┘  └───────────────────┘\n"
        "                                                        │\n"
        "                                              implements│\n"
        "                                              ┌─────────┴─────────┐\n"
        "                                              │  Commissionable   │\n"
        "                                              │   (интерфейс)     │\n"
        "                                              │  + commission()*  │\n"
        "                                              └───────────────────┘"
    )

    add_h2("2.2. Механизм позднего связывания (Late / Dynamic Binding)")
    add_p(
        "При вызове transaction.balanceImpact() в цикле компилятор Java не знает, какой именно подкласс окажется в ссылке Transaction. "
        "В байт-код помещается инструкция invokevirtual. Во время выполнения виртуальная машина JVM обращается к таблице виртуальных методов (vtable) "
        "конкретного объекта в куче (Heap) и вызывает метод того класса, объект которого был фактически создан (IncomeTransaction, ExpenseTransaction, "
        "TransferTransaction или CashbackTransaction)."
    )

    # 3. Сравнение Overloading и Overriding
    add_h1("3. Сравнение механизмов Overloading и Overriding")
    add_styled_table(
        ["Характеристика", "Перегрузка (Overloading)", "Переопределение (Overriding)"],
        [
            ["Определение", "Несколько методов с одним именем, но разными параметрами в одном классе", "Замещение реализации родительского метода в классе-наследнике"],
            ["Где объявляется", "В пределах одного класса (или доступных из предка)", "В подклассе (с сохранением сигнатуры)"],
            ["Сигнатура метода", "Обязательно разный состав / типы аргументов", "Строго одинаковое имя и типы параметров"],
            ["Возвращаемый тип", "Может быть любым", "Совпадает или ковариантен (подтип)"],
            ["Момент связывания", "Compile-time (статическое / раннее связывание)", "Runtime (динамическое / позднее связывание)"],
            ["Аннотация", "Не используется", "Рекомендуется @Override для контроля компилятора"],
            ["Инструкция байт-кода", "invokevirtual / invokestatic с фиксированным типом", "invokevirtual через динамическую vtable"]
        ],
        col_widths=[1.8, 2.5, 2.5]
    )

    # 4. Практикум
    add_h1("4. Практикум: Перегрузка методов и контракт Object")
    add_h2("4.1. Демонстрация Overloading (файл TransactionPrinter.java)")
    add_code_block(
        "package edu.course.lab03.service;\n\n"
        "import edu.course.lab03.model.Transaction;\n\n"
        "public class TransactionPrinter {\n"
        "    // Перегруженный вариант № 1 для одиночного объекта\n"
        "    public void print(Transaction transaction) {\n"
        "        if (transaction == null) {\n"
        "            System.out.println(\"null transaction\");\n"
        "            return;\n"
        "        }\n"
        "        System.out.println(\"  [Единичная транзакция] \" + transaction);\n"
        "    }\n\n"
        "    // Перегруженный вариант № 2 для массива объектов\n"
        "    public void print(Transaction[] transactions) {\n"
        "        if (transactions == null || transactions.length == 0) {\n"
        "            System.out.println(\"Список транзакций пуст.\");\n"
        "            return;\n"
        "        }\n"
        "        System.out.printf(\"  [Список из %d транзакций]:%n\", transactions.length);\n"
        "        for (Transaction tx : transactions) {\n"
        "            System.out.println(\"    * \" + tx);\n"
        "        }\n"
        "    }\n"
        "}"
    )

    add_h2("4.2. Контракт методов equals() и hashCode() в Transaction.java")
    add_p("Идентичность транзакции определяется ее уникальным бизнес-идентификатором UUID id:")
    add_code_block(
        "    @Override\n"
        "    public boolean equals(Object o) {\n"
        "        if (this == o) return true;\n"
        "        if (o == null || getClass() != o.getClass()) return false;\n"
        "        Transaction that = (Transaction) o;\n"
        "        return Objects.equals(id, that.id);\n"
        "    }\n\n"
        "    @Override\n"
        "    public int hashCode() {\n"
        "        return Objects.hash(id);\n"
        "    }"
    )
    add_p(
        "Контракт языка Java строго требует: если a.equals(b) == true, то a.hashCode() == b.hashCode() обязан возвращать одинаковое целое число. "
        "При нарушении этого правила объекты теряются в коллекциях HashMap или HashSet, попадая в разные корзины (buckets) хеш-таблицы."
    )

    # 5. Интерфейс Commissionable
    add_h1("5. Интерфейс как гибкий контракт (файл Commissionable.java)")
    add_code_block(
        "package edu.course.lab03.model;\n\n"
        "import java.math.BigDecimal;\n\n"
        "public interface Commissionable {\n"
        "    BigDecimal commission();\n"
        "}"
    )
    add_p(
        "Комиссия присуща только определенным операциям (банковским переводам). Доходные операции (зарплата) или начисления кэшбэка не имеют комиссии. "
        "Если поместить метод commission() в базовый класс Transaction, наследники без комиссии были бы вынуждены возвращать 0 или выбрасывать "
        "исключения UnsupportedOperationException, что нарушило бы принцип разделения интерфейсов (ISP) и принцип подстановки Барбары Лисков (LSP). "
        "Интерфейс подключается избирательно только туда, где комиссия действительно существует.",
        bold_prefix="Анализ архитектурного решения (почему Commissionable не является родителем всей иерархии):\n"
    )

    # 6. Объектно-ориентированная реализация
    add_h1("6. Объектно-ориентированная реализация")
    add_h2("6.1. Базовый абстрактный класс Transaction.java")
    add_code_block(
        "package edu.course.lab03.model;\n\n"
        "import java.math.BigDecimal;\n"
        "import java.time.LocalDate;\n"
        "import java.util.Objects;\n"
        "import java.util.UUID;\n\n"
        "public abstract class Transaction {\n"
        "    private final UUID id;\n"
        "    private final LocalDate date;\n"
        "    private final BigDecimal amount;\n\n"
        "    protected Transaction(UUID id, LocalDate date, BigDecimal amount) {\n"
        "        if (id == null) throw new IllegalArgumentException(\"Идентификатор транзакции не может быть null\");\n"
        "        if (date == null) throw new IllegalArgumentException(\"Дата транзакции не может быть null\");\n"
        "        if (amount == null) throw new IllegalArgumentException(\"Сумма транзакции не может быть null\");\n"
        "        if (amount.compareTo(BigDecimal.ZERO) <= 0) {\n"
        "            throw new IllegalArgumentException(\"Сумма транзакции должна быть строго положительной: \" + amount);\n"
        "        }\n"
        "        this.id = id;\n"
        "        this.date = date;\n"
        "        this.amount = amount;\n"
        "    }\n\n"
        "    public final UUID getId() { return id; }\n"
        "    public final LocalDate getDate() { return date; }\n"
        "    public final BigDecimal getAmount() { return amount; }\n\n"
        "    public abstract BigDecimal balanceImpact();\n"
        "    public abstract String getTypeName();\n\n"
        "    @Override\n"
        "    public boolean equals(Object o) {\n"
        "        if (this == o) return true;\n"
        "        if (o == null || getClass() != o.getClass()) return false;\n"
        "        Transaction that = (Transaction) o;\n"
        "        return Objects.equals(id, that.id);\n"
        "    }\n\n"
        "    @Override\n"
        "    public int hashCode() { return Objects.hash(id); }\n\n"
        "    @Override\n"
        "    public String toString() {\n"
        "        String sign = balanceImpact().compareTo(BigDecimal.ZERO) >= 0 ? \"+\" : \"\";\n"
        "        return String.format(\"%s | %s | %-8s | Сумма: %s | Влияние: %s%s\",\n"
        "                id, date, getTypeName(), amount, sign, balanceImpact());\n"
        "    }\n"
        "}"
    )

    add_h2("6.2. Классы-наследники (IncomeTransaction, ExpenseTransaction, TransferTransaction)")
    add_code_block(
        "package edu.course.lab03.model;\n\n"
        "import java.math.BigDecimal;\nimport java.time.LocalDate;\nimport java.util.UUID;\n\n"
        "public final class IncomeTransaction extends Transaction {\n"
        "    public IncomeTransaction(UUID id, LocalDate date, BigDecimal amount) {\n"
        "        super(id, date, amount);\n"
        "    }\n"
        "    @Override public BigDecimal balanceImpact() { return getAmount(); }\n"
        "    @Override public String getTypeName() { return \"Доход\"; }\n"
        "}\n\n"
        "public final class ExpenseTransaction extends Transaction {\n"
        "    public ExpenseTransaction(UUID id, LocalDate date, BigDecimal amount) {\n"
        "        super(id, date, amount);\n"
        "    }\n"
        "    @Override public BigDecimal balanceImpact() { return getAmount().negate(); }\n"
        "    @Override public String getTypeName() { return \"Расход\"; }\n"
        "}\n\n"
        "public final class TransferTransaction extends Transaction implements Commissionable {\n"
        "    private final BigDecimal commissionFee;\n"
        "    private final String recipientAccount;\n\n"
        "    public TransferTransaction(UUID id, LocalDate date, BigDecimal amount, BigDecimal fee, String acc) {\n"
        "        super(id, date, amount);\n"
        "        if (fee == null || fee.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(\"Неверная комиссия\");\n"
        "        if (acc == null || acc.isBlank()) throw new IllegalArgumentException(\"Счет пуст\");\n"
        "        this.commissionFee = fee;\n"
        "        this.recipientAccount = acc;\n"
        "    }\n"
        "    @Override public BigDecimal commission() { return commissionFee; }\n"
        "    @Override public BigDecimal balanceImpact() { return getAmount().add(commissionFee).negate(); }\n"
        "    @Override public String getTypeName() { return \"Перевод\"; }\n"
        "}"
    )

    add_h2("6.3. Индивидуальное расширение (CashbackTransaction.java)")
    add_code_block(
        "package edu.course.lab03.model;\n\n"
        "import java.math.BigDecimal;\nimport java.math.RoundingMode;\nimport java.time.LocalDate;\nimport java.util.UUID;\n\n"
        "public final class CashbackTransaction extends Transaction {\n"
        "    private final BigDecimal rate;\n"
        "    private final BigDecimal originalPurchaseAmount;\n\n"
        "    public CashbackTransaction(UUID id, LocalDate date, BigDecimal purchase, BigDecimal rate) {\n"
        "        super(id, date, purchase.multiply(rate).setScale(2, RoundingMode.HALF_UP));\n"
        "        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {\n"
        "            throw new IllegalArgumentException(\"Ставка вне диапазона (0, 1]\");\n"
        "        }\n"
        "        this.originalPurchaseAmount = purchase;\n"
        "        this.rate = rate;\n"
        "    }\n"
        "    @Override public BigDecimal balanceImpact() { return getAmount(); }\n"
        "    @Override public String getTypeName() { return \"Кэшбэк\"; }\n"
        "}"
    )

    add_h2("6.4. Сервис полиморфного учета (AccountService.java)")
    add_code_block(
        "package edu.course.lab03.service;\n\n"
        "import edu.course.lab03.model.Commissionable;\nimport edu.course.lab03.model.Transaction;\nimport java.math.BigDecimal;\n\n"
        "public class AccountService {\n"
        "    public BigDecimal calculateTotalBalanceImpact(Transaction[] transactions) {\n"
        "        if (transactions == null) return BigDecimal.ZERO;\n"
        "        BigDecimal total = BigDecimal.ZERO;\n"
        "        for (Transaction transaction : transactions) {\n"
        "            // Динамическая диспетчеризация (invokevirtual в байт-коде) без instanceof и switch!\n"
        "            total = total.add(transaction.balanceImpact());\n"
        "        }\n"
        "        return total;\n"
        "    }\n\n"
        "    public BigDecimal calculateTotalCommission(Transaction[] transactions) {\n"
        "        if (transactions == null) return BigDecimal.ZERO;\n"
        "        BigDecimal totalCommission = BigDecimal.ZERO;\n"
        "        for (Transaction transaction : transactions) {\n"
        "            if (transaction instanceof Commissionable commissionable) {\n"
        "                totalCommission = totalCommission.add(commissionable.commission());\n"
        "            }\n"
        "        }\n"
        "        return totalCommission;\n"
        "    }\n"
        "}"
    )

    add_h2("6.5. Главный класс приложения DeliveryApp.java / TransactionApp.java (Полиморфизм)")
    add_code_block(
        "package edu.course.lab03;\n\n"
        "import edu.course.lab03.model.*;\n"
        "import edu.course.lab03.service.AccountService;\n"
        "import edu.course.lab03.service.TransactionPrinter;\n"
        "import java.math.BigDecimal;\nimport java.time.LocalDate;\nimport java.util.UUID;\n\n"
        "public class TransactionApp {\n"
        "    public static void main(String[] args) {\n"
        "        UUID id1 = UUID.fromString(\"11111111-1111-1111-1111-111111111111\");\n"
        "        UUID id2 = UUID.fromString(\"22222222-2222-2222-2222-222222222222\");\n"
        "        UUID id3 = UUID.fromString(\"33333333-3333-3333-3333-333333333333\");\n"
        "        UUID id4 = UUID.fromString(\"44444444-4444-4444-4444-444444444444\");\n"
        "        LocalDate today = LocalDate.of(2026, 10, 27);\n\n"
        "        Transaction[] transactions = new Transaction[] {\n"
        "                new IncomeTransaction(id1, today, new BigDecimal(\"50000.00\")),\n"
        "                new ExpenseTransaction(id2, today, new BigDecimal(\"15000.00\")),\n"
        "                new TransferTransaction(id3, today, new BigDecimal(\"10000.00\"), new BigDecimal(\"150.00\"), \"RU40817810000012345678\"),\n"
        "                new CashbackTransaction(id4, today, new BigDecimal(\"20000.00\"), new BigDecimal(\"0.05\"))\n"
        "        };\n\n"
        "        AccountService service = new AccountService();\n"
        "        BigDecimal total = service.calculateTotalBalanceImpact(transactions);\n"
        "        System.out.printf(\"Суммарное изменение баланса: %+s руб.%n\", total);\n"
        "    }\n"
        "}"
    )

    add_p("Консольный вывод приложения:")
    add_code_block(
        "==================================================================\n"
        " ЛАБОРАТОРНАЯ РАБОТА № 3: НАСЛЕДОВАНИЕ, ИНТЕРФЕЙСЫ И ПОЛИМОРФИЗМ \n"
        "==================================================================\n\n"
        "--- 1. Демонстрация Overloading (TransactionPrinter) ---\n"
        "Вызов print(Transaction) [раннее статическое связывание]:\n"
        "  [Единичная транзакция] 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00\n\n"
        "Вызов print(Transaction[]) [раннее статическое связывание]:\n"
        "  [Список из 4 транзакций]:\n"
        "    * 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00\n"
        "    * 22222222-2222-2222-2222-222222222222 | 2026-10-27 | Расход   | Сумма: 15000.00 | Влияние: -15000.00\n"
        "    * 33333333-3333-3333-3333-333333333333 | 2026-10-27 | Перевод  | Сумма: 10000.00 | Влияние: -10150.00 [Получатель: RU40817810000012345678, Комиссия: 150.00]\n"
        "    * 44444444-4444-4444-4444-444444444444 | 2026-10-27 | Кэшбэк   | Сумма: 1000.00 | Влияние: +1000.00 [Покупка: 20000.00, Ставка: 5,0%]\n\n"
        "--- 2. Полиморфный расчет баланса (Late Binding) ---\n"
        "Расчет по отдельным операциям:\n"
        "  [Доход   ] Сумма:   50000.00 | Влияние на баланс: +  50000.00\n"
        "  [Расход  ] Сумма:   15000.00 | Влияние на баланс:  -15000.00\n"
        "  [Перевод ] Сумма:   10000.00 | Влияние на баланс:  -10150.00\n"
        "  [Кэшбэк  ] Сумма:    1000.00 | Влияние на баланс: +   1000.00\n"
        "------------------------------------------------------------------\n"
        "Суммарное изменение баланса счета: +25850.00 руб.\n"
        "Суммарная уплаченная комиссия:     150.00 руб.\n"
        "------------------------------------------------------------------\n\n"
        "--- 3. Проверка контракта Object (equals & hashCode) ---\n"
        "t1.equals(t1Clone): true (ожидается true, т.к. ID одинаковы)\n"
        "t1.hashCode() == t1Clone.hashCode(): true (ожидается true)\n"
        "t1.equals(tDifferent): false (ожидается false, т.к. ID разные)\n"
        "t1.toString(): 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00"
    )

    # 7. Четыре принципа ООП
    add_h1("7. Четыре принципа ООП в разработанной программе")
    add_styled_table(
        ["Принцип ООП", "Где проявляется в программе", "Практическая польза в архитектуре"],
        [
            ["Абстракция", "Базовый абстрактный класс Transaction, абстрактный метод balanceImpact(), интерфейс Commissionable", "Позволяет отделить контракт операции (влияние на баланс и комиссия) от деталей вычисления каждого конкретного подтипа."],
            ["Инкапсуляция", "Модификаторы private final для полей (id, date, amount, commissionFee), проверка в конструкторе", "Прячет поля внутри объекта и защищает их от неверных значений (предотвращает null и отрицательные суммы транзакций)."],
            ["Наследование", "extends Transaction у классов IncomeTransaction, ExpenseTransaction, TransferTransaction, CashbackTransaction", "Позволяет не переписывать заново код для хранения идентификатора, даты и суммы в каждом новом классе операции."],
            ["Полиморфизм", "Вызов transaction.balanceImpact() в цикле по массиву базового типа Transaction[]", "Дает возможность единообразно обрабатывать транзакции любых видов через единый контракт без использования instanceof и switch (принцип OCP)."]
        ],
        col_widths=[1.5, 2.6, 2.7]
    )

    # 8. Исследование байт-кода
    add_h1("8. Исследование байт-кода и сборка JAR")
    add_p("Команды компиляции и сборки:")
    add_code_block(
        "mkdir -p out\n"
        "javac -encoding UTF-8 -d out src/edu/course/lab03/model/*.java src/edu/course/lab03/service/*.java src/edu/course/lab03/*.java\n"
        "javap -classpath out -c -p edu.course.lab03.service.AccountService\n"
        "jar --create --file lab03.jar --main-class edu.course.lab03.TransactionApp -C out .\n"
        "java -jar lab03.jar"
    )

    add_p(
        "invokevirtual — динамический вызов метода объекта через таблицу виртуальных методов во время работы (late binding), например transaction.balanceImpact().\n"
        "invokespecial — вызов конструктора предка super(...) или приватного метода (статически разрешается компилятором).\n"
        "invokeinterface — вызов метода интерфейса, например commissionable.commission().\n"
        "invokestatic — вызов статического вспомогательного метода (например, Objects.equals(...) или Objects.hash(...)).\n"
        "areturn — возврат объектной ссылки (BigDecimal) из метода.",
        bold_prefix="Ключевые инструкции байт-кода:\n"
    )

    add_p("Дизассемблированный фрагмент метода calculateTotalBalanceImpact:")
    add_code_block(
        "34: aload_2\n"
        "35: aload         6\n"
        "37: invokevirtual #13  // Method edu/course/lab03/model/Transaction.balanceImpact:()Ljava/math/BigDecimal;\n"
        "40: invokevirtual #19  // Method java/math/BigDecimal.add:(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;\n"
        "43: astore_2"
    )

    # 9. Ответы на контрольные вопросы к защите
    add_h1("9. Ответы на контрольные вопросы к защите")
    add_p(
        "Наследование — это механизм ООП, позволяющий создать новый класс на базе уже существующего. Ключевое слово extends указывает имя родительского класса, свойства и поведение которого перенимает класс-наследник.",
        bold_prefix="Что такое наследование и как используется extends?\n"
    )
    add_p(
        "Перегрузка — это создание в рамках одного класса нескольких методов с одинаковым именем, но разными сигнатурами параметров (статическое связывание компилятором). Переопределение — это реализация метода суперкласса в подклассе с идентичной сигнатурой (динамическое связывание во время работы).",
        bold_prefix="Чем отличается перегрузка от переопределения методов?\n"
    )
    add_p(
        "Это способность программы вызывать метод конкретного объекта через ссылку на его абстрактный базовый тип. Позднее связывание означает, что JVM определяет точный метод для исполнения только во время работы программы по реальному типу созданного в куче объекта.",
        bold_prefix="Что такое полиморфизм времени выполнения и позднее связывание?\n"
    )
    add_p(
        "Абстрактный класс задает общую родовую сущность («кто я?»), может иметь переменные состояния и конструкторы. Интерфейс задает независимый контракт поведения («что я умею делать?»), допускает множественную реализацию и не может иметь изменяемого состояния.",
        bold_prefix="Абстрактные классы и интерфейсы: в чем различия и когда что применять?\n"
    )
    add_p(
        "Класс Object — прародитель всех классов в Java. Метод toString() формирует понятное строковое описание объекта; equals() сравнивает смысловое равенство данных объектов; hashCode() вычисляет целочисленный хеш для эффективного поиска в хеш-таблицах.",
        bold_prefix="Какую роль играет класс Object и методы toString(), equals(), hashCode()?\n"
    )
    add_p(
        "Если два объекта равны согласно equals(), они обязательно должны возвращать одинаковый результат hashCode(). Если это правило нарушить, равные объекты окажутся в разных корзинах хеш-таблицы (HashSet/HashMap) и поиск по ним перестанет работать.",
        bold_prefix="Почему при переопределении equals() необходимо согласованно реализовывать hashCode()?\n"
    )
    add_p(
        "Потому что он незавершен — в нем могут быть объявлены методы без тела (как balanceImpact()), и он предназначен исключительно как фундамент для наследования подклассами.",
        bold_prefix="Почему нельзя создать экземпляр абстрактного класса?\n"
    )
    add_p(
        "Она сообщает компилятору, что данный метод намеренно замещает метод родительского класса. Если допущена опечатка в имени или сигнатуре метода, компилятор немедленно укажет на ошибку.",
        bold_prefix="Что означает аннотация @Override?\n"
    )

    output_path = r"C:\Users\sanya\.gemini\antigravity\scratch\lab03\REPORT_LAB03.docx"
    doc.save(output_path)
    print(f"Report saved successfully to {output_path}")

if __name__ == "__main__":
    create_report()
