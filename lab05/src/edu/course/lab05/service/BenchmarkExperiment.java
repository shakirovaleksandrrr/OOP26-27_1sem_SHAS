package edu.course.lab05.service;

import edu.course.lab05.model.Category;
import edu.course.lab05.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Исследовательский модуль для проведения экспериментов:
 * 1. ArrayList vs LinkedList (4 сценария);
 * 2. Эффективность поиска на 100 000 транзакциях: List vs Map;
 * 3. Исследование стирания типов (Type Erasure).
 */
public class BenchmarkExperiment {

    /**
     * Эксперимент 1: ArrayList vs LinkedList по 4 сценариям.
     */
    public static void runListComparison() {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 1: ArrayList vs LinkedList (50 000 элементов)        ");
        System.out.println("==================================================================");

        final int N = 50_000;
        final int RANDOM_LOOKUPS = 10_000;

        List<Integer> arrayList = new ArrayList<>();
        List<Integer> linkedList = new LinkedList<>();

        // Сценарий 1: Добавление в конец
        long t0 = System.nanoTime();
        for (int i = 0; i < N; i++) arrayList.add(i);
        long t1 = System.nanoTime();
        long alAdd = (t1 - t0) / 1_000_000;

        t0 = System.nanoTime();
        for (int i = 0; i < N; i++) linkedList.add(i);
        t1 = System.nanoTime();
        long llAdd = (t1 - t0) / 1_000_000;

        System.out.printf("1. Добавление в конец (%d эл.):%n", N);
        System.out.printf("   - ArrayList:  %4d мс  [O(1) амортизированное, непрерывный массив]%n", alAdd);
        System.out.printf("   - LinkedList: %4d мс  [O(1), но постоянное выделение памяти под Node]%n", llAdd);

        // Сценарий 2: Последовательный проход (Iterator / foreach)
        t0 = System.nanoTime();
        long alSum = 0;
        for (int val : arrayList) alSum += val;
        t1 = System.nanoTime();
        long alIter = (t1 - t0) / 1_000_000;

        t0 = System.nanoTime();
        long llSum = 0;
        for (int val : linkedList) llSum += val;
        t1 = System.nanoTime();
        long llIter = (t1 - t0) / 1_000_000;

        System.out.printf("2. Последовательный проход (for-each):%n");
        System.out.printf("   - ArrayList:  %4d мс  [Высокая локальность кэша CPU]%n", alIter);
        System.out.printf("   - LinkedList: %4d мс  [Промахи кэша из-за разрозненных узлов в памяти]%n", llIter);

        // Сценарий 3: get(i) по случайным индексам
        Random rnd = new Random(42);
        int[] indices = new int[RANDOM_LOOKUPS];
        for (int i = 0; i < RANDOM_LOOKUPS; i++) indices[i] = rnd.nextInt(N);

        t0 = System.nanoTime();
        long alGetSum = 0;
        for (int idx : indices) alGetSum += arrayList.get(idx);
        t1 = System.nanoTime();
        long alGet = (t1 - t0) / 1_000_000;

        t0 = System.nanoTime();
        long llGetSum = 0;
        for (int idx : indices) llGetSum += linkedList.get(idx);
        t1 = System.nanoTime();
        long llGet = (t1 - t0) / 1_000_000;

        System.out.printf("3. get(i) по случайным индексам (%d вызовов):%n", RANDOM_LOOKUPS);
        System.out.printf("   - ArrayList:  %4d мс  [O(1) прямой доступ по смещению]%n", alGet);
        System.out.printf("   - LinkedList: %4d мс  [O(N) последовательный обход от начала/конца!]%n", llGet);

        // Сценарий 4: Вставка в середину через ListIterator
        ListIterator<Integer> alIt = arrayList.listIterator(N / 2);
        t0 = System.nanoTime();
        for (int i = 0; i < 1_000; i++) alIt.add(-1);
        t1 = System.nanoTime();
        long alMid = (t1 - t0) / 1_000_000;

        ListIterator<Integer> llIt = linkedList.listIterator(N / 2);
        t0 = System.nanoTime();
        for (int i = 0; i < 1_000; i++) llIt.add(-1);
        t1 = System.nanoTime();
        long llMid = (t1 - t0) / 1_000_000;

        System.out.printf("4. Вставка в середину через ListIterator (1 000 вставок):%n");
        System.out.printf("   - ArrayList:  %4d мс  [O(N) постоянное копирование элементов System.arraycopy]%n", alMid);
        System.out.printf("   - LinkedList: %4d мс  [O(1) переброска ссылок узла Node]%n", llMid);
    }

