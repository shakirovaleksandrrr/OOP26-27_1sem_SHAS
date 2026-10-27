ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 3
Дисциплина: Современные технологии объектно-ориентированного программирования
Тема: Наследование, интерфейсы и полиморфизм. Позднее связывание и контракт Object

1. Концепция наследования и абстракции в Java
1.1. Наследование (extends) и отношение IS-A
Наследование позволяет создать новый класс на базе уже существующего, перенимая его поля и методы. Отношение между классами выражается принципом «является» (IS-A): `IncomeTransaction` является `Transaction`. Ключевое слово `extends` задает прямого предка. В Java поддерживается только одиночное наследование классов (у класса может быть только один прямой родитель), но множественная реализация интерфейсов (`implements`).

Контрольный вопрос: В чем различие между абстрактным классом и интерфейсом?
Абстрактный класс задает общую сущность («кто я?», состояние и частичное поведение), определяет защищенные конструкторы, хранит изменяемые или неизменяемые поля (`id`, `date`, `amount`) и служит корневым типом родственной иерархии.
Интерфейс задает отдельный контракт («что я умею делать?», роль/возможность), не может иметь состояния (полей экземпляра) и может быть реализован любым, даже не родственным классом (например, `Commissionable` для операций с комиссией).

1.2. Проверка базовых контрактов без сторонних библиотек
Для проверки контрактов типов и работы полиморфизма в проект включен самодостаточный проверочный модуль `TransactionVerificationTest.java`, компилируемый стандартным JDK.

1.3. Ответы на вводные вопросы к Заданию
Почему нельзя создать экземпляр класса Transaction напрямую через new Transaction(...)?
Класс `Transaction` объявлен как `abstract`. Он представляет собой незавершенную концепцию транзакции без конкретного правила влияния на баланс (`balanceImpact()` не имеет тела). Попытка вызвать `new Transaction(...)` пресекается компилятором Java.

Какую роль играет ключевое слово super в конструкторах наследников?
Оно вызывает защищенный конструктор базового класса `Transaction`, гарантируя инициализацию и валидацию общих полей (`id`, `date`, `amount`) до выполнения логики наследника.

Что произойдет, если в наследнике не реализовать абстрактный метод balanceImpact()?
Компилятор выдаст ошибку: класс-наследник обязан либо реализовать все абстрактные методы предка, либо сам быть объявлен как `abstract`.

2. Архитектура иерархии типов и позднее связывание (Late Binding)
2.1. Схема работы иерархии программы

                     ┌───────────────────────┐
                     │   java.lang.Object    │
                     └───────────▲───────────┘
                                 │
                     ┌───────────┴───────────┐
                     │ Transaction (abstract)│
                     │  - id, date, amount   │
                     │  + balanceImpact()*   │
                     │  + equals/hashCode()  │
                     └───────────▲───────────┘
                                 │
          ┌──────────────────────┼──────────────────────┬──────────────────────┐
          │                      │                      │                      │
┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐  ┌─────────┴─────────┐
│ IncomeTransaction │  │ExpenseTransaction │  │TransferTransaction│  │CashbackTransaction│
│ (баланс: +amount) │  │ (баланс: -amount) │  │(баланс: -(a + c)) │  │(баланс: +cashback)│
└───────────────────┘  └───────────────────┘  └─────────▲─────────┘  └───────────────────┘
                                                        │
                                              implements│
                                              ┌─────────┴─────────┐
                                              │  Commissionable   │
                                              │   (интерфейс)     │
                                              │  + commission()*  │
                                              └───────────────────┘

2.2. Механизм позднего связывания (Late / Dynamic Binding)
При вызове `transaction.balanceImpact()` в цикле компилятор Java не знает, какой именно подкласс окажется в ссылке `Transaction`. В байт-код помещается инструкция `invokevirtual`. Во время выполнения виртуальная машина JVM обращается к виртуальной таблице методов (vtable) конкретного экземпляра в куче (Heap) и вызывает метод того класса, объект которого был фактически создан (`IncomeTransaction`, `ExpenseTransaction`, `TransferTransaction` или `CashbackTransaction`).

3. Сравнение механизмов Overloading и Overriding

