ОТЧЕТ ПО ЛАБОРАТОРНОЙ РАБОТЕ № 7
Дисциплина: Современные технологии объектно-ориентированного программирования
Тема: Функциональное программирование в Java: лямбда-выражения и Stream API

1. Концепция функционального программирования в Java
1.1. Лямбда-выражения и функциональные интерфейсы
Лямбда-выражение в Java — это компактный синтаксис для реализации функционального интерфейса (анонимная функция).
Функциональным называется интерфейс, содержащий ровно один абстрактный метод (Single Abstract Method, SAM). Такой интерфейс помечается аннотацией `@FunctionalInterface`.

Контрольный вопрос: Чем лямбда-выражение отличается от анонимного внутреннего класса?
1. **Генерация байт-кода**: Анонимный класс создает отдельный `.class`-файл на диске (например, `App$1.class`). Лямбда компилируется через опкод `invokedynamic` и связывается в рантайме через `LambdaMetafactory`, что снижает расход памяти Metaspace.
2. **Контекст `this`**: В анонимном классе `this` ссылается на экземпляр самого анонимного класса. В лямбда-выражении `this` указывает на окружающий экземпляр внешнего класса (lexical scoping).

1.2. Стандартные функциональные интерфейсы Java

| Интерфейс | Сигнатура метода | Назначение | Пример использования в Finance Tracker |
| :--- | :--- | :--- | :--- |
| `Predicate<T>` | `boolean test(T t)` | Проверка логического условия (фильтрация) | `tx -> tx.getType() == TransactionType.EXPENSE` |
| `Function<T, R>` | `R apply(T t)` | Отображение/трансформация типа $T \to R$ | `Transaction::getAmount` |
| `Consumer<T>` | `void accept(T t)` | Потребитель: выполнение действия над объектом | `tx -> System.out.println(tx)` |
| `Supplier<T>` | `T get()` | Поставщик: ленивое создание объекта | `ArrayList::new` |

1.3. Проверка версий инструментов JDK
```powershell
java -version
javac -version
javap -version
```

2. Архитектура конвейера Stream API
Конвейер Stream состоит из трех звеньев:
`[Источник данных (Source)]` $\to$ `[Промежуточные операции (Intermediate)]` $\to$ `[Терминальная операция (Terminal)]`
1. **Источник**: коллекция, массив или генератор (`transactions.stream()`).
2. **Промежуточные операции (Intermediate)**: `filter`, `map`, `sorted`, `limit`, `distinct`. Они возвращают новый `Stream` и являются строго **ленивыми (lazy)** — не производят фактической обработки до вызова терминальной операции.
3. **Терминальная операция (Terminal)**: `toList`, `reduce`, `collect`, `count`, `forEach`, `max`. Инициирует сквозной проход данных по конвейеру и завершает его работу.

3. Решение 8 аналитических задач Finance Tracker

| № | Задача | Конвейер Stream API (Источник $\to$ Промежуточные $\to$ Терминальная) |
| :- | :--- | :--- |
| 1 | **Расходы за период** | `transactions.stream()` $\to$ `.filter(IS_EXPENSE).filter(date in range)` $\to$ `.toList()` |
| 2 | **Операции > суммы** | `transactions.stream()` $\to$ `.filter(amount > threshold)` $\to$ `.toList()` |
| 3 | **Сортировка по сумме** | `transactions.stream()` $\to$ `.sorted(comparing(amount).reversed())` $\to$ `.toList()` |
| 4 | **Список описаний** | `transactions.stream()` $\to$ `.map(Transaction::getDescription).filter(nonBlank)` $\to$ `.toList()` |
| 5 | **Сумма всех расходов** | `transactions.stream()` $\to$ `.filter(IS_EXPENSE).map(Transaction::getAmount)` $\to$ `.reduce(ZERO, BigDecimal::add)` |
| 6 | **Группировка расходов** | `transactions.stream()` $\to$ `.filter(IS_EXPENSE)` $\to$ `.collect(groupingBy(Category, reducing(sum)))` |
| 7 | **Категория с макс. расходом**| `groupExpensesByCategory().entrySet().stream()` $\to$ `max(comparingByValue())` |
| 8 | **5 крупнейших расходов**| `transactions.stream()` $\to$ `.filter(IS_EXPENSE).sorted(amount desc).limit(5)` $\to$ `.toList()` |