    /**
     * Эксперимент 2: Эффективность поиска на 100 000 транзакциях: List vs Map.
     */
    public static void run100kSearchComparison() {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 2: Поиск в массиве 100 000 транзакций (List vs Map)  ");
        System.out.println("==================================================================");

        final int TOTAL_TX = 100_000;
        final int SEARCH_COUNT = 100;

        List<Transaction> list = new ArrayList<>(TOTAL_TX);
        Map<UUID, Transaction> map = new HashMap<>(TOTAL_TX * 2);

        Category cat = new Category("EXP", "Эксперимент");
        LocalDate date = LocalDate.now();
        BigDecimal amount = new BigDecimal("100.00");

        UUID[] sampleIds = new UUID[SEARCH_COUNT];
        Random rnd = new Random(12345);

        for (int i = 0; i < TOTAL_TX; i++) {
            UUID id = UUID.randomUUID();
            Transaction tx = new Transaction(id, date, amount, cat, "Тест " + i);
            list.add(tx);
            map.put(id, tx);

            if (i < SEARCH_COUNT) {
                sampleIds[i] = id;
            }
        }

        // Перемешаем sampleIds, чтобы искать разные транзакции
        Collections.shuffle(Arrays.asList(sampleIds), rnd);

        // Поиск в List (полный линейный перебор O(N))
        long t0 = System.nanoTime();
        int foundInList = 0;
        for (UUID targetId : sampleIds) {
            for (Transaction tx : list) {
                if (tx.getId().equals(targetId)) {
                    foundInList++;
                    break;
                }
            }
        }
        long t1 = System.nanoTime();
        long listTimeMs = (t1 - t0) / 1_000_000;

        // Поиск в Map (хеш-индексирование O(1))
        t0 = System.nanoTime();
        int foundInMap = 0;
        for (UUID targetId : sampleIds) {
            Transaction tx = map.get(targetId);
            if (tx != null) {
                foundInMap++;
            }
        }
        t1 = System.nanoTime();
        long mapTimeMicro = (t1 - t0) / 1_000;

        System.out.printf("Поиск %d случайных транзакций среди %d элементов:%n", SEARCH_COUNT, TOTAL_TX);
        System.out.printf("  * List<Transaction> (линейный поиск O(N)):     %6d мс  (найдено: %d)%n", listTimeMs, foundInList);
        System.out.printf("  * Map<UUID, Transaction> (хеш-индекс O(1)):    %6d мкс (найдено: %d)%n", mapTimeMicro, foundInMap);
        System.out.println("Вывод: Использование Map дает ускорение в тысячи раз для задач точечного поиска по ключу!");
    }

    /**
     * Эксперимент 3: Стирание типов (Type Erasure).
     */
    public static void runTypeErasureDemo() {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 3: Стирание типов (Type Erasure) в JVM               ");
        System.out.println("==================================================================");

        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        boolean sameClass = (strings.getClass() == integers.getClass());

        System.out.println("strings.getClass():  " + strings.getClass().getName());
        System.out.println("integers.getClass(): " + integers.getClass().getName());
        System.out.println("strings.getClass() == integers.getClass(): " + sameClass);
        System.out.println("Объяснение: В байт-коде параметры типов стираются до их границы (Object).");
        System.out.println("Поэтому JVM во время выполнения видит только 'сырой' класс java.util.ArrayList!");
    }
}