| Характеристика | Перегрузка (Overloading) | Переопределение (Overriding) |
| :--- | :--- | :--- |
| Определение | Несколько методов с одним именем, но разными параметрами в одном классе | Замещение реализации метода родительского класса в классе-наследнике |
| Где объявляется | В пределах одного класса (или доступных из предка) | В подклассе (с той же сигнатурой) |
| Сигнатура метода | Обязательно разный состав / типы аргументов | Строго одинаковое имя и типы аргументов |
| Возвращаемый тип | Может быть любым | Совпадает или ковариантен (подтип) |
| Момент связывания | Compile-time (статическое / раннее связывание) | Runtime (динамическое / позднее связывание JVM) |
| Аннотация | Не используется | Рекомендуется `@Override` для контроля компилятора |
| Байт-код инструкция | `invokevirtual` / `invokestatic` с фиксированным типом | `invokevirtual` через динамическую таблицу vtable |

4. Практикум: Перегрузка методов и контракт Object
4.1. Демонстрация Overloading (файл TransactionPrinter.java)
```java
package edu.course.lab03.service;

import edu.course.lab03.model.Transaction;

public class TransactionPrinter {

    // Перегруженный вариант № 1 для одиночного объекта
    public void print(Transaction transaction) {
        if (transaction == null) {
            System.out.println("null transaction");
            return;
        }
        System.out.println("  [Единичная транзакция] " + transaction);
    }

    // Перегруженный вариант № 2 для массива объектов
    public void print(Transaction[] transactions) {
        if (transactions == null || transactions.length == 0) {
            System.out.println("Список транзакций пуст.");
            return;
        }
        System.out.printf("  [Список из %d транзакций]:%n", transactions.length);
        for (Transaction tx : transactions) {
            System.out.println("    * " + tx);
        }
    }
}
```

4.2. Контракт методов equals() и hashCode() в Transaction.java
Идентичность транзакции определяется ее уникальным бизнес-идентификатором `UUID id`.
```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
```
Анализ согласованности:
Контракт языка Java требует: если `a.equals(b) == true`, то `a.hashCode() == b.hashCode()` обязан возвращать одинаковое число. Если нарушить это правило, объекты потеряются в коллекциях `HashMap` или `HashSet`, так как они попадут в разные корзины (buckets) хеш-таблицы.

5. Интерфейс как гибкий контракт (файл Commissionable.java)
```java
package edu.course.lab03.model;

import java.math.BigDecimal;

public interface Commissionable {
    BigDecimal commission();
}
```
Анализ архитектурного решения:
Почему `Commissionable` нельзя делать суперклассом всей иерархии?
Комиссия присуща только определенным операциям (например, банковским переводам или валютному обмену). Доходные операции (зарплата) или внутренние начисления кэшбэка не имеют комиссии. Если поместить метод `commission()` в базовый класс `Transaction`, наследники без комиссии были бы вынуждены возвращать `0` или выбрасывать исключения (`UnsupportedOperationException`), что нарушило бы принцип разделения интерфейсов (Interface Segregation Principle, ISP) и принцип подстановки Барбары Лисков (LSP). Интерфейс подключается избирательно только туда, где комиссия существует.

6. Объектно-ориентированная реализация иерархии
6.1. Базовый абстрактный класс Transaction.java
```java
package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public abstract class Transaction {

    private final UUID id;
    private final LocalDate date;
    private final BigDecimal amount;

    protected Transaction(UUID id, LocalDate date, BigDecimal amount) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор транзакции не может быть null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата транзакции не может быть null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Сумма транзакции не может быть null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть строго положительной: " + amount);
        }
        this.id = id;
        this.date = date;
        this.amount = amount;
    }

    public final UUID getId() {
        return id;
    }

    public final LocalDate getDate() {
        return date;
    }

    public final BigDecimal getAmount() {
        return amount;
    }

    public abstract BigDecimal balanceImpact();

    public abstract String getTypeName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String sign = balanceImpact().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s | %s | %-8s | Сумма: %s | Влияние: %s%s",
                id, date, getTypeName(), amount, sign, balanceImpact());
    }
}
```

6.2. Классы-наследники
```java
package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class IncomeTransaction extends Transaction {

    public IncomeTransaction(UUID id, LocalDate date, BigDecimal amount) {
        super(id, date, amount);
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount();
    }

    @Override
    public String getTypeName() {
        return "Доход";
    }
}
```

```java
package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class ExpenseTransaction extends Transaction {

    public ExpenseTransaction(UUID id, LocalDate date, BigDecimal amount) {
        super(id, date, amount);
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount().negate();
    }

    @Override
    public String getTypeName() {
        return "Расход";
    }
}
```

