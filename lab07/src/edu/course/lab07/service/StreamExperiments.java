package edu.course.lab07.service;

import edu.course.lab07.model.Transaction;
import edu.course.lab07.model.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

/**
 * Модуль проведения исследовательских экспериментов по функциональному программированию:
 * 1. Эксперимент на ленивость (Laziness);
 * 2. Эксперимент «Плохой Stream» (устранение сайд-эффектов и изменяемого состояния);
 * 3. Эксперимент с parallelStream() (параллельная обработка и порядок выполнения).
 */
public class StreamExperiments {

    /**
     * Эксперимент 1: Ленивость вычислений (Stream Laziness).
     */
    public static void runLazinessExperiment(List<Transaction> transactions) {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 1: Ленивость вычислений (Stream Laziness)            ");
        System.out.println("==================================================================");

        System.out.println("Шаг 1: Создание конвейера с промежуточной операцией filter...");
        Stream<Transaction> lazyStream = transactions.stream()
                .filter(tx -> {
                    System.out.println("  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: " + tx.getId().toString().substring(0, 8));
                    return tx.getType() == TransactionType.EXPENSE;
                });

        System.out.println(">>> Конвейер объявлен, но терминальная операция еще НЕ вызвана.");
        System.out.println(">>> Обратите внимание: ни одной строки [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] в консоли нет!");

        System.out.println("\nШаг 2: Вызов терминальной операции .count()...");
        long count = lazyStream.count();
        System.out.println(">>> Терминальная операция завершена. Результат count: " + count);
        System.out.println("Вывод: Промежуточные операции Stream API строго ленивы и не выполняются до момента вызова терминальной операции!");
    }

    /**
     * Эксперимент 2: «Плохой Stream» (устранение мутабельного состояния).
     */
    public static void runBadStreamRefactoringExperiment(List<Transaction> transactions) {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 2: Устранение антипаттерна «Плохой Stream»           ");
        System.out.println("==================================================================");

        // 1. Антипаттерн: Изменяемое внешнее состояние (side-effect в forEach)
        BigDecimal[] mutableTotal = {BigDecimal.ZERO};
        transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .forEach(t -> mutableTotal[0] = mutableTotal[0].add(t.getAmount()));

        System.out.println("1. Результат с антипаттерном (массив-аккумулятор в forEach): " + mutableTotal[0]);
        System.out.println("   В чем опасность: Нарушает функциональную чистоту. При переходе на parallelStream()");
        System.out.println("   возникает состояние гонки (Race Condition) и потеря данных из-за неатомарной перезаписи total[0]!");

        // 2. Исправленный функциональный вариант: чистая редукция через reduce
        BigDecimal pureTotal = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        System.out.println("\n2. Исправленный функциональный вариант (чистый .reduce()):    " + pureTotal);
        System.out.println("   Преимущества: Полное отсутствие изменяемого состояния, потокобезопасность и готовность к распараллеливанию.");
    }

    /**
     * Эксперимент 3: parallelStream() vs stream().
     */
    public static void runParallelStreamExperiment(List<Transaction> transactions) {
        System.out.println("\n==================================================================");
        System.out.println(" ЭКСПЕРИМЕНТ 3: stream() vs parallelStream()                      ");
        System.out.println("==================================================================");

        System.out.println("1. Последовательный поток (stream):");
        transactions.stream()
                .limit(5)
                .forEach(t -> System.out.printf("   [Поток: %-25s] Обработка ID: %s%n",
                        Thread.currentThread().getName(), t.getId().toString().substring(0, 8)));

        System.out.println("\n2. Параллельный поток (parallelStream):");
        transactions.parallelStream()
                .limit(5)
                .forEach(t -> System.out.printf("   [Поток: %-25s] Обработка ID: %s%n",
                        Thread.currentThread().getName(), t.getId().toString().substring(0, 8)));

        System.out.println("\nВывод: parallelStream использует общий пул ForkJoinPool.commonPool().");
        System.out.println("Порядок forEach в параллельном потоке недетерминирован (элементы обрабатываются хаотично разными потоками).");
        System.out.println("Правило «parallel всегда быстрее» ошибочно: на небольших объемах накладные расходы на разделение задач");
        System.out.println("и синхронизацию потоков превышают выигрыш от многоядерности!");
    }
}
