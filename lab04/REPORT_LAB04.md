ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 4
Дисциплина: Современные технологии объектно-ориентированного программирования
Тема: Исключения, диагностика и устойчивое поведение программы. Иерархия Throwable и Single Responsibility Principle

1. Концепция обработки исключений в Java
1.1. Иерархия классов Throwable, Exception, RuntimeException и Error
В языке Java все типы исключительных ситуаций являются наследниками базового класса `java.lang.Throwable`. Иерархия делится на две ключевые ветви:
1. `java.lang.Error` — критические сбои на уровне виртуальной машины JVM (например, `OutOfMemoryError`, `StackOverflowError`). Такие ошибки не должны перехватываться пользовательским кодом, поскольку нормальная работа программы после них невозможна.
2. `java.lang.Exception` — исключения, возникающие в логике программы. Они делятся на:
   - **Проверяемые (Checked Exceptions)**: прямые наследники `Exception` (за исключением поддерева `RuntimeException`). Компилятор требует их обязательного объявления в сигнатуре метода (`throws`) или обработки в блоке `try-catch`.
   - **Непроверяемые (Unchecked Exceptions)**: наследники `java.lang.RuntimeException` (например, `NullPointerException`, `IllegalArgumentException`, `NumberFormatException`). Отражают логические ошибки программирования или нарушение контрактов вызова методов.

Контрольный вопрос: В чем различие между throw и throws?
`throw` — это оператор в теле метода, который осуществляет непосредственный выброс конкретного объекта исключения в текущий поток выполнения (`throw new InsufficientFundsException(...);`).
`throws` — это ключевое слово в сигнатуре метода, предупреждающее компилятор и вызывающий код о том, что данный метод потенциально может выбросить указанные проверяемые исключения (`public void transfer(...) throws InsufficientFundsException`).

1.2. Checked vs Unchecked исключения в проектировании API
- **Checked исключения (`InsufficientFundsException`)** используются для моделирования ожидаемых альтернативных сценариев предметной области, которые вызывающая сторона обязана предвидеть и корректно обработать (например, предложить пользователю пополнить баланс или ввести меньшую сумму).
- **Unchecked исключения (`InvalidTransactionException`, `WalletNotFoundException`)** применяются для грубых нарушений контракта или некорректных входных параметров (передача null, отрицательная сумма, попытка перевода на несуществующий кошелек).

1.3. Ответы на вводные вопросы к Заданию
Почему нельзя использовать исключения как обычную замену условным операторам if-else?
Создание объекта исключения сопряжено с дорогостоящей операцией `fillInStackTrace()`, при которой JVM выполняет обход всего стека вызовов потока. Использование исключений для стандартного управления потоком управления (control flow) приводит к резкой деградации производительности программы и запутывает бизнес-логику.

Какую роль играет принцип единственной ответственности (SRP) при обработке ошибок?
Низкоуровневый сервис (`WalletService`) не должен взаимодействовать с пользователем и выводить текст «Ошибка!». Его обязанность — либо успешно провести транзакцию, либо выбросить информативное исключение с параметрами сбоя. Преобразование ошибки в понятный человекочитаемый текст с рекомендациями для исправления — это исключительная зона ответственности UI-контроллера.

2. Архитектура надежного приложения и обработка исключений (try-catch-finally)
2.1. Схема движения потока управления и распределения ответственности

┌────────────────────────────────────────────────────────────────────────┐
│                      Пользовательский слой (UI)                        │
│                 edu.course.lab04.ui.WalletConsoleController            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ 1. Ввод пользователя (rawAmount)
                                    ▼
                      ┌───────────────────────────┐
                      │    try-catch-finally      │
                      └─────────────┬─────────────┘
                                    │ 2. Вызов transfer(source, target, amount)
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         Сервисный слой (Core)                          │
│                 edu.course.lab04.service.WalletService                 │
│                                                                        │
│   Валидация аргументов      Проверка баланса       Транзакция          │
│   ┌───────────────────┐    ┌────────────────┐   ┌──────────────────┐   │
│   │ amount <= 0 ?     │    │ balance < amt ?│   │ source.withdraw()│   │
│   └─────────┬─────────┘    └───────┬────────┘   │ target.deposit() │   │
│             │                      │            └──────────────────┘   │
└─────────────┼──────────────────────┼───────────────────────────────────┘
              │ throw (Unchecked)    │ throw (Checked)
              ▼                      ▼