```java
package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class TransferTransaction extends Transaction implements Commissionable {

    private final BigDecimal commissionFee;
    private final String recipientAccount;

    public TransferTransaction(UUID id, LocalDate date, BigDecimal amount, BigDecimal commissionFee, String recipientAccount) {
        super(id, date, amount);
        if (commissionFee == null || commissionFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер комиссии не может быть отрицательным или null");
        }
        if (recipientAccount == null || recipientAccount.isBlank()) {
            throw new IllegalArgumentException("Счет получателя не может быть пустым");
        }
        this.commissionFee = commissionFee;
        this.recipientAccount = recipientAccount;
    }

    @Override
    public BigDecimal commission() {
        return commissionFee;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount().add(commissionFee).negate();
    }

    @Override
    public String getTypeName() {
        return "Перевод";
    }

    @Override
    public String toString() {
        return String.format("%s [Получатель: %s, Комиссия: %s]",
                super.toString(), recipientAccount, commissionFee);
    }
}
```

6.3. Индивидуальное расширение (CashbackTransaction.java)
```java
package edu.course.lab03.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public final class CashbackTransaction extends Transaction {

    private final BigDecimal rate;
    private final BigDecimal originalPurchaseAmount;

    public CashbackTransaction(UUID id, LocalDate date, BigDecimal originalPurchaseAmount, BigDecimal rate) {
        super(id, date, calculateCashback(originalPurchaseAmount, rate));
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Ставка кэшбэка должна находиться в диапазоне (0, 1]: " + rate);
        }
        this.originalPurchaseAmount = originalPurchaseAmount;
        this.rate = rate;
    }

    private static BigDecimal calculateCashback(BigDecimal purchaseAmount, BigDecimal rate) {
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма покупки должна быть положительной");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ставка кэшбэка должна быть положительной");
        }
        return purchaseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getOriginalPurchaseAmount() {
        return originalPurchaseAmount;
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount();
    }

    @Override
    public String getTypeName() {
        return "Кэшбэк";
    }

    @Override
    public String toString() {
        return String.format("%s [Покупка: %s, Ставка: %.1f%%]",
                super.toString(), originalPurchaseAmount, rate.multiply(BigDecimal.valueOf(100)));
    }
}
```

6.4. Сервис полиморфного учета (AccountService.java)
```java
package edu.course.lab03.service;

import edu.course.lab03.model.Commissionable;
import edu.course.lab03.model.Transaction;

import java.math.BigDecimal;

public class AccountService {

    public BigDecimal calculateTotalBalanceImpact(Transaction[] transactions) {
        if (transactions == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            // Полиморфный вызов: позднее связывание без instanceof и switch!
            total = total.add(transaction.balanceImpact());
        }
        return total;
    }

    public BigDecimal calculateTotalCommission(Transaction[] transactions) {
        if (transactions == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction instanceof Commissionable commissionable) {
                totalCommission = totalCommission.add(commissionable.commission());
            }
        }
        return totalCommission;
    }
}
```

