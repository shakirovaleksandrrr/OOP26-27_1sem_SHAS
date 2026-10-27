package edu.course.lab03;

import edu.course.lab03.model.*;
import edu.course.lab03.service.AccountService;

import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Автоматические верификационные тесты для проверки требований Лабораторной работы № 3:
 * 1. Невозможность прямого создания экземпляра абстрактного класса Transaction.
 * 2. Корректность вызова balanceImpact() для каждого подкласса.
 * 3. Полиморфный расчет без изменения агрегирующего алгоритма.
 * 4. Корректность контракта equals() и hashCode().
 * 5. Контракт интерфейса Commissionable.
 */
public class TransactionVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println(">>> Запуск проверочных тестов Лабораторной работы № 3 <<<");

        testAbstractClassConstraint();
        testIncomeTransactionImpact();
        testExpenseTransactionImpact();
        testTransferTransactionImpactAndCommission();
        testCashbackTransactionImpact();
        testPolymorphicAggregatorWithoutModification();
        testEqualsAndHashCodeContract();

        System.out.println("---------------------------------------------------------");
        System.out.printf("Результаты тестов: Всего: %d, Успешно: %d, Провалено: %d%n",
                testsRun, testsPassed, testsRun - testsPassed);

        if (testsRun != testsPassed) {
            System.err.println("!!! Есть проваленные тесты!");
            System.exit(1);
        } else {
            System.out.println(">>> Все требования успешно подтверждены! <<<");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testAbstractClassConstraint() {
        boolean isAbstract = Modifier.isAbstract(Transaction.class.getModifiers());
        assertTrue("Класс Transaction должен быть объявлен как abstract", isAbstract);
    }

    private static void testIncomeTransactionImpact() {
        IncomeTransaction income = new IncomeTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("1000.00"));
        assertTrue("IncomeTransaction balanceImpact() == +amount",
                income.balanceImpact().compareTo(new BigDecimal("1000.00")) == 0);
    }

    private static void testExpenseTransactionImpact() {
        ExpenseTransaction expense = new ExpenseTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("450.00"));
        assertTrue("ExpenseTransaction balanceImpact() == -amount",
                expense.balanceImpact().compareTo(new BigDecimal("-450.00")) == 0);
    }

    private static void testTransferTransactionImpactAndCommission() {
        TransferTransaction transfer = new TransferTransaction(
                UUID.randomUUID(), LocalDate.now(),
                new BigDecimal("2000.00"), new BigDecimal("50.00"), "ACC123"
        );
        assertTrue("TransferTransaction реализует Commissionable", transfer instanceof Commissionable);
        assertTrue("TransferTransaction commission() == 50.00",
                transfer.commission().compareTo(new BigDecimal("50.00")) == 0);
        assertTrue("TransferTransaction balanceImpact() == -(amount + commission)",
                transfer.balanceImpact().compareTo(new BigDecimal("-2050.00")) == 0);
    }

    private static void testCashbackTransactionImpact() {
        CashbackTransaction cashback = new CashbackTransaction(
                UUID.randomUUID(), LocalDate.now(),
                new BigDecimal("10000.00"), new BigDecimal("0.05") // 5% от 10000 = 500
        );
        assertTrue("CashbackTransaction balanceImpact() == +cashbackAmount",
                cashback.balanceImpact().compareTo(new BigDecimal("500.00")) == 0);
    }

    private static void testPolymorphicAggregatorWithoutModification() {
        AccountService service = new AccountService();
        Transaction[] txs = new Transaction[] {
                new IncomeTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("1000.00")),
                new ExpenseTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("300.00")),
                new TransferTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("200.00"), new BigDecimal("20.00"), "ACC1"),
                new CashbackTransaction(UUID.randomUUID(), LocalDate.now(), new BigDecimal("4000.00"), new BigDecimal("0.10")) // 400.00
        };

        // 1000 - 300 - 220 + 400 = 880.00
        BigDecimal total = service.calculateTotalBalanceImpact(txs);
        assertTrue("Полиморфный расчет общего баланса (1000 - 300 - 220 + 400 = 880.00)",
                total.compareTo(new BigDecimal("880.00")) == 0);
    }

    private static void testEqualsAndHashCodeContract() {
        UUID sharedId = UUID.randomUUID();
        LocalDate now = LocalDate.now();

        Transaction tx1 = new IncomeTransaction(sharedId, now, new BigDecimal("100.00"));
        Transaction tx2 = new IncomeTransaction(sharedId, now, new BigDecimal("200.00")); // та же идентичность по ID
        Transaction tx3 = new IncomeTransaction(UUID.randomUUID(), now, new BigDecimal("100.00"));

        assertTrue("equals() возвращает true для объектов с одинаковым id", tx1.equals(tx2));
        assertTrue("hashCode() совпадает для объектов, равных по equals()", tx1.hashCode() == tx2.hashCode());
        assertTrue("equals() возвращает false для объектов с разными id", !tx1.equals(tx3));
    }
}