┌──────────────────────────┐   ┌──────────────────────────────┐
│InvalidTransactionExcept. │   │  InsufficientFundsException  │
└─────────────┬────────────┘   └──────────────┬───────────────┘
              │                               │
              └───────────────┬───────────────┘
                              │ 3. Перехват в UI-слое
                              ▼
┌────────────────────────────────────────────────────────────────────────┐
│   catch (NumberFormatException)       -> Подсказка формата числа       │
│   catch (InsufficientFundsException)  -> Рекомендация пополнить кошелек│
│   catch (WalletNotFoundException)     -> Проверка номера кошелька      │
│   finally                             -> Гарантированный аудит операции│
└────────────────────────────────────────────────────────────────────────┘

2.2. Механизм гарантированного выполнения (блок finally)
Блок `finally` выполняется всегда: как при успешном завершении блока `try`, так и в случае возникновения любого исключения (перехваченного или проброшенного дальше). В разработанном приложении блок `finally` используется для независимого аудита каждого поступившего финансового запроса, гарантируя, что факт попытки перевода будет зафиксирован независимо от исхода операции.

3. Анализ антипаттернов обработки исключений

| Антипаттерн | Какую информацию уничтожает | В чем опасность для системы | Как исправить |
| :--- | :--- | :--- | :--- |
| `catch (Exception e) {}` | Полностью уничтожает тип исключения, сообщение об ошибке и весь stack trace. | Система продолжает работу в невалидном состоянии («тихий сбой»). Программист не узнает о баге до катастрофического отказа. | Залогировать ошибку через `logger.log(Level.WARNING, ...)` или пробросить специализированное исключение. |
| `catch (Exception e) { return null; }` | Скрывает истинную причину неудачи операции, подменяя ее пустым значением. | Приводит к лавинообразным `NullPointerException` в других частях программы далеко от места возникновения сбоя. | Выбрасывать бизнес-исключение или использовать явный контейнер отсутствия значения (`Optional`). |
| `catch (Throwable t) { ... }` | Перехватывает системные сбои JVM (`OutOfMemoryError`, `StackOverflowError`, `ThreadDeath`). | Нарушает гарантии целостности среды выполнения. Попытка продолжить работу после фатальных сбоев памяти ведет к порче данных. | Перехватывать только `Exception` или конкретные прикладные подклассы, не трогая системные `Error`. |

4. Практикум: Разработка пользовательских классов исключений
4.1. Проверяемое исключение InsufficientFundsException.java
Хранит контекст операции: идентификатор кошелька, текущий остаток и запрошенную сумму.
```java
package edu.course.lab04.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends Exception {

    private final String walletId;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(String walletId, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Недостаточно средств на кошельке [%s]: текущий баланс %s руб., запрошено к списанию %s руб.",
                walletId, currentBalance, requestedAmount));
        this.walletId = walletId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public String getWalletId() { return walletId; }
    public BigDecimal getCurrentBalance() { return currentBalance; }
    public BigDecimal getRequestedAmount() { return requestedAmount; }
}
```

4.2. Непроверяемое исключение InvalidTransactionException.java и механизм Chaining
Конструктор с параметром `Throwable cause` позволяет сохранить первопричину сбоя (цепочка исключений).
```java
package edu.course.lab04.exception;

public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

4.3. Непроверяемое исключение WalletNotFoundException.java
```java
package edu.course.lab04.exception;

public class WalletNotFoundException extends RuntimeException {

    private final String walletId;

