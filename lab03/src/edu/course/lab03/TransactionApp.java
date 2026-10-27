package edu.course.lab03;

import edu.course.lab03.model.*;
import edu.course.lab03.service.AccountService;
import edu.course.lab03.service.TransactionPrinter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Главный класс приложения.
 * Демонстрация наследования, интерфейсов, полиморфизма и контракта Object.
 */
public class TransactionApp {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 3: НАСЛЕДОВАНИЕ, ИНТЕРФЕЙСЫ И ПОЛИМОРФИЗМ ");
        System.out.println("==================================================================");

        // 1. Создание полиморфного массива транзакций
        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID id4 = UUID.fromString("44444444-4444-4444-4444-444444444444");

        LocalDate today = LocalDate.of(2026, 10, 27);

        Transaction[] transactions = new Transaction[] {
                new IncomeTransaction(id1, today, new BigDecimal("50000.00")),
                new ExpenseTransaction(id2, today, new BigDecimal("15000.00")),
                new TransferTransaction(id3, today, new BigDecimal("10000.00"), new BigDecimal("150.00"), "RU40817810000012345678"),
                new CashbackTransaction(id4, today, new BigDecimal("20000.00"), new BigDecimal("0.05")) // 5% = 1000.00
        };

        // 2. Демонстрация Overloading (перегрузки методов)
        System.out.println("\n--- 1. Демонстрация Overloading (TransactionPrinter) ---");
        TransactionPrinter printer = new TransactionPrinter();

        System.out.println("Вызов print(Transaction) [раннее статическое связывание]:");
        printer.print(transactions[0]);

        System.out.println("\nВызов print(Transaction[]) [раннее статическое связывание]:");
        printer.print(transactions);

        // 3. Демонстрация полиморфизма и Late Binding
        System.out.println("\n--- 2. Полиморфный расчет баланса (Late Binding) ---");
        AccountService accountService = new AccountService();

        BigDecimal totalImpact = accountService.calculateTotalBalanceImpact(transactions);
        BigDecimal totalCommission = accountService.calculateTotalCommission(transactions);

        System.out.println("Расчет по отдельным операциям:");
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

        // 4. Демонстрация контракта Object: equals() и hashCode()
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