6.5. Главный класс приложения TransactionApp.java (Полиморфизм)
```java
package edu.course.lab03;

import edu.course.lab03.model.*;
import edu.course.lab03.service.AccountService;
import edu.course.lab03.service.TransactionPrinter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionApp {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 3: НАСЛЕДОВАНИЕ, ИНТЕРФЕЙСЫ И ПОЛИМОРФИЗМ ");
        System.out.println("==================================================================");

        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID id4 = UUID.fromString("44444444-4444-4444-4444-444444444444");

        LocalDate today = LocalDate.of(2026, 10, 27);

        Transaction[] transactions = new Transaction[] {
                new IncomeTransaction(id1, today, new BigDecimal("50000.00")),
                new ExpenseTransaction(id2, today, new BigDecimal("15000.00")),
                new TransferTransaction(id3, today, new BigDecimal("10000.00"), new BigDecimal("150.00"), "RU40817810000012345678"),
                new CashbackTransaction(id4, today, new BigDecimal("20000.00"), new BigDecimal("0.05"))
        };

        System.out.println("\n--- 1. Демонстрация Overloading (TransactionPrinter) ---");
        TransactionPrinter printer = new TransactionPrinter();
        printer.print(transactions[0]);
        printer.print(transactions);

        System.out.println("\n--- 2. Полиморфный расчет баланса (Late Binding) ---");
        AccountService accountService = new AccountService();

        BigDecimal totalImpact = accountService.calculateTotalBalanceImpact(transactions);
        BigDecimal totalCommission = accountService.calculateTotalCommission(transactions);

        for (Transaction tx : transactions) {
            String sign = tx.balanceImpact().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
            System.out.printf("  [%-8s] Сумма: %10s | Влияние на баланс: %s%10s%n",
                    tx.getTypeName(), tx.getAmount(), sign, tx.balanceImpact());
        }

        String totalSign = totalImpact.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        System.out.println("------------------------------------------------------------------");
        System.out.printf("Суммарное изменение баланса счета: %s%s руб.%n", totalSign, totalImpact);
        System.out.printf("Суммарная уплаченная комиссия:     %s руб.%n", totalCommission);
        System.out.println("------------------------------------------------------------------");

        System.out.println("\n--- 3. Проверка контракта Object (equals & hashCode) ---");
        Transaction t1 = new IncomeTransaction(id1, today, new BigDecimal("50000.00"));
        Transaction t1Clone = new IncomeTransaction(id1, today, new BigDecimal("50000.00"));
        Transaction tDifferent = new IncomeTransaction(UUID.randomUUID(), today, new BigDecimal("50000.00"));

        System.out.println("t1.equals(t1Clone): " + t1.equals(t1Clone) + " (ожидается true, т.к. ID одинаковы)");
        System.out.println("t1.hashCode() == t1Clone.hashCode(): " + (t1.hashCode() == t1Clone.hashCode()) + " (ожидается true)");
        System.out.println("t1.equals(tDifferent): " + t1.equals(tDifferent) + " (ожидается false, т.к. ID разные)");
        System.out.println("t1.toString(): " + t1);
    }
}
```

Вывод программы:
```text
==================================================================
 ЛАБОРАТОРНАЯ РАБОТА № 3: НАСЛЕДОВАНИЕ, ИНТЕРФЕЙСЫ И ПОЛИМОРФИЗМ 
==================================================================

--- 1. Демонстрация Overloading (TransactionPrinter) ---
Вызов print(Transaction) [раннее статическое связывание]:
  [Единичная транзакция] 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00

Вызов print(Transaction[]) [раннее статическое связывание]:
  [Список из 4 транзакций]:
    * 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00
    * 22222222-2222-2222-2222-222222222222 | 2026-10-27 | Расход   | Сумма: 15000.00 | Влияние: -15000.00
    * 33333333-3333-3333-3333-333333333333 | 2026-10-27 | Перевод  | Сумма: 10000.00 | Влияние: -10150.00 [Получатель: RU40817810000012345678, Комиссия: 150.00]
    * 44444444-4444-4444-4444-444444444444 | 2026-10-27 | Кэшбэк   | Сумма: 1000.00 | Влияние: +1000.00 [Покупка: 20000.00, Ставка: 5,0%]

--- 2. Полиморфный расчет баланса (Late Binding) ---
Расчет по отдельным операциям:
  [Доход   ] Сумма:   50000.00 | Влияние на баланс: +  50000.00
  [Расход  ] Сумма:   15000.00 | Влияние на баланс:   -15000.00
  [Перевод ] Сумма:   10000.00 | Влияние на баланс:   -10150.00
  [Кэшбэк  ] Сумма:    1000.00 | Влияние на баланс: +   1000.00
------------------------------------------------------------------
Суммарное изменение баланса счета: +25850.00 руб.
Суммарная уплаченная комиссия:     150.00 руб.
------------------------------------------------------------------

--- 3. Проверка контракта Object (equals & hashCode) ---
t1.equals(t1Clone): true (ожидается true, т.к. ID одинаковы)
t1.hashCode() == t1Clone.hashCode(): true (ожидается true)
t1.equals(tDifferent): false (ожидается false, т.к. ID разные)
t1.toString(): 11111111-1111-1111-1111-111111111111 | 2026-10-27 | Доход    | Сумма: 50000.00 | Влияние: +50000.00
```

7. Четыре принципа ООП в разработанной программе

| Принцип ООП | Где проявляется в программе | Практическая польза в архитектуре |
| :--- | :--- | :--- |
| **Абстракция** | Базовый класс `Transaction`, метод `balanceImpact()`, интерфейс `Commissionable` | Позволяет отделить контракт операции (влияние на баланс и комиссия) от деталей вычисления конкретных подтипов. |
| **Инкапсуляция** | Модификаторы `private final` для полей (`id`, `date`, `amount`, `commissionFee`), строгая валидация в конструкторах | Предотвращает невалидное состояние (null, отрицательные суммы) и защищает данные объекта от несанкционированного изменения извне. |
| **Наследование** | `extends Transaction` у классов `IncomeTransaction`, `ExpenseTransaction`, `TransferTransaction`, `CashbackTransaction` | Устраняет дублирование кода хранения идентификатора, даты и суммы, обеспечивая единое повторное использование базовой логики. |
| **Полиморфизм** | Вызов `transaction.balanceImpact()` в цикле по массиву `Transaction[]` | Позволяет агрегатору `AccountService` вычислять итоговый баланс для любых транзакций, не зная их конкретных классов и не требуя правок при добавлении новых типов (принцип OCP). |

