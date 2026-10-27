package edu.course.lab07;

import edu.course.lab07.model.Category;
import edu.course.lab07.model.Transaction;
import edu.course.lab07.model.TransactionType;
import edu.course.lab07.service.ImperativeAnalytics;
import edu.course.lab07.service.StreamAnalytics;
import edu.course.lab07.service.StreamExperiments;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Главный класс приложения.
 * Демонстрация функционального программирования, Stream API и аналитических конвейеров.
 */
public class StreamApp {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 7: STREAM API И ФУНКЦИОНАЛЬНЫЕ ИНТЕРФЕЙСЫ  ");
        System.out.println("==================================================================");

        // 1. Инициализация категорий
        Category catFood = new Category("CAT-01", "Продукты");
        Category catTransport = new Category("CAT-02", "Транспорт");
        Category catSalary = new Category("CAT-03", "Зарплата");
        Category catGadgets = new Category("CAT-04", "Электроника");
        Category catRent = new Category("CAT-05", "Жилье");

        // 2. Инициализация набора данных
        List<Transaction> transactions = List.of(
                new Transaction(UUID.fromString("11111111-1111-1111-1111-111111111111"), LocalDate.of(2026, 9, 1), TransactionType.EXPENSE, new BigDecimal("1200.00"), catFood, "Супермаркет у дома"),
                new Transaction(UUID.fromString("22222222-2222-2222-2222-222222222222"), LocalDate.of(2026, 9, 2), TransactionType.EXPENSE, new BigDecimal("350.00"), catTransport, "Билет на поезд"),
                new Transaction(UUID.fromString("33333333-3333-3333-3333-333333333333"), LocalDate.of(2026, 9, 5), TransactionType.INCOME, new BigDecimal("95000.00"), catSalary, "Зарплата за август"),
                new Transaction(UUID.fromString("44444444-4444-4444-4444-444444444444"), LocalDate.of(2026, 9, 7), TransactionType.EXPENSE, new BigDecimal("45000.00"), catRent, "Аренда квартиры"),
                new Transaction(UUID.fromString("55555555-5555-5555-5555-555555555555"), LocalDate.of(2026, 9, 10), TransactionType.EXPENSE, new BigDecimal("3200.50"), catFood, "Мясная лавка"),
                new Transaction(UUID.fromString("66666666-6666-6666-6666-666666666666"), LocalDate.of(2026, 9, 12), TransactionType.EXPENSE, new BigDecimal("12500.00"), catGadgets, "Беспроводные наушники"),
                new Transaction(UUID.fromString("77777777-7777-7777-7777-777777777777"), LocalDate.of(2026, 9, 15), TransactionType.EXPENSE, new BigDecimal("850.00"), catTransport, "Такси в аэропорт"),
                new Transaction(UUID.fromString("88888888-8888-8888-8888-888888888888"), LocalDate.of(2026, 9, 20), TransactionType.EXPENSE, new BigDecimal("650.00"), catFood, "Кофейня")
        );

        System.out.println("\n--- ИСХОДНЫЙ СПИСОК ОПЕРАЦИЙ (8 ТРАНЗАКЦИЙ) ---");
        transactions.forEach(StreamAnalytics.PRINT_TX);

        // 3. Сравнение: Императивный vs Декларативный подход
        System.out.println("\n--- 1. Сравнение подходов (Императивный vs Stream API) ---");
        ImperativeAnalytics imperative = new ImperativeAnalytics();
        StreamAnalytics streamAnalytics = new StreamAnalytics();

        BigDecimal impTotal = imperative.totalByCategory(transactions, catFood);
        BigDecimal strTotal = streamAnalytics.groupExpensesByCategory(transactions).getOrDefault(catFood, BigDecimal.ZERO);
        System.out.printf("Сумма по категории 'Продукты':%n");
        System.out.printf("  * Императивно (цикл for + accumulator): %s руб.%n", impTotal);
        System.out.printf("  * Декларативно (filter + collect/reduce): %s руб.%n", strTotal);

        // 4. Демонстрация 8 обязательных задач Stream API
        System.out.println("\n--- 2. Выполнение 8 аналитических задач Stream API ---");

        // Задача 1: Расходы за период 1-10 сентября
        System.out.println("\nЗадача 1: Все расходы с 2026-09-01 по 2026-09-10:");
        List<Transaction> periodExpenses = streamAnalytics.findExpensesForPeriod(
                transactions, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10));
        periodExpenses.forEach(t -> System.out.println("  " + t));

        // Задача 2: Операции больше 10 000 руб.
        System.out.println("\nЗадача 2: Операции на сумму более 10 000 руб.:");
        streamAnalytics.findTransactionsGreaterThan(transactions, new BigDecimal("10000.00"))
                .forEach(t -> System.out.println("  " + t));

        // Задача 3: Сортировка по убыванию суммы
        System.out.println("\nЗадача 3: Топ операций по убыванию суммы:");
        streamAnalytics.sortByAmountDescending(transactions).stream().limit(3)
                .forEach(t -> System.out.println("  " + t));

        // Задача 4: Список описаний
        System.out.println("\nЗадача 4: Список описаний (map -> String):");
        System.out.println("  " + streamAnalytics.extractDescriptions(transactions));

        // Задача 5: Сумма всех расходов
        BigDecimal totalExpenses = streamAnalytics.calculateTotalExpenses(transactions);
        System.out.println("\nЗадача 5: Общая сумма расходов (reduce): " + totalExpenses + " руб.");

        // Задача 6: Группировка расходов по категориям
        System.out.println("\nЗадача 6: Группировка расходов по категориям (groupingBy):");
        Map<Category, BigDecimal> categoryExpenses = streamAnalytics.groupExpensesByCategory(transactions);
        categoryExpenses.forEach((cat, sum) -> System.out.printf("  * %-12s: %10s руб.%n", cat.getName(), sum));

        // Задача 7: Категория с максимальными расходами
        System.out.println("\nЗадача 7: Категория с максимальными расходами:");
        streamAnalytics.findCategoryWithMaxExpenses(transactions)
                .ifPresent(e -> System.out.printf("  * %s (Сумма: %s руб.)%n", e.getKey().getName(), e.getValue()));

        // Задача 8: Топ-5 крупнейших расходов
        System.out.println("\nЗадача 8: Пять крупнейших расходов (filter -> sorted -> limit(5)):");
        streamAnalytics.findTop5Expenses(transactions)
                .forEach(t -> System.out.printf("  * %s: %s руб. (%s)%n", t.getDate(), t.getAmount(), t.getDescription()));

        // 5. Запуск исследовательских экспериментов
        StreamExperiments.runLazinessExperiment(transactions);
        StreamExperiments.runBadStreamRefactoringExperiment(transactions);
        StreamExperiments.runParallelStreamExperiment(transactions);
    }
}
