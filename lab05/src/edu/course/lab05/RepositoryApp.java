package edu.course.lab05;

import edu.course.lab05.model.Category;
import edu.course.lab05.model.Transaction;
import edu.course.lab05.repository.InMemoryRepository;
import edu.course.lab05.repository.Repository;
import edu.course.lab05.service.BenchmarkExperiment;
import edu.course.lab05.service.TransactionAnalytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Главный класс приложения.
 * Демонстрация Collections Framework, Generic Repository, Optional,
 * принципа PECS и экспериментального анализа структур данных.
 */
public class RepositoryApp {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 5: COLLECTIONS FRAMEWORK И GENERICS        ");
        System.out.println("==================================================================");

        // 1. Создание категорий
        Category catFood = new Category("CAT-01", "Продукты");
        Category catTransport = new Category("CAT-02", "Транспорт");
        Category catSalary = new Category("CAT-03", "Зарплата");

        // 2. Инициализация обобщенного репозитория
        Repository<Transaction, UUID> repository = new InMemoryRepository<>();

        UUID id1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID id2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID id3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID id4 = UUID.fromString("44444444-4444-4444-4444-444444444444");
        UUID id5 = UUID.fromString("55555555-5555-5555-5555-555555555555");

        LocalDate day1 = LocalDate.of(2026, 10, 25);
        LocalDate day2 = LocalDate.of(2026, 10, 26);
        LocalDate day3 = LocalDate.of(2026, 10, 27);

        List<Transaction> initialBatch = List.of(
                new Transaction(id1, day1, new BigDecimal("1200.50"), catFood, "Супермаркет"),
                new Transaction(id2, day1, new BigDecimal("350.00"), catTransport, "Метро и автобус"),
                new Transaction(id3, day2, new BigDecimal("65000.00"), catSalary, "Основная зарплата"),
                new Transaction(id4, day2, new BigDecimal("850.00"), catFood, "Кафе и обед"),
                new Transaction(id5, day3, new BigDecimal("4500.00"), catFood, "Продукты на неделю")
        );

        // Демонстрация PECS в saveAll(Iterable<? extends T>)
        repository.saveAll(initialBatch);

        System.out.printf("%n--- 1. Обобщенный репозиторий: сохранено %d транзакций ---%n", repository.count());
        for (Transaction tx : repository.findAll()) {
            System.out.println("  " + tx);
        }

        // 3. Поиск через Optional
        System.out.println("\n--- 2. Поиск по UUID через Optional<T> ---");
        Optional<Transaction> found = repository.findById(id3);
        System.out.println("Поиск существующего id3: " + found.map(Transaction::toString).orElse("Не найден"));

        UUID unknownId = UUID.randomUUID();
        Optional<Transaction> notFound = repository.findById(unknownId);
        System.out.println("Поиск отсутствующего id: " + (notFound.isEmpty() ? "Optional.empty() [Без null!]" : "Найдено"));

        // 4. Семантика коллекций: Аналитика без Stream API
        System.out.println("\n--- 3. Выбор коллекций по семантике (TransactionAnalytics) ---");
        TransactionAnalytics analytics = new TransactionAnalytics();
        List<Transaction> all = repository.findAll();

        // 4.1. Уникальные категории через Set
        Set<Category> uniqueCategories = analytics.getUniqueCategories(all);
        System.out.println("Уникальные категории (Set<Category>): " + uniqueCategories);

        // 4.2. Повторяющиеся категории
        Set<Category> duplicateCats = analytics.findDuplicateCategories(all);
        System.out.println("Повторяющиеся категории в операциях:  " + duplicateCats);

        // 4.3. Группировка по датам через TreeMap (в порядке дат без Stream API)
        Map<LocalDate, BigDecimal> dateSums = analytics.groupTotalAmountByDate(all);
        System.out.println("Группировка сумм по дате (TreeMap<LocalDate, BigDecimal>):");
        for (Map.Entry<LocalDate, BigDecimal> entry : dateSums.entrySet()) {
            System.out.printf("  * Дата: %s | Сумма расходов/доходов: %10s руб.%n", entry.getKey(), entry.getValue());
        }

        // 5. Демонстрация трех вариантов сортировки через Comparator
        System.out.println("\n--- 4. Сортировка операций через Comparator ---");

        List<Transaction> sortedByDate = new ArrayList<>(all);
        sortedByDate.sort(TransactionAnalytics.Comparators.BY_DATE);
        System.out.println("Сортировка по дате:");
        sortedByDate.forEach(t -> System.out.printf("  %s -> %s руб. (%s)%n", t.getDate(), t.getAmount(), t.getCategory().getName()));

        List<Transaction> sortedByAmount = new ArrayList<>(all);
        sortedByAmount.sort(TransactionAnalytics.Comparators.BY_AMOUNT_DESC);
        System.out.println("\nСортировка по сумме (убывание):");
        sortedByAmount.forEach(t -> System.out.printf("  %10s руб. -> %s (%s)%n", t.getAmount(), t.getDate(), t.getDescription()));

        List<Transaction> sortedByCatThenDate = new ArrayList<>(all);
        sortedByCatThenDate.sort(TransactionAnalytics.Comparators.BY_CATEGORY_THEN_DATE);
        System.out.println("\nСортировка по категории, затем по дате:");
        sortedByCatThenDate.forEach(t -> System.out.printf("  %-15s | %s | %10s руб.%n", t.getCategory().getName(), t.getDate(), t.getAmount()));

        // 6. Запуск исследовательских экспериментов
        BenchmarkExperiment.runListComparison();
        BenchmarkExperiment.run100kSearchComparison();
        BenchmarkExperiment.runTypeErasureDemo();
    }
}
