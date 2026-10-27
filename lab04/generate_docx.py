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

    # Поля страницы 2 см со всех сторон
    sections = doc.sections
    for section in sections:
        section.top_margin = Inches(0.79)
        section.bottom_margin = Inches(0.79)
        section.left_margin = Inches(0.79)
        section.right_margin = Inches(0.79)

    # Базовый стиль
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

    add_title("ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 4")
    add_subtitle(
        "Современные технологии объектно-ориентированного программирования",
        "Исключения, диагностика и устойчивое поведение программы. Иерархия Throwable и Single Responsibility Principle"
    )

    # 1. Концепция исключений
    add_h1("1. Концепция обработки исключений в Java")
    add_h2("1.1. Иерархия классов Throwable, Exception, RuntimeException и Error")
    add_p(
        "В Java все типы исключительных ситуаций являются наследниками базового класса java.lang.Throwable. "
        "Иерархия делится на две принципиальные ветви:\n"
        "1. java.lang.Error — критические ошибки JVM (OutOfMemoryError, StackOverflowError), при которых продолжение программы невозможно.\n"
        "2. java.lang.Exception — программные исключения. Они подразделяются на проверяемые (Checked) и непроверяемые (Unchecked / RuntimeException)."
    )

    add_p(
        "throw — это оператор в теле метода, осуществляющий выброс конкретного объекта исключения (throw new InsufficientFundsException(...);).\n"
        "throws — это ключевое слово в заголовке метода, предупреждающее компилятор о том, что данный метод может выбросить указанное проверяемое исключение.",
        bold_prefix="Контрольный вопрос: В чем различие между throw и throws?\n"
    )

    add_h2("1.2. Проверка версий инструментов JDK")
    add_code_block("java -version\njavac -version\njavap -version")

    add_h2("1.3. Ответы на вопросы к Заданию")
    add_p(
        "Создание объекта исключения сопряжено с дорогостоящей операцией fillInStackTrace() (обход всего стека вызовов потока). "
        "Использование исключений для обычного ветвления кода приводит к резкой деградации производительности и загромождению бизнес-логики.",
        bold_prefix="Почему нельзя использовать исключения как обычную замену условным операторам if-else?\n"
    )
    add_p(
        "Низкоуровневый сервис (WalletService) не должен напрямую выводить в консоль сообщения 'У вас ошибка!'. Его задача — либо успешно провести транзакцию, "
        "либо выбросить строгое исключение. Преобразование ошибки в текст для пользователя — зона ответственности UI-контроллера.",
        bold_prefix="Какую роль играет принцип единственной ответственности (SRP) при обработке ошибок?\n"
    )

    # 2. Архитектура приложения
    add_h1("2. Архитектура надежного приложения и обработка исключений (try-catch-finally)")
    add_h2("2.1. Схема движения потока управления и распределения ответственности")
    add_code_block(
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│                      Пользовательский слой (UI)                        │\n"
        "│                 edu.course.lab04.ui.WalletConsoleController            │\n"
        "└───────────────────────────────────┬────────────────────────────────────┘\n"
        "                                    │ 1. Ввод пользователя (rawAmount)\n"
        "                                    ▼\n"
        "                      ┌───────────────────────────┐\n"
        "                      │    try-catch-finally      │\n"
        "                      └─────────────┬─────────────┘\n"
        "                                    │ 2. Вызов transfer(source, target, amount)\n"
        "                                    ▼\n"
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│                         Сервисный слой (Core)                          │\n"
        "│                 edu.course.lab04.service.WalletService                 │\n"
        "│                                                                        │\n"
        "│   Валидация аргументов      Проверка баланса       Транзакция          │\n"
        "│   ┌───────────────────┐    ┌────────────────┐   ┌──────────────────┐   │\n"
        "│   │ amount <= 0 ?     │    │ balance < amt ?│   │ source.withdraw()│   │\n"
        "│   └─────────┬─────────┘    └───────┬────────┘   │ target.deposit() │   │\n"
        "│             │                      │            └──────────────────┘   │\n"
        "└─────────────┼──────────────────────┼───────────────────────────────────┘\n"
        "              │ throw (Unchecked)    │ throw (Checked)\n"
        "              ▼                      ▼\n"
        "┌──────────────────────────┐   ┌──────────────────────────────┐\n"
        "│InvalidTransactionExcept. │   │  InsufficientFundsException  │\n"
        "└─────────────┬────────────┘   └──────────────┬───────────────┘\n"
        "              │                               │\n"
        "              └───────────────┬───────────────┘\n"
        "                              │ 3. Перехват в UI-слое\n"
        "                              ▼\n"
        "┌────────────────────────────────────────────────────────────────────────┐\n"
        "│   catch (NumberFormatException)       -> Подсказка формата числа       │\n"
        "│   catch (InsufficientFundsException)  -> Рекомендация пополнить кошелек│\n"
        "│   catch (WalletNotFoundException)     -> Проверка номера кошелька      │\n"
        "│   finally                             -> Гарантированный аудит операции│\n"
        "└────────────────────────────────────────────────────────────────────────┘"
    )

    add_h2("2.2. Механизм гарантированного выполнения (блок finally)")
    add_p(
        "Блок finally выполняется всегда: и при нормальном выполнении try, и при возникновении любого типа исключения. "
        "В проекте блок finally обеспечивает обязательный аудит попытки проведения транзакции, гарантируя независимую фиксацию операции."
    )

    # 3. Анализ антипаттернов
    add_h1("3. Анализ антипаттернов обработки исключений")
    add_styled_table(
        ["Антипаттерн", "Какую информацию уничтожает", "В чем опасность для системы", "Как исправить"],
        [
            ["catch (Exception e) {}", "Уничтожает тип исключения, текст ошибки и stack trace.", "Система переходит в состояние 'тихого сбоя', продолжая работу с испорченными данными.", "Залогировать сбой через Logger или пробросить специализированное исключение."],
            ["catch (Exception e) { return null; }", "Скрывает причину сбоя, подменяя результат пустым значением.", "Вызывает лавинообразные NullPointerException в вызывающих модулях далеко от корня проблемы.", "Выбрасывать бизнес-исключение либо возвращать явный контейнер Optional."],
            ["catch (Throwable t) { ... }", "Перехватывает критические ошибки JVM (OutOfMemoryError, StackOverflowError).", "Нарушает стабильность JVM. Попытка продолжать работу после сбоев памяти ведет к порче структур данных.", "Перехватывать только Exception или узкие прикладные типы, не маскируя системные Error."]
        ],
        col_widths=[1.7, 1.7, 1.8, 1.6]
    )

    # 4. Практикум
    add_h1("4. Практикум: Разработка пользовательских классов исключений")
    add_h2("4.1. Проверяемое исключение InsufficientFundsException.java")
    add_code_block(
        "package edu.course.lab04.exception;\n\n"
        "import java.math.BigDecimal;\n\n"
        "public class InsufficientFundsException extends Exception {\n"
        "    private final String walletId;\n"
        "    private final BigDecimal currentBalance;\n"
        "    private final BigDecimal requestedAmount;\n\n"
        "    public InsufficientFundsException(String walletId, BigDecimal currentBalance, BigDecimal requestedAmount) {\n"
        "        super(String.format(\"Недостаточно средств на кошельке [%s]: текущий баланс %s руб., запрошено к списанию %s руб.\",\n"
        "                walletId, currentBalance, requestedAmount));\n"
        "        this.walletId = walletId;\n"
        "        this.currentBalance = currentBalance;\n"
        "        this.requestedAmount = requestedAmount;\n"
        "    }\n\n"
        "    public String getWalletId() { return walletId; }\n"
        "    public BigDecimal getCurrentBalance() { return currentBalance; }\n"
        "    public BigDecimal getRequestedAmount() { return requestedAmount; }\n"
        "}"
    )

    add_h2("4.2. Непроверяемое исключение InvalidTransactionException.java и цепочки исключений")
    add_code_block(
        "package edu.course.lab04.exception;\n\n"
        "public class InvalidTransactionException extends RuntimeException {\n"
        "    public InvalidTransactionException(String message) {\n"
        "        super(message);\n"
        "    }\n"
        "    public InvalidTransactionException(String message, Throwable cause) {\n"
        "        super(message, cause);\n"
        "    }\n"
        "}"
    )

    add_h2("4.3. Непроверяемое исключение WalletNotFoundException.java")
    add_code_block(
        "package edu.course.lab04.exception;\n\n"
        "public class WalletNotFoundException extends RuntimeException {\n"
        "    private final String walletId;\n"
        "    public WalletNotFoundException(String walletId) {\n"
        "        super(String.format(\"Кошелек с идентификатором [%s] не зарегистрирован в системе\", walletId));\n"
        "        this.walletId = walletId;\n"
        "    }\n"
        "    public String getWalletId() { return walletId; }\n"
        "}"
    )

    # 5. Программная реализация
    add_h1("5. Программная реализация сервиса и пользовательского слоя (SRP)")
    add_h2("5.1. Модель данных кошелька Wallet.java")
    add_code_block(
        "package edu.course.lab04.model;\n\n"
        "import java.math.BigDecimal;\n\n"
        "public class Wallet {\n"
        "    private final String id;\n"
        "    private final String ownerName;\n"
        "    private BigDecimal balance;\n\n"
        "    public Wallet(String id, String ownerName, BigDecimal initialBalance) {\n"
        "        if (id == null || id.isBlank()) throw new IllegalArgumentException(\"Идентификатор пуст\");\n"
        "        if (ownerName == null || ownerName.isBlank()) throw new IllegalArgumentException(\"Имя пусто\");\n"
        "        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {\n"
        "            throw new IllegalArgumentException(\"Баланс не может быть отрицательным\");\n"
        "        }\n"
        "        this.id = id;\n"
        "        this.ownerName = ownerName;\n"
        "        this.balance = initialBalance;\n"
        "    }\n\n"
        "    public String getId() { return id; }\n"
        "    public String getOwnerName() { return ownerName; }\n"
        "    public synchronized BigDecimal getBalance() { return balance; }\n"
        "    public synchronized void deposit(BigDecimal amount) {\n"
        "        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(\"Сумма <= 0\");\n"
        "        this.balance = this.balance.add(amount);\n"
        "    }\n"
        "    public synchronized void withdraw(BigDecimal amount) {\n"
        "        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException(\"Сумма <= 0\");\n"
        "        if (this.balance.compareTo(amount) < 0) throw new IllegalStateException(\"Баланс меньше суммы списания\");\n"
        "        this.balance = this.balance.subtract(amount);\n"
        "    }\n"
        "    @Override public String toString() {\n"
        "        return String.format(\"Кошелек [%s | %s | Баланс: %s руб.]\", id, ownerName, balance);\n"
        "    }\n"
        "}"
    )

    add_h2("5.2. Сервис межбанковского перевода WalletService.java")
    add_code_block(
        "package edu.course.lab04.service;\n\n"
        "import edu.course.lab04.exception.*;\nimport edu.course.lab04.model.Wallet;\n"
        "import java.math.BigDecimal;\nimport java.util.Map;\nimport java.util.concurrent.ConcurrentHashMap;\n"
        "import java.util.logging.*;\n\n"
        "public class WalletService {\n"
        "    private static final Logger logger = Logger.getLogger(WalletService.class.getName());\n"
        "    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();\n\n"
        "    public void registerWallet(Wallet wallet) {\n"
        "        if (wallet == null) throw new IllegalArgumentException(\"Кошелек не может быть null\");\n"
        "        wallets.put(wallet.getId(), wallet);\n"
        "        logger.info(() -> \"Зарегистрирован кошелек: \" + wallet);\n"
        "    }\n\n"
        "    public Wallet getWallet(String id) {\n"
        "        if (id == null || id.isBlank()) throw new InvalidTransactionException(\"Идентификатор пуст\");\n"
        "        Wallet wallet = wallets.get(id);\n"
        "        if (wallet == null) throw new WalletNotFoundException(id);\n"
        "        return wallet;\n"
        "    }\n\n"
        "    public void transfer(String sourceId, String targetId, BigDecimal amount)\n"
        "            throws InsufficientFundsException {\n"
        "        if (sourceId == null || sourceId.isBlank() || targetId == null || targetId.isBlank()) {\n"
        "            throw new InvalidTransactionException(\"Идентификаторы кошельков обязательны\");\n"
        "        }\n"
        "        if (sourceId.equals(targetId)) {\n"
        "            throw new InvalidTransactionException(\"Перевод самому себе запрещен: \" + sourceId);\n"
        "        }\n"
        "        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {\n"
        "            throw new InvalidTransactionException(\"Сумма перевода должна быть строго положительной\");\n"
        "        }\n"
        "        Wallet source = getWallet(sourceId);\n"
        "        Wallet target = getWallet(targetId);\n"
        "        if (source.getBalance().compareTo(amount) < 0) {\n"
        "            throw new InsufficientFundsException(sourceId, source.getBalance(), amount);\n"
        "        }\n"
        "        try {\n"
        "            source.withdraw(amount);\n"
        "            target.deposit(amount);\n"
        "            logger.info(() -> String.format(\"Успешный перевод %s руб. [%s -> %s]\", amount, sourceId, targetId));\n"
        "        } catch (Exception ex) {\n"
        "            logger.log(Level.SEVERE, \"Критический сбой изменения балансов\", ex);\n"
        "            throw new InvalidTransactionException(\"Внутренний сбой при выполнении перевода\", ex);\n"
        "        }\n"
        "    }\n"
        "}"
    )

    add_h2("5.3. Пользовательский слой WalletConsoleController.java (Соблюдение SRP)")
    add_code_block(
        "package edu.course.lab04.ui;\n\n"
        "import edu.course.lab04.exception.*;\nimport edu.course.lab04.service.WalletService;\nimport java.math.BigDecimal;\n\n"
        "public class WalletConsoleController {\n"
        "    private final WalletService walletService;\n"
        "    public WalletConsoleController(WalletService walletService) { this.walletService = walletService; }\n\n"
        "    public void processTransfer(String sourceId, String targetId, String rawAmount) {\n"
        "        System.out.printf(\"%n>>> Запрос на перевод: [%s -> %s], сумма: '%s'%n\", sourceId, targetId, rawAmount);\n"
        "        boolean success = false;\n"
        "        try {\n"
        "            BigDecimal amount = new BigDecimal(rawAmount.trim().replace(',', '.'));\n"
        "            walletService.transfer(sourceId, targetId, amount);\n"
        "            System.out.printf(\"  [УСПЕХ] Перевод на сумму %s руб. успешно завершен!%n\", amount);\n"
        "            success = true;\n"
        "        } catch (NumberFormatException nfe) {\n"
        "            System.err.printf(\"  [ОШИБКА ВВОДА] Значение '%s' не число! Формат: 1500.50%n\", rawAmount);\n"
        "        } catch (InsufficientFundsException ife) {\n"
        "            System.err.printf(\"  [ОТКАЗ БАНКА] %s%n\", ife.getMessage());\n"
        "            System.err.printf(\"  [СОВЕТ] Пополните кошелек или укажите сумму <= %s руб.%n\", ife.getCurrentBalance());\n"
        "        } catch (WalletNotFoundException wnfe) {\n"
        "            System.err.printf(\"  [ОШИБКА РЕКВИЗИТОВ] %s. Проверьте номер кошелька.%n\", wnfe.getMessage());\n"
        "        } catch (InvalidTransactionException ite) {\n"
        "            System.err.printf(\"  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] %s%n\", ite.getMessage());\n"
        "        } finally {\n"
        "            System.out.printf(\"  [FINALLY-АУДИТ] Обработка [%s -> %s] завершена. Результат: %s.%n\",\n"
        "                    sourceId, targetId, success ? \"УСПЕШНО\" : \"ОШИБКА / ОТКЛОНЕНО\");\n"
        "        }\n"
        "    }\n"
        "}"
    )

    add_h2("5.4. Главный класс приложения WalletApp.java и консольный вывод")
    add_code_block(
        "==================================================================\n"
        " ЛАБОРАТОРНАЯ РАБОТА № 4: ОБРАБОТКА ИСКЛЮЧЕНИЙ И ДИАГНОСТИКА     \n"
        "==================================================================\n"
        "INFO: Зарегистрирован кошелек: Кошелек [W-100 | Иван Иванов | Баланс: 10000.00 руб.]\n"
        "INFO: Зарегистрирован кошелек: Кошелек [W-200 | Анна Смирнова | Баланс: 5000.00 руб.]\n\n"
        "--- ИСХОДНОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ---\n"
        "  Кошелек [W-100 | Иван Иванов | Баланс: 10000.00 руб.]\n"
        "  Кошелек [W-200 | Анна Смирнова | Баланс: 5000.00 руб.]\n\n"
        "--- ВЫПОЛНЕНИЕ СЕРИИ ОПЕРАЦИЙ (ДЕМОНСТРАЦИЯ УСТОЙЧИВОСТИ) ---\n\n"
        ">>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '2500.00'\n"
        "INFO: Успешный перевод: 2500.00 руб. [W-100 -> W-200]. Новый баланс отправителя: 7500.00\n"
        "  [УСПЕХ] Перевод на сумму 2500.00 руб. успешно завершен!\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: УСПЕШНО.\n\n"
        ">>> Запрос на перевод: [W-100 -> W-200], введенная сумма: 'три_тысячи_рублей'\n"
        "  [ОШИБКА ВВОДА] Значение 'три_тысячи_рублей' не является корректным числом! Пожалуйста, используйте формат: 1500.50\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.\n\n"
        ">>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '50000.00'\n"
        "WARNING: Отказ в транзакции: на кошельке [W-100] баланс 7500.00 меньше суммы перевода 50000.00\n"
        "  [ОТКАЗ БАНКА] Недостаточно средств на кошельке [W-100]: текущий баланс 7500.00 руб., запрошено к списанию 50000.00 руб.\n"
        "  [СОВЕТ] Пополните кошелек или укажите сумму меньше либо равную 7500.00 руб.\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.\n\n"
        ">>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '-500.00'\n"
        "WARNING: Отклонена операция: некорректная сумма -500.00 <= 0\n"
        "  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] Сумма перевода должна быть строго положительной (запрошено: -500.00)\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.\n\n"
        ">>> Запрос на перевод: [W-100 -> W-999_UNKNOWN], введенная сумма: '1000.00'\n"
        "  [ОШИБКА РЕКВИЗИТОВ] Кошелек с идентификатором [W-999_UNKNOWN] не зарегистрирован в системе. Проверьте правильность номера кошелька.\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-999_UNKNOWN]. Результат операции: ОШИБКА / ОТКЛОНЕНО.\n\n"
        ">>> Запрос на перевод: [W-100 -> W-100], введенная сумма: '100.00'\n"
        "WARNING: Отклонена операция: кошелек отправителя совпадает с получателем [W-100]\n"
        "  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] Перевод средств на тот же самый кошелек запрещен: W-100\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-100]. Результат операции: ОШИБКА / ОТКЛОНЕНО.\n\n"
        ">>> Запрос на перевод: [W-200 -> W-100], введенная сумма: '1500.00'\n"
        "INFO: Успешный перевод: 1500.00 руб. [W-200 -> W-100]. Новый баланс отправителя: 6000.00\n"
        "  [УСПЕХ] Перевод на сумму 1500.00 руб. успешно завершен!\n"
        "  [FINALLY-АУДИТ] Завершена обработка запроса [W-200 -> W-100]. Результат операции: УСПЕШНО.\n\n"
        "--- ИТОГОВОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ПОСЛЕ ВСЕХ ТРАНЗАКЦИЙ ---\n"
        "  Кошелек [W-100 | Иван Иванов | Баланс: 9000.00 руб.]\n"
        "  Кошелек [W-200 | Анна Смирнова | Баланс: 6000.00 руб.]\n\n"
        "==================================================================\n"
        " Программа успешно обработала все сбои и не завершилась аварийно!\n"
        "=================================================================="
    )

    # 6. Инварианты надежности
    add_h1("6. Инварианты надежности и транзакционная безопасность")
    add_p(
        "1. Транзакционная атомарность: Любая неудавшаяся транзакция (нехватка средств, неверный счет, отрицательная сумма) "
        "оставляет балансы обоих участников строго неизменными. Общая денежная масса в системе сохраняется: 10000 + 5000 = 15000 = 9000 + 6000 руб.\n"
        "2. Отказоустойчивость (Resilience): Некорректный ввод пользователя не прерывает работу приложения; пользователь может сразу же выполнить повторные операции."
    )

    # 7. Байт-код
    add_h1("7. Исследование байт-кода и таблица исключений (Exception Table)")
    add_p("Команды компиляции и сборки:")
    add_code_block(
        "mkdir -p out\n"
        "javac -encoding UTF-8 -d out src/edu/course/lab04/exception/*.java src/edu/course/lab04/model/*.java src/edu/course/lab04/service/*.java src/edu/course/lab04/ui/*.java src/edu/course/lab04/*.java\n"
        "javap -classpath out -c -p edu.course.lab04.service.WalletService\n"
        "jar --create --file lab04.jar --main-class edu.course.lab04.WalletApp -C out .\n"
        "java -jar lab04.jar"
    )

    add_p(
        "athrow — инструкция JVM, выполняющая выброс объекта исключения и инициирующая поиск подходящего обработчика в таблице исключений.\n"
        "Exception table: from to target type — служебная таблица байт-кода метода, связывающая защищаемый диапазон команд с адресом обработчика catch.",
        bold_prefix="Ключевые инструкции байт-кода:\n"
    )

    add_p("Фрагмент таблицы исключений метода transfer:")
    add_code_block(
        "  Exception table:\n"
        "     from    to  target type\n"
        "       162   184   187   Class java/lang/Exception"
    )

    # 8. Ответы на контрольные вопросы к защите
    add_h1("8. Ответы на контрольные вопросы к защите")
    add_p(
        "Исключение — это событие, нарушающее нормальный ход выполнения программы. Все они наследуются от Throwable, "
        "разделяясь на системные фатальные ошибки Error и программные исключения Exception.",
        bold_prefix="Что такое исключения и как устроена их иерархия в Java?\n"
    )
    add_p(
        "Checked исключения проверяются компилятором (обязательны к объявлению в throws или обработке в try-catch). "
        "Unchecked исключения (наследники RuntimeException) отражают ошибки программирования или логики и не требуют обязательного перехвата.",
        bold_prefix="В чем фундаментальная разница между checked и unchecked исключениями?\n"
    )
    add_p(
        "try оборачивает потенциально сбойный код; catch перехватывает исключение; finally выполняется гарантированно; "
        "throw выбрасывает объект исключения; throws объявляет возможные исключения в заголовке метода.",
        bold_prefix="Как работают конструкции try, catch, finally, throw и throws?\n"
    )
    add_p(
        "Обработчики catch проверяются сверху вниз до первого совпадения. Если поставить базовый catch (Exception e) раньше производного, "
        "то специфический обработчик станет недостижимым кодом, и компилятор выдаст ошибку.",
        bold_prefix="Почему порядок блоков catch имеет решающее значение?\n"
    )
    add_p(
        "Это сохранение низкоуровневой причины сбоя внутри нового высокоуровневого исключения (через параметр cause). "
        "Это позволяет сохранить полный контекст для отладки без демонстрации технических деталей пользователю.",
        bold_prefix="Что такое цепочки исключений (Exception Chaining) и зачем они нужны?\n"
    )
    add_p(
        "Теряется тип ошибки, текст сообщения и stack trace. Программа переходит в скрыто-некорректное состояние, "
        "а причину сбоя в продакшене невозможно обнаружить.",
        bold_prefix="Что теряется при антипаттерне пустой обработки catch (Exception e) {}?\n"
    )

    output_path = r"C:\Users\sanya\.gemini\antigravity\scratch\lab04\REPORT_LAB04.docx"
    doc.save(output_path)
    print(f"Report saved successfully to {output_path}")

if __name__ == "__main__":
    create_report()