8. Исследование байт-кода и сборка JAR
Команды компиляции и сборки:
```powershell
# Компиляция всех исходных файлов
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java src).FullName

# Исследование байт-кода полиморфного метода
javap -c -p out/edu/course/lab03/service/AccountService.class

# Сборка исполняемого архива JAR
jar --create --file lab03.jar --main-class edu.course.lab03.TransactionApp -C out .

# Запуск JAR
java -jar lab03.jar
```

Ключевые инструкции байт-кода:
`invokevirtual` — динамический вызов метода объекта через vtable во время выполнения (`late binding`), например `transaction.balanceImpact()`.
`invokespecial` — прямой вызов конструктора предка `super(...)` или приватного метода (статически разрешается компилятором).
`invokeinterface` — вызов метода через интерфейс, например `commissionable.commission()` (требует поиска реализации в таблице интерфейсов itable).
`invokestatic` — вызов статического вспомогательного метода (например, `Objects.equals(...)` или `Objects.hash(...)`).
`areturn` — возврат ссылки на объект (например, объекта `BigDecimal`) из метода.

Фрагмент дизассемблированного байт-кода метода `calculateTotalBalanceImpact`:
```text
34: aload_2
35: aload         6
37: invokevirtual #13  // Method edu/course/lab03/model/Transaction.balanceImpact:()Ljava/math/BigDecimal;
40: invokevirtual #19  // Method java/math/BigDecimal.add:(Ljava/math/BigDecimal;)Ljava/math/BigDecimal;
43: astore_2
```
Инструкция `37: invokevirtual` наглядно доказывает реализацию позднего связывания на уровне виртуальной машины Java.

9. Ответы на контрольные вопросы к защите
Что такое наследование и как используется extends?
Наследование — это механизм ООП, позволяющий классу перенять поля и методы другого класса. Ключевое слово `extends` указывает имя родительского класса, от которого строится новый подкласс.

Чем отличается перегрузка (overloading) от переопределения (overriding) методов?
Перегрузка — это создание методов с одним именем, но разными типами/количеством аргументов в одном классе; выбор нужного метода делает компилятор (раннее связывание). Переопределение — это изменение реализации родительского метода в подклассе с сохранением сигнатуры; выбор метода делает JVM во время работы (позднее связывание).

Что такое полиморфизм времени выполнения и позднее связывание?
Это способность программы вызывать метод конкретного объекта-наследника через ссылку на базовый тип. Позднее связывание означает, что фактический исполняемый код метода определяется JVM в рантайме по реальному типу созданного объекта в памяти.

Абстрактные классы и интерфейсы: в чем различия и когда что применять?
Абстрактный класс задает базовую сущность («кто это?») в иерархии родственных классов, может иметь конструкторы и переменные экземпляра. Интерфейс задает независимый контракт поведения («что умеет делать?»), поддерживающий множественную реализацию и не хранящий состояние.

Какую роль играет класс Object и методы toString(), equals(), hashCode()?
Класс `Object` — неявный корень всей иерархии классов в Java. Метод `toString()` возвращает строковое представление объекта; `equals()` определяет эквивалентность двух объектов по смыслу их данных; `hashCode()` генерирует целочисленный хеш для эффективного размещения объекта в хеш-таблицах (`HashMap`, `HashSet`).

Почему при переопределении equals() обязательно переопределять hashCode()?
Если два объекта равны по `equals()`, они обязаны иметь равные хеш-коды. Если не переопределить `hashCode()`, одинаковые по бизнес-логике объекты получат разные хеш-коды от `Object` (по адресам в памяти), и их невозможно будет корректно найти в `HashMap` или `HashSet`.

Что произойдет при попытке вызвать new для абстрактного класса?
Компилятор выдаст синтаксическую ошибку (`Transaction is abstract; cannot be instantiated`), так как создание экземпляра незавершенного класса запрещено правилами языка Java.