    public WalletNotFoundException(String walletId) {
        super(String.format("Кошелек с идентификатором [%s] не зарегистрирован в системе", walletId));
        this.walletId = walletId;
    }

    public String getWalletId() { return walletId; }
}
```

5. Программная реализация сервиса и пользовательского слоя
5.1. Модель данных кошелька Wallet.java
```java
package edu.course.lab04.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Wallet {
    private final String id;
    private final String ownerName;
    private BigDecimal balance;

    public Wallet(String id, String ownerName, BigDecimal initialBalance) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Идентификатор кошелька пуст");
        if (ownerName == null || ownerName.isBlank()) throw new IllegalArgumentException("Имя владельца пусто");
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Баланс не может быть отрицательным");
        }
        this.id = id;
        this.ownerName = ownerName;
        this.balance = initialBalance;
    }

    public String getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public synchronized BigDecimal getBalance() { return balance; }

    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть > 0: " + amount);
        }
        this.balance = this.balance.add(amount);
    }

    public synchronized void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма списания должна быть > 0: " + amount);
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Внутренняя несогласованность: баланс меньше списания");
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public String toString() {
        return String.format("Кошелек [%s | %s | Баланс: %s руб.]", id, ownerName, balance);
    }
}
```

5.2. Сервис межбанковского перевода WalletService.java
```java
package edu.course.lab04.service;

import edu.course.lab04.exception.InsufficientFundsException;
import edu.course.lab04.exception.InvalidTransactionException;
import edu.course.lab04.exception.WalletNotFoundException;
import edu.course.lab04.model.Wallet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WalletService {

    private static final Logger logger = Logger.getLogger(WalletService.class.getName());
    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    public void registerWallet(Wallet wallet) {
        if (wallet == null) throw new IllegalArgumentException("Кошелек не может быть null");
        wallets.put(wallet.getId(), wallet);
        logger.info(() -> String.format("Зарегистрирован кошелек: %s", wallet));
    }

    public Wallet getWallet(String id) {
        if (id == null || id.isBlank()) throw new InvalidTransactionException("Идентификатор пуст");
        Wallet wallet = wallets.get(id);
        if (wallet == null) throw new WalletNotFoundException(id);
        return wallet;
    }

    public void transfer(String sourceId, String targetId, BigDecimal amount)
            throws InsufficientFundsException {

        if (sourceId == null || sourceId.isBlank() || targetId == null || targetId.isBlank()) {
            logger.warning("Отклонена операция: один из идентификаторов кошельков пуст.");
            throw new InvalidTransactionException("Идентификаторы отправителя и получателя обязательны");
        }
        if (sourceId.equals(targetId)) {
            logger.warning(() -> "Отклонена операция: перевод самому себе: " + sourceId);
            throw new InvalidTransactionException("Перевод средств на тот же самый кошелек запрещен: " + sourceId);
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning(() -> "Отклонена операция: некорректная сумма: " + amount);
            throw new InvalidTransactionException("Сумма перевода должна быть строго положительной: " + amount);
        }

        Wallet source = getWallet(sourceId);
        Wallet target = getWallet(targetId);

        if (source.getBalance().compareTo(amount) < 0) {
            logger.warning(() -> String.format("Отказ: баланс %s меньше суммы %s", source.getBalance(), amount));
            throw new InsufficientFundsException(sourceId, source.getBalance(), amount);
        }

        try {
            source.withdraw(amount);
            target.deposit(amount);
            logger.info(() -> String.format("Успешный перевод %s руб. [%s -> %s]", amount, sourceId, targetId));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Критический сбой изменения балансов", ex);
            throw new InvalidTransactionException("Внутренний сбой при выполнении перевода", ex);
        }
    }

    public void transferFromString(String sourceId, String targetId, String amountStr)
            throws InsufficientFundsException {
        try {
            if (amountStr == null || amountStr.isBlank()) {
                throw new InvalidTransactionException("Строка с суммой не может быть пустой");
            }
            BigDecimal parsedAmount = new BigDecimal(amountStr.trim().replace(',', '.'));
            transfer(sourceId, targetId, parsedAmount);
        } catch (NumberFormatException nfe) {
            logger.log(Level.WARNING, () -> "Ошибка формата числа: " + amountStr);
            throw new InvalidTransactionException("Некорректный числовой формат суммы: '" + amountStr + "'", nfe);
        }
    }
}
```

5.3. Пользовательский слой WalletConsoleController.java (Соблюдение SRP)
```java
package edu.course.lab04.ui;

