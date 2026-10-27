package edu.course.lab05.service;

import edu.course.lab05.model.Category;
import edu.course.lab05.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Сервис аналитики финансовых транзакций на базе Collections Framework.
 * Все задачи реализованы традиционными циклами БЕЗ Stream API,
 * наглядно демонстрируя осознанный выбор коллекций под семантику каждой задачи.
 */
public class TransactionAnalytics {

    /**
     * Задача 1: Поиск транзакции по UUID.
     * Структура: Map<UUID, Transaction> - обеспечивает константную сложность O(1)
     * вместо полного перебора списка O(N).
     */
    public Optional<Transaction> findByIdInMap(Map<UUID, Transaction> index, UUID id) {
        if (index == null || id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(index.get(id));
    }

    /**
     * Задача 2: Получение всех уникальных категорий.
     * Структура: HashSet<Category> - устраняет дубликаты за O(1) на вставку
     * благодаря корректным equals() и hashCode().
     */
    public Set<Category> getUniqueCategories(Collection<Transaction> transactions) {
        Set<Category> uniqueCategories = new HashSet<>();
        if (transactions != null) {
            for (Transaction tx : transactions) {
                uniqueCategories.add(tx.getCategory());
            }
        }
        return uniqueCategories;
    }

    /**
     * Задача 3: Группировка сумм по дате БЕЗ Stream API в хронологическом порядке.
     * Структура: TreeMap<LocalDate, BigDecimal> - автоматически поддерживает ключи (даты)
     * в отсортированном порядке на основе красно-черного дерева (O(log N)).
     */
    public Map<LocalDate, BigDecimal> groupTotalAmountByDate(Collection<Transaction> transactions) {
        Map<LocalDate, BigDecimal> dateSumMap = new TreeMap<>();
        if (transactions == null) {
            return dateSumMap;
        }

        for (Transaction tx : transactions) {
            LocalDate date = tx.getDate();
            BigDecimal amount = tx.getAmount();

            BigDecimal currentTotal = dateSumMap.get(date);
            if (currentTotal == null) {
                dateSumMap.put(date, amount);
            } else {
                dateSumMap.put(date, currentTotal.add(amount));
            }
        }
        return dateSumMap;
    }

    /**
     * Задача 4: Определение повторяющихся категорий.
     * Структура: Map<Category, Integer> (частотный словарь) и Set<Category>.
     */
    public Set<Category> findDuplicateCategories(Collection<Transaction> transactions) {
        Set<Category> seen = new HashSet<>();
        Set<Category> duplicates = new HashSet<>();

        if (transactions != null) {
            for (Transaction tx : transactions) {
                Category cat = tx.getCategory();
                // Set.add возвращает false, если элемент уже присутствовал
                if (!seen.add(cat)) {
                    duplicates.add(cat);
                }
            }
        }
        return duplicates;
    }

    /**
     * Задача 5: Сортировка по альтернативным критериям через Comparator.
     */
    public static class Comparators {
        // 1. По дате
        public static final Comparator<Transaction> BY_DATE =
                Comparator.comparing(Transaction::getDate);

        // 2. По сумме (по убыванию)
        public static final Comparator<Transaction> BY_AMOUNT_DESC =
                (t1, t2) -> t2.getAmount().compareTo(t1.getAmount());

        // 3. Многоуровневый: по категории, затем по дате
        public static final Comparator<Transaction> BY_CATEGORY_THEN_DATE =
                Comparator.comparing((Transaction t) -> t.getCategory().getName(), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Transaction::getDate);
    }

    /**
     * Задача 6: Демонстрация принципа PECS (Producer Extends, Consumer Super).
     * src является источником (Producer) данных -> ? extends T
     * dest является приемником (Consumer) данных -> ? super T
     */
    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        if (src == null || dest == null) {
            return;
        }
        for (T item : src) {
            dest.add(item); // Читаем из Producer, пишем в Consumer
        }
    }
}