4. Исследовательские эксперименты
4.1. Эксперимент 1: «Ленивость вычислений» (Stream Laziness)
```java
Stream<Transaction> stream = transactions.stream()
    .filter(t -> {
        System.out.println("filter: " + t.getId());
        return t.getType() == TransactionType.EXPENSE;
    });
// Ничего больше не делать
```
- До вызова терминальной операции на экран не вывелось ни одной строки — фильтрация не стартовала.
- При последующем вызове `stream.count()` элементы начали поступать в предикат по требованию.

4.2. Эксперимент 2: «Плохой Stream» (устранение мутабельного состояния)
- **Антипаттерн**:
```java
BigDecimal[] total = {BigDecimal.ZERO};
transactions.stream().forEach(t -> total[0] = total[0].add(t.getAmount()));
```
Опасность: Внешнее изменяемое состояние нарушает функциональную чистоту. При переходе на параллельные потоки `total[0]` страдает от состояния гонки (Race Condition).
- **Чистый вариант**:
```java
BigDecimal total = transactions.stream()
    .filter(t -> t.getType() == TransactionType.EXPENSE)
    .map(Transaction::getAmount)
    .reduce(BigDecimal.ZERO, BigDecimal::add);
```

4.3. Эксперимент 3: `parallelStream()` vs `stream()`
- `stream()` обрабатывает все элементы последовательно в потоке `main`.
- `parallelStream()` распределяет задачи по пулу рабочих потоков `ForkJoinPool.commonPool-worker-X`.
- Порядок вывода `forEach` становится недетерминированным.
- **Вывод**: Правило «parallel всегда быстрее» неверно. Накладные расходы на разделение задач в ForkJoinPool, синхронизацию и инвалидацию процессорных кэшей делают параллельные потоки на небольших объемах данных (до сотен тысяч элементов) медленнее последовательных.

5. Сравнение императивного и декларативного подходов
- **Императивный подход**: Описывает **КАК** выполнять задачу (циклы `for`, временные флаги, ручное добавление в `ArrayList/HashMap`). Высокая когнитивная нагрузка, риск ошибок индексации и мутации состояния.
- **Декларативный подход (Stream API)**: Описывает **ЧТО** требуется получить (отфильтровать $\to$ сгруппировать $\to$ сложить). Читаемость кода возрастает в разы, логика изолирована в чистых функциях без сайд-эффектов.

6. Программная реализация
6.1. Сервис StreamAnalytics.java
```java
package edu.course.lab07.service;

import edu.course.lab07.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

public class StreamAnalytics {

    public static final Predicate<Transaction> IS_EXPENSE = tx -> tx.getType() == TransactionType.EXPENSE;
    public static final Function<Transaction, BigDecimal> GET_AMOUNT = Transaction::getAmount;

    public List<Transaction> findExpensesForPeriod(List<Transaction> txs, LocalDate from, LocalDate to) {
        return txs.stream()
                .filter(IS_EXPENSE)
                .filter(tx -> !tx.getDate().isBefore(from) && !tx.getDate().isAfter(to))
                .toList();
    }

    public List<Transaction> findTransactionsGreaterThan(List<Transaction> txs, BigDecimal min) {
        return txs.stream()
                .filter(tx -> tx.getAmount().compareTo(min) > 0)
                .toList();
    }

    public List<Transaction> sortByAmountDescending(List<Transaction> txs) {
        return txs.stream()
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .toList();
    }

    public List<String> extractDescriptions(List<Transaction> txs) {
        return txs.stream()
                .map(Transaction::getDescription)
                .filter(desc -> !desc.isBlank())
                .toList();
    }

    public BigDecimal calculateTotalExpenses(List<Transaction> txs) {
        return txs.stream()
                .filter(IS_EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<Category, BigDecimal> groupExpensesByCategory(List<Transaction> txs) {
        return txs.stream()
                .filter(IS_EXPENSE)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));
    }

    public Optional<Map.Entry<Category, BigDecimal>> findCategoryWithMaxExpenses(List<Transaction> txs) {
        return groupExpensesByCategory(txs).entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    public List<Transaction> findTop5Expenses(List<Transaction> txs) {
        return txs.stream()
                .filter(IS_EXPENSE)
                .sorted(Comparator.comparing(Transaction::getAmount).reversed())
                .limit(5)
                .toList();
    }
}
```