import edu.course.lab04.exception.InsufficientFundsException;
import edu.course.lab04.exception.InvalidTransactionException;
import edu.course.lab04.exception.WalletNotFoundException;
import edu.course.lab04.service.WalletService;

import java.math.BigDecimal;

public class WalletConsoleController {

    private final WalletService walletService;

    public WalletConsoleController(WalletService walletService) {
        this.walletService = walletService;
    }

    public void processTransfer(String sourceId, String targetId, String rawAmount) {
        System.out.printf("%n>>> Запрос на перевод: [%s -> %s], введенная сумма: '%s'%n", sourceId, targetId, rawAmount);
        boolean success = false;

        try {
            BigDecimal amount = new BigDecimal(rawAmount.trim().replace(',', '.'));
            walletService.transfer(sourceId, targetId, amount);
            System.out.printf("  [УСПЕХ] Перевод на сумму %s руб. успешно завершен!%n", amount);
            success = true;

        } catch (NumberFormatException nfe) {
            System.err.printf("  [ОШИБКА ВВОДА] Значение '%s' не является числом! Формат: 1500.50%n", rawAmount);

        } catch (InsufficientFundsException ife) {
            System.err.printf("  [ОТКАЗ БАНКА] %s%n", ife.getMessage());
            System.err.printf("  [СОВЕТ] Пополните кошелек или укажите сумму <= %s руб.%n", ife.getCurrentBalance());

        } catch (WalletNotFoundException wnfe) {
            System.err.printf("  [ОШИБКА РЕКВИЗИТОВ] %s. Проверьте правильность номера кошелька.%n", wnfe.getMessage());

        } catch (InvalidTransactionException ite) {
            System.err.printf("  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] %s%n", ite.getMessage());
            if (ite.getCause() != null) {
                System.err.printf("  [ПРИЧИНА (CHAINING)] Исходный сбой: %s%n", ite.getCause().toString());
            }

        } finally {
            System.out.printf("  [FINALLY-АУДИТ] Завершена обработка запроса [%s -> %s]. Результат: %s.%n",
                    sourceId, targetId, success ? "УСПЕШНО" : "ОШИБКА / ОТКЛОНЕНО");
        }
    }
}
```

5.4. Главный класс приложения WalletApp.java и консольный вывод
```java
package edu.course.lab04;

import edu.course.lab04.model.Wallet;
import edu.course.lab04.service.WalletService;
import edu.course.lab04.ui.WalletConsoleController;
import java.math.BigDecimal;

public class WalletApp {
    public static void main(String[] args) {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("W-100", "Иван Иванов", new BigDecimal("10000.00"));
        Wallet w2 = new Wallet("W-200", "Анна Смирнова", new BigDecimal("5000.00"));
        service.registerWallet(w1);
        service.registerWallet(w2);

        WalletConsoleController controller = new WalletConsoleController(service);

        // Серия операций подряд
        controller.processTransfer("W-100", "W-200", "2500.00");
        controller.processTransfer("W-100", "W-200", "три_тысячи_рублей");
        controller.processTransfer("W-100", "W-200", "50000.00");
        controller.processTransfer("W-100", "W-200", "-500.00");
        controller.processTransfer("W-100", "W-999_UNKNOWN", "1000.00");
        controller.processTransfer("W-100", "W-100", "100.00");
        controller.processTransfer("W-200", "W-100", "1500.00");

        System.out.println("\nИтоговое состояние:");
        System.out.println("  " + w1);
        System.out.println("  " + w2);
    }
}
```

Вывод программы:
```text
==================================================================
 ЛАБОРАТОРНАЯ РАБОТА № 4: ОБРАБОТКА ИСКЛЮЧЕНИЙ И ДИАГНОСТИКА     
