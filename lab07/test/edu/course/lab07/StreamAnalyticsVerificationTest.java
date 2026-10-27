package edu.course.lab07;

import edu.course.lab07.model.Category;
import edu.course.lab07.model.Transaction;
import edu.course.lab07.model.TransactionType;
import edu.course.lab07.service.StreamAnalytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Автоматические верификационные тесты для проверки требований Лабораторной работы № 7:
 * Проверка всех 8 задач Stream API и чистоты редукции.
 */
public class StreamAnalyticsVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println(">>> Запуск проверочных тестов Лабораторной работы № 7 <<<");

        Category cFood = new Category("C1", "Еда");
        Category cTech = new Category("C2", "Техника");

        List<Transaction> testData = List.of(
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 1), TransactionType.EXPENSE, new BigDecimal("100.00"), cFood, "Хлеб"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 2), TransactionType.EXPENSE, new BigDecimal("500.00"), cFood, "Ужин"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 3), TransactionType.INCOME,  new BigDecimal("1000.00"), cFood, "Зарплата"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 4), TransactionType.EXPENSE, new BigDecimal("400.00"), cTech, "Кабель"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 5), TransactionType.EXPENSE, new BigDecimal("200.00"), cFood, "Молоко"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 6), TransactionType.EXPENSE, new BigDecimal("50.00"),  cFood, "Жвачка"),
                new Transaction(UUID.randomUUID(), LocalDate.of(2026, 9, 7), TransactionType.EXPENSE, new BigDecimal("150.00"), cFood, "Чай")
        );

        StreamAnalytics analytics = new StreamAnalytics();

        // Тест 1: Расходы за период (1-3 сентября) -> 100 и 500 (доход 1000 исключен)
        List<Transaction> p1 = analytics.findExpensesForPeriod(testData, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3));
        assertTrue("findExpensesForPeriod исключает доходы и фильтрует даты", p1.size() == 2);

        // Тест 2: Больше 300 руб. -> 500, 1000, 400 (3 шт.)
        List<Transaction> gt = analytics.findTransactionsGreaterThan(testData, new BigDecimal("300.00"));
        assertTrue("findTransactionsGreaterThan находит операции > 300", gt.size() == 3);

        // Тест 3: Сортировка по убыванию суммы
        List<Transaction> sorted = analytics.sortByAmountDescending(testData);
        assertTrue("sortByAmountDescending ставит наибольшую сумму первой",
                sorted.get(0).getAmount().compareTo(new BigDecimal("1000.00")) == 0);

        // Тест 4: Извлечение описаний
        List<String> descs = analytics.extractDescriptions(testData);
        assertTrue("extractDescriptions извлекает все описания", descs.size() == 7 && descs.contains("Хлеб"));

        // Тест 5: Сумма расходов: 100 + 500 + 400 + 200 + 50 + 150 = 1400.00 (без 1000 дохода)
        BigDecimal totalExpenses = analytics.calculateTotalExpenses(testData);
        assertTrue("calculateTotalExpenses верно суммирует расходы (1400.00)",
                totalExpenses.compareTo(new BigDecimal("1400.00")) == 0);

        // Тест 6: Группировка по категориям
        Map<Category, BigDecimal> grouped = analytics.groupExpensesByCategory(testData);
        assertTrue("groupExpensesByCategory: Еда = 1000.00, Техника = 400.00",
                grouped.get(cFood).compareTo(new BigDecimal("1000.00")) == 0 &&
                grouped.get(cTech).compareTo(new BigDecimal("400.00")) == 0);

        // Тест 7: Категория с макс. расходами
        var maxCat = analytics.findCategoryWithMaxExpenses(testData);
        assertTrue("findCategoryWithMaxExpenses определяет 'Еда' с 1000.00 руб.",
                maxCat.isPresent() && maxCat.get().getKey().equals(cFood));

        // Тест 8: Топ-5 расходов
        List<Transaction> top5 = analytics.findTop5Expenses(testData);
        assertTrue("findTop5Expenses возвращает ровно 5 расходов по убыванию",
                top5.size() == 5 && top5.get(0).getAmount().compareTo(new BigDecimal("500.00")) == 0);

        System.out.println("---------------------------------------------------------");
        System.out.printf("Результаты тестов: Всего: %d, Успешно: %d, Провалено: %d%n",
                testsRun, testsPassed, testsRun - testsPassed);

        if (testsRun != testsPassed) {
            System.err.println("!!! Есть проваленные тесты!");
            System.exit(1);
        } else {
            System.out.println(">>> Все требования лабораторной работы успешно подтверждены! <<<");
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
}
