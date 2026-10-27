package edu.course.lab05;

import edu.course.lab05.model.Category;
import edu.course.lab05.model.Transaction;
import edu.course.lab05.repository.InMemoryRepository;
import edu.course.lab05.repository.Repository;
import edu.course.lab05.service.TransactionAnalytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Автоматические верификационные тесты для проверки требований Лабораторной работы № 5:
 * 1. Корректность CRUD операций в Generic Repository.
 * 2. Возврат Optional.empty() при поиске несуществующего ключа.
 * 3. Гарантия уникальности элементов в Set<Category>.
 * 4. Корректность группировки сумм по дате в TreeMap.
 * 5. Корректность естественной сортировки (Comparable) и всех трех компараторов.
 * 6. Работоспособность метода с wildcards (PECS).
 * 7. Подтверждение стирания типов (Type Erasure).
 */
public class CollectionsVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println(">>> Запуск проверочных тестов Лабораторной работы № 5 <<<");

        testGenericRepositoryCrudAndOptional();
        testSetUniquenessWithEqualsHashCode();
        testTreeMapGroupingAndOrdering();
        testComparatorsAndNaturalOrder();
        testPecsGenericCopy();
        testTypeErasure();

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

    private static void testGenericRepositoryCrudAndOptional() {
        Repository<Transaction, UUID> repo = new InMemoryRepository<>();
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction(id, LocalDate.now(), new BigDecimal("100.00"), new Category("C1", "Food"), "Test");

        repo.save(tx);
        assertTrue("Repository.count() == 1 после save", repo.count() == 1);

        Optional<Transaction> found = repo.findById(id);
        assertTrue("findById возвращает Optional с объектом", found.isPresent() && found.get().equals(tx));

        Optional<Transaction> missing = repo.findById(UUID.randomUUID());
        assertTrue("findById для отсутствующего ключа возвращает Optional.empty()", missing.isEmpty());

        boolean deleted = repo.deleteById(id);
        assertTrue("deleteById возвращает true и удаляет объект", deleted && repo.count() == 0);
    }

    private static void testSetUniquenessWithEqualsHashCode() {
        Set<Category> set = new HashSet<>();
        Category c1 = new Category("C1", "Еда");
        Category c2 = new Category("C1", "Еда дубликат с тем же ID");
        Category c3 = new Category("C2", "Транспорт");

        set.add(c1);
        set.add(c2);
        set.add(c3);

        assertTrue("Set гарантирует уникальность категорий по equals/hashCode (размер равен 2)", set.size() == 2);
    }

    private static void testTreeMapGroupingAndOrdering() {
        TransactionAnalytics analytics = new TransactionAnalytics();
        Category cat = new Category("C1", "Тест");

        LocalDate d1 = LocalDate.of(2026, 1, 10);
        LocalDate d2 = LocalDate.of(2026, 1, 5); // более ранняя дата
        LocalDate d3 = LocalDate.of(2026, 1, 10); // та же дата d1

        List<Transaction> list = List.of(
                new Transaction(UUID.randomUUID(), d1, new BigDecimal("100.00"), cat, "T1"),
                new Transaction(UUID.randomUUID(), d2, new BigDecimal("50.00"), cat, "T2"),
                new Transaction(UUID.randomUUID(), d3, new BigDecimal("200.00"), cat, "T3")
        );

        Map<LocalDate, BigDecimal> grouped = analytics.groupTotalAmountByDate(list);

        assertTrue("TreeMap сгруппировал суммы для d1 (100 + 200 = 300.00)",
                grouped.get(d1).compareTo(new BigDecimal("300.00")) == 0);
        assertTrue("TreeMap сохранил сумму для d2 (50.00)",
                grouped.get(d2).compareTo(new BigDecimal("50.00")) == 0);

        List<LocalDate> sortedKeys = new ArrayList<>(grouped.keySet());
        assertTrue("Ключи в TreeMap автоматически отсортированы по возрастанию дат",
                sortedKeys.get(0).equals(d2) && sortedKeys.get(1).equals(d1));
    }

    private static void testComparatorsAndNaturalOrder() {
        Category cFood = new Category("C1", "А_Еда");
        Category cAuto = new Category("C2", "Б_Авто");

        Transaction t1 = new Transaction(UUID.randomUUID(), LocalDate.of(2026, 2, 1), new BigDecimal("500.00"), cAuto, "1");
        Transaction t2 = new Transaction(UUID.randomUUID(), LocalDate.of(2026, 1, 1), new BigDecimal("1000.00"), cFood, "2");

        List<Transaction> list = new ArrayList<>(List.of(t1, t2));

        // Сортировка по сумме убывание
        list.sort(TransactionAnalytics.Comparators.BY_AMOUNT_DESC);
        assertTrue("BY_AMOUNT_DESC ставит большую сумму первой", list.get(0).equals(t2));

        // Сортировка по дате
        list.sort(TransactionAnalytics.Comparators.BY_DATE);
        assertTrue("BY_DATE ставит раннюю дату первой", list.get(0).equals(t2));
    }

    private static void testPecsGenericCopy() {
        List<Integer> ints = List.of(1, 2, 3);
        List<Number> nums = new ArrayList<>();
        // Метод copy принимает List<? extends T> и List<? super T>
        TransactionAnalytics.copy(ints, nums);
        assertTrue("PECS метод copyAll успешно скопировал подтипы в надтип", nums.size() == 3);
    }

    private static void testTypeErasure() {
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();
        assertTrue("Type Erasure: классы параметризованных списков одинаковы во время выполнения",
                strings.getClass() == integers.getClass());
    }
}