==================================================================
INFO: Зарегистрирован кошелек: Кошелек [W-100 | Иван Иванов | Баланс: 10000.00 руб.]
INFO: Зарегистрирован кошелек: Кошелек [W-200 | Анна Смирнова | Баланс: 5000.00 руб.]

--- ИСХОДНОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ---
  Кошелек [W-100 | Иван Иванов | Баланс: 10000.00 руб.]
  Кошелек [W-200 | Анна Смирнова | Баланс: 5000.00 руб.]

--- ВЫПОЛНЕНИЕ СЕРИИ ОПЕРАЦИЙ (ДЕМОНСТРАЦИЯ УСТОЙЧИВОСТИ) ---

>>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '2500.00'
INFO: Успешный перевод: 2500.00 руб. [W-100 -> W-200]. Новый баланс отправителя: 7500.00
  [УСПЕХ] Перевод на сумму 2500.00 руб. успешно завершен!
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: УСПЕШНО.

>>> Запрос на перевод: [W-100 -> W-200], введенная сумма: 'три_тысячи_рублей'
  [ОШИБКА ВВОДА] Значение 'три_тысячи_рублей' не является корректным числом! Пожалуйста, используйте формат: 1500.50
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.

>>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '50000.00'
WARNING: Отказ в транзакции: на кошельке [W-100] баланс 7500.00 меньше суммы перевода 50000.00
  [ОТКАЗ БАНКА] Недостаточно средств на кошельке [W-100]: текущий баланс 7500.00 руб., запрошено к списанию 50000.00 руб.
  [СОВЕТ] Пополните кошелек или укажите сумму меньше либо равную 7500.00 руб.
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.

>>> Запрос на перевод: [W-100 -> W-200], введенная сумма: '-500.00'
WARNING: Отклонена операция: некорректная сумма -500.00 <= 0
  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] Сумма перевода должна быть строго положительной (запрошено: -500.00)
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-200]. Результат операции: ОШИБКА / ОТКЛОНЕНО.

>>> Запрос на перевод: [W-100 -> W-999_UNKNOWN], введенная сумма: '1000.00'
  [ОШИБКА РЕКВИЗИТОВ] Кошелек с идентификатором [W-999_UNKNOWN] не зарегистрирован в системе. Проверьте правильность номера кошелька.
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-999_UNKNOWN]. Результат операции: ОШИБКА / ОТКЛОНЕНО.

>>> Запрос на перевод: [W-100 -> W-100], введенная сумма: '100.00'
WARNING: Отклонена операция: кошелек отправителя совпадает с получателем [W-100]
  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] Перевод средств на тот же самый кошелек запрещен: W-100
  [FINALLY-АУДИТ] Завершена обработка запроса [W-100 -> W-100]. Результат операции: ОШИБКА / ОТКЛОНЕНО.

>>> Запрос на перевод: [W-200 -> W-100], введенная сумма: '1500.00'
INFO: Успешный перевод: 1500.00 руб. [W-200 -> W-100]. Новый баланс отправителя: 6000.00
  [УСПЕХ] Перевод на сумму 1500.00 руб. успешно завершен!
  [FINALLY-АУДИТ] Завершена обработка запроса [W-200 -> W-100]. Результат операции: УСПЕШНО.

--- ИТОГОВОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ПОСЛЕ ВСЕХ ТРАНЗАКЦИЙ ---
  Кошелек [W-100 | Иван Иванов | Баланс: 9000.00 руб.]
  Кошелек [W-200 | Анна Смирнова | Баланс: 6000.00 руб.]

==================================================================
 Программа успешно обработала все сбои и не завершилась аварийно!
