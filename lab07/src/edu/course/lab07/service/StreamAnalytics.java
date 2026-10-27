package edu.course.lab07.service;

import edu.course.lab07.model.Category;
import edu.course.lab07.model.Transaction;
import edu.course.lab07.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Декларативная аналитика на основе функциональных интерфейсов и Stream API.
 * Каждый pipeline четко структурирован:
 * [Источник] -> [Промежуточные операции (Intermediate)] -> [Терминальная операция (Terminal)].
 */
public class StreamAnalytics {

    // ------------------------------------------------------------------------------------
    // 1. Стандартные функциональные интерфейсы Java
    // ------------------------------------------------------------------------------------

    /**
     * Predicate<Transaction>: предикат фильтрации (проверка условия -> boolean).
     */
    public static final Predicate<Transaction> IS_EXPENSE =
            tx -> tx.getType() == TransactionType.EXPENSE;

    /**
     * Function<Transaction, BigDecimal>: функция трансформации (отображение T -> R).
     */
    public static final Function<Transaction, BigDecimal> GET_AMOUNT =
            Transaction::getAmount;

    /**
     * Consumer<Transaction>: потребитель данных (выполнение действия над элементом).
     */
    public static final Consumer<Transaction> PRINT_TX =
            tx -> System.out.println("  [Consumer] " + tx);

    /**
     * Supplier<List<Transaction>>: поставщик данных (ленивая генерация результата).
     */
    public static final Supplier<List<Transaction>> EMPTY_TRANSACTIONS =
            ArrayList::new;

    // ------------------------------------------------------------------------------------
    // 2. Обязательные задачи Stream API
    // ------------------------------------------------------------------------------------

    /**
     * Задача 1: Найти все расходы за указанный период (включительно).
     * [Источник]: transactions.stream()
     * [Промежуточные]: filter(IS_EXPENSE), filter(date in range)
     * [Терминальная]: toList()
     */
    public List<Transaction> findExpensesForPeriod(List<Transaction> transactions, LocalDate from, LocalDate to) {
        if (transactions == null) return List.of();

        return transactions.stream()
                .filter(IS_EXPENSE)
                .filter(tx -> !tx.getDate().isBefore(from) && !tx.getDate().isAfter(to))
                .toList();
    }

    /**
     * Задача 2: Найти операции с суммой больше заданной величины.
     * [Источник]: transactions.stream()
     * [Промежуточные]: filter(amount > threshold)
     * [Терминальная]: toList()
     */
    public List<Transaction> findTransactionsGreaterThan(List<Transaction> transactions, BigDecimal minAmount) {
        if (transactions == null || minAmount == null) return List.of();

        return transactions.stream()
                .filter(tx -> tx.getAmount().compareTo(minAmount) > 0)
                .toList();
    }

    /**
     * Задача 3: Отсортировать операции по убыванию суммы.
     * [Источник]: transactions.stream()
     * [Промежуточные]: sorted(Comparator.comparing(Transaction::getAmount).reversed())
     * [Терминальная]: toList()
     */
    public List<Transaction> sortByAmountDescending(List<Transaction> transactions) {
        if (transactions == null) return List.of();

        return transactions.stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();
    }

    /**
     * Задача 4: Получить список описаний (наименований) операций.
     * [Источник]: transactions.stream()
     * [Промежуточные]: map(Transaction::getDescription)
     * [Терминальная]: toList()
     */
    public List<String> extractDescriptions(List<Transaction> transactions) {
        if (transactions == null) return List.of();

        return transactions.stream()
                .map(Transaction::getDescription)
                .filter(desc -> !desc.isBlank())
                .toList();
    }

    /**
     * Задача 5: Посчитать общую сумму всех расходов.
     * [Источник]: transactions.stream()
     * [Промежуточные]: filter(IS_EXPENSE), map(Transaction::getAmount)
     * [Терминальная]: reduce(BigDecimal.ZERO, BigDecimal::add)
     */
    public BigDecimal calculateTotalExpenses(List<Transaction> transactions) {
        if (transactions == null) return BigDecimal.ZERO;

        return transactions.stream()
                .filter(IS_EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Задача 6: Сгруппировать расходы по категории с подсчетом суммы.
     * [Источник]: transactions.stream()
     * [Промежуточные]: filter(IS_EXPENSE)
     * [Терминальная]: collect(groupingBy(category, reducing(sum)))
     */
    public Map<Category, BigDecimal> groupExpensesByCategory(List<Transaction> transactions) {
        if (transactions == null) return Map.of();

        return transactions.stream()
                .filter(IS_EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    /**
     * Задача 7: Найти категорию с максимальной суммой расходов.
     * [Источник]: groupExpensesByCategory(transactions).entrySet().stream()
     * [Промежуточные]: -
     * [Терминальная]: max(Map.Entry.comparingByValue())
     */
    public Optional<Map.Entry<Category, BigDecimal>> findCategoryWithMaxExpenses(List<Transaction> transactions) {
        Map<Category, BigDecimal> categoryTotals = groupExpensesByCategory(transactions);

        return categoryTotals.entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    /**
     * Задача 8: Получить пять крупнейших расходов.
     * [Источник]: transactions.stream()
     * [Промежуточные]: filter(IS_EXPENSE), sorted(amount desc), limit(5)
     * [Терминальная]: toList()
     */
    public List<Transaction> findTop5Expenses(List<Transaction> transactions) {
        if (transactions == null) return List.of();

        return transactions.stream()
                .filter(IS_EXPENSE)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .toList();
    }
}