6.2. Главный класс приложения StreamApp.java и консольный вывод
```text
==================================================================
 ЛАБОРАТОРНАЯ РАБОТА № 7: STREAM API И ФУНКЦИОНАЛЬНЫЕ ИНТЕРФЕЙСЫ  
==================================================================

--- ИСХОДНЫЙ СПИСОК ОПЕРАЦИЙ (8 ТРАНЗАКЦИЙ) ---
  [Consumer] [11111111 | 2026-09-01 | Расход  |   1200.00 руб. | Продукты     | Супермаркет у дома]
  [Consumer] [22222222 | 2026-09-02 | Расход  |    350.00 руб. | Транспорт    | Билет на поезд]
  [Consumer] [33333333 | 2026-09-05 | Доход   |  95000.00 руб. | Зарплата     | Зарплата за август]
  [Consumer] [44444444 | 2026-09-07 | Расход  |  45000.00 руб. | Жилье        | Аренда квартиры]
  [Consumer] [55555555 | 2026-09-10 | Расход  |   3200.50 руб. | Продукты     | Мясная лавка]
  [Consumer] [66666666 | 2026-09-12 | Расход  |  12500.00 руб. | Электроника  | Беспроводные наушники]
  [Consumer] [77777777 | 2026-09-15 | Расход  |    850.00 руб. | Транспорт    | Такси в аэропорт]
  [Consumer] [88888888 | 2026-09-20 | Расход  |    650.00 руб. | Продукты     | Кофейня]

--- 1. Сравнение подходов (Императивный vs Stream API) ---
Сумма по категории 'Продукты':
  * Императивно (цикл for + accumulator): 5050.50 руб.
  * Декларативно (filter + collect/reduce): 5050.50 руб.

--- 2. Выполнение 8 аналитических задач Stream API ---

Задача 1: Все расходы с 2026-09-01 по 2026-09-10:
  [11111111 | 2026-09-01 | Расход  |   1200.00 руб. | Продукты     | Супермаркет у дома]
  [22222222 | 2026-09-02 | Расход  |    350.00 руб. | Транспорт    | Билет на поезд]
  [44444444 | 2026-09-07 | Расход  |  45000.00 руб. | Жилье        | Аренда квартиры]
  [55555555 | 2026-09-10 | Расход  |   3200.50 руб. | Продукты     | Мясная лавка]

Задача 2: Операции на сумму более 10 000 руб.:
  [33333333 | 2026-09-05 | Доход   |  95000.00 руб. | Зарплата     | Зарплата за август]
  [44444444 | 2026-09-07 | Расход  |  45000.00 руб. | Жилье        | Аренда квартиры]
  [66666666 | 2026-09-12 | Расход  |  12500.00 руб. | Электроника  | Беспроводные наушники]

Задача 3: Топ операций по убыванию суммы:
  [33333333 | 2026-09-05 | Доход   |  95000.00 руб. | Зарплата     | Зарплата за август]
  [44444444 | 2026-09-07 | Расход  |  45000.00 руб. | Жилье        | Аренда квартиры]
  [66666666 | 2026-09-12 | Расход  |  12500.00 руб. | Электроника  | Беспроводные наушники]

Задача 4: Список описаний (map -> String):
  [Супермаркет у дома, Билет на поезд, Зарплата за август, Аренда квартиры, Мясная лавка, Беспроводные наушники, Такси в аэропорт, Кофейня]

Задача 5: Общая сумма расходов (reduce): 63750.50 руб.

Задача 6: Группировка расходов по категориям (groupingBy):
  * Жилье       :   45000.00 руб.
  * Транспорт   :    1200.00 руб.
  * Продукты    :    5050.50 руб.
  * Электроника :   12500.00 руб.

Задача 7: Категория с максимальными расходами:
  * Жилье (Сумма: 45000.00 руб.)

Задача 8: Пять крупнейших расходов (filter -> sorted -> limit(5)):
  * 2026-09-07: 45000.00 руб. (Аренда квартиры)
  * 2026-09-12: 12500.00 руб. (Беспроводные наушники)
  * 2026-09-10: 3200.50 руб. (Мясная лавка)
  * 2026-09-01: 1200.00 руб. (Супермаркет у дома)
  * 2026-09-15: 850.00 руб. (Такси в аэропорт)

==================================================================
 ЭКСПЕРИМЕНТ 1: Ленивость вычислений (Stream Laziness)            
==================================================================
Шаг 1: Создание конвейера с промежуточной операцией filter...
>>> Конвейер объявлен, но терминальная операция еще НЕ вызвана.
>>> Обратите внимание: ни одной строки [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] в консоли нет!

Шаг 2: Вызов терминальной операции .count()...
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 11111111
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 22222222
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 33333333
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 44444444
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 55555555
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 66666666
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 77777777
  [ВЫПОЛНЕНИЕ ЛЕНИВОГО FILTER] Проверка транзакции: 88888888
>>> Терминальная операция завершена. Результат count: 7
Вывод: Промежуточные операции Stream API строго ленивы и не выполняются до момента вызова терминальной операции!

==================================================================
 ЭКСПЕРИМЕНТ 2: Устранение антипаттерна «Плохой Stream»           
==================================================================
1. Результат с антипаттерном (массив-аккумулятор в forEach): 63750.50
2. Исправленный функциональный вариант (чистый .reduce()):    63750.50

==================================================================
 ЭКСПЕРИМЕНТ 3: stream() vs parallelStream()                      
==================================================================
1. Последовательный поток (stream):
   [Поток: main                     ] Обработка ID: 11111111
   [Поток: main                     ] Обработка ID: 22222222
   [Поток: main                     ] Обработка ID: 33333333
   [Поток: main                     ] Обработка ID: 44444444
   [Поток: main                     ] Обработка ID: 55555555

2. Параллельный поток (parallelStream):
   [Поток: main                     ] Обработка ID: 33333333
   [Поток: ForkJoinPool.commonPool-worker-1] Обработка ID: 55555555
   [Поток: ForkJoinPool.commonPool-worker-2] Обработка ID: 22222222
   [Поток: ForkJoinPool.commonPool-worker-1] Обработка ID: 44444444
   [Поток: main                     ] Обработка ID: 11111111
```