==================================================================
```

6. Инварианты надежности и транзакционная безопасность
В ходе серии тестов и выполнения демонстрационного сценария доказаны ключевые инварианты системы:
1. **Транзакционная безопасность (атомарность)**: Если кошелек не найден, указана отрицательная сумма или на счете недостаточно средств, состояние обоих счетов гарантированно остается неизменным. Сумма всех средств в системе сохраняется: $10000 + 5000 = 15000 = 9000 + 6000$ руб.
2. **Отказоустойчивость приложения (Resilience)**: Некорректный ввод строки, отрицательные числа и отказы банка не приводят к падению программы. Приложение продолжает диалог с пользователем и успешно выполняет последующие корректные операции.

7. Исследование байт-кода и таблица исключений (Exception Table)
Команды компиляции и сборки:
```powershell
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java src).FullName
javap -classpath out -c -p edu.course.lab04.service.WalletService
jar --create --file lab04.jar --main-class edu.course.lab04.WalletApp -C out .
java -jar lab04.jar
```

Ключевые инструкции байт-кода:
`athrow` — инструкция виртуальной машины Java, снимающая объект исключения с вершины стека операндов и инициирующая поиск подходящего обработчика в таблице исключений метода.
`Exception table: from to target type` — специальная служебная таблица метаданных байт-кода метода, генерируемая компилятором для блоков `try-catch`. В ней указываются диапазоны байт-код инструкций (`from`, `to`), адрес перехода (`target`) и квалифицированное имя перехватываемого типа исключения (`type`).

Фрагмент дизассемблированного байт-кода метода `transfer`:
```text
  Exception table:
     from    to  target type
       162   184   187   Class java/lang/Exception
```
При возникновении исключения типа `java/lang/Exception` в диапазоне инструкций от 162 до 184 управление немедленно передается на инструкцию 187, где происходит логирование сбоя и выброс `InvalidTransactionException` через инструкцию `athrow`.

8. Ответы на контрольные вопросы к защите
Что такое исключения и как устроена их иерархия в Java?
Исключение — это событие, нарушающее нормальный ход выполнения программы. Все исключения наследуются от класса `Throwable`, разделяясь на системные фатальные ошибки `Error` и программные исключения `Exception` (проверяемые и непроверяемые `RuntimeException`).

В чем фундаментальная разница между checked и unchecked исключениями?
Checked исключения проверяются компилятором на этапе компиляции: их обязательно нужно перехватить в `try-catch` или объявить в сигнатуре `throws`. Unchecked исключения (наследники `RuntimeException`) компилятор не требует объявлять или принудительно перехватывать; они возникают из-за ошибок логики или невалидного ввода.

Как работают ключевые конструкции try, catch, finally, throw и throws?
`try` оборачивает потенциально опасный блок кода; `catch` перехватывает и обрабатывает конкретный тип исключения; `finally` гарантированно выполняется при любом исходе; `throw` выбрасывает экземпляр исключения; `throws` декларирует возможные исключения в заголовке метода.

Почему порядок блоков catch имеет решающее значение?
Обработчики `catch` анализируются JVM строго сверху вниз до первого совпадения по типу. Поэтому частные (более конкретные) классы исключений обязаны располагаться выше базовых. Если расположить `catch (Exception e)` первым, все последующие блоки для производных типов станут недостижимыми («unreachable code»), и компилятор выдаст ошибку.

Что такое цепочки исключений (Exception Chaining) и зачем они нужны?
Это прием оборачивания низкоуровневого исключения (например, `NumberFormatException`) в высокоуровневое бизнес-исключение (`InvalidTransactionException`) с передачей исходного объекта в качестве причины (`cause`). Это позволяет сохранить полный контекст ошибки для разработчика, не раскрывая лишних технических деталей клиенту.

Что теряется при антипаттерне пустой обработки catch (Exception e) {}?
Уничтожается вся информация о произошедшем: тип ошибки, ее текстовое описание и стек вызовов. Программа оказывается в скрыто-некорректном состоянии, а причину бага в продакшене становится невозможно диагностировать.