7. Исследование байт-кода: invokedynamic и LambdaMetafactory
Команды компиляции и сборки:
```powershell
javac -encoding UTF-8 -d out $(Get-ChildItem -Recurse -Filter *.java src).FullName
javap -classpath out -c -p edu.course.lab07.service.StreamAnalytics
jar --create --file lab07.jar --main-class edu.course.lab07.StreamApp -C out .
java -jar lab07.jar
```

Ключевые инструкции байт-кода:
`invokedynamic` — инструкция, вызывающая bootstrap-метод `java.lang.invoke.LambdaMetafactory.metafactory(...)`. При первом выполнении она генерирует в оперативной памяти динамический класс реализации функционального интерфейса.
Фрагмент байт-кода:
```text
  static {};
    Code:
       0: invokedynamic #183,  0  // InvokeDynamic #8:test:()Ljava/util/function/Predicate;
       5: putstatic     #17       // Field IS_EXPENSE:Ljava/util/function/Predicate;
```

8. Ответы на контрольные вопросы к защите
Что такое лямбда-выражение и функциональный интерфейс?
Лямбда-выражение — это анонимная функция для лаконичной реализации интерфейса. Функциональный интерфейс — интерфейс ровно с одним абстрактным методом (SAM), служащий целевым типом для лямбд.

Какие задачи решают Predicate, Function, Consumer, Supplier?
`Predicate` проверяет условие (возвращает `boolean`); `Function` преобразует объект одного типа в другой; `Consumer` выполняет побочное действие (печать, сохранение) без возврата значения; `Supplier` создает или поставляет объект.

Как устроен конвейер Stream: промежуточные и терминальные операции?
Промежуточные операции (`filter`, `map`, `sorted`) возвращают новый поток, настраивают логику трансформации и являются ленивыми. Терминальные операции (`collect`, `reduce`, `toList`, `count`) запускают фактическую обработку данных и возвращают результат.

Что означает «ленивость» вычислений в Stream API?
Промежуточные операции не выполняются в момент их вызова. Обработка данных происходит только тогда, когда вызвана терминальная операция. Это позволяет оптимизировать конвейер (например, прекратить обработку при `limit(5)`).

В чем особенности и риски parallelStream()?
`parallelStream` использует общий пул потоков `ForkJoinPool.commonPool()`. Риски: гонки данных (при использовании мутабельного состояния), накладные расходы на разделение задач и блокировки общих потоков, что на малых объемах делает его медленнее последовательного `stream()`.
