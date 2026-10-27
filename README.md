# Лабораторные работы по дисциплине «Современные технологии объектно-ориентированного программирования»

**Автор курса:** к.п.н., Румянцев Сергей Александрович  
**Стек:** Java 21 LTS, Java Collections Framework, Java NIO.2, Stream API, JVM Bytecode (javap).

---

## 📁 Структура единого репозитория

```
oop-course-labs/
├── lab03/                   # Лабораторная работа № 3: Наследование и полиморфизм
│   ├── src/...              # Иерархия Transaction, Commissionable, late binding
│   ├── test/...             # Автопроверки полиморфизма и контракта Object
│   ├── REPORT_LAB03.docx    # Отчет в формате Microsoft Word
│   ├── REPORT_LAB03.md      # Отчет в формате Markdown
│   ├── run.bat              # Запуск программы в 1 клик
│   └── run_tests.bat        # Запуск тестов в 1 клик
│
├── lab04/                   # Лабораторная работа № 4: Исключения и диагностика
│   ├── src/...              # InsufficientFundsException (Checked), InvalidTransactionException (Unchecked)
│   ├── test/...             # Автопроверки транзакционной безопасности и Chaining
│   ├── REPORT_LAB04.docx    # Отчет в формате Microsoft Word
│   ├── REPORT_LAB04.md      # Отчет в формате Markdown
│   ├── run.bat              # Запуск программы в 1 клик
│   └── run_tests.bat        # Запуск тестов в 1 клик
│
├── lab05/                   # Лабораторная работа № 5: Collections Framework и Generics
│   ├── src/...              # Generic Repository<T, ID>, Optional, PECS, бенчмарки
│   ├── test/...             # Автопроверки CRUD, Set uniqueness, TreeMap, Comparators
│   ├── REPORT_LAB05.docx    # Отчет в формате Microsoft Word
│   ├── REPORT_LAB05.md      # Отчет в формате Markdown
│   ├── run.bat              # Запуск программы в 1 клик
│   └── run_tests.bat        # Запуск тестов в 1 клик
│
├── lab06/                   # Лабораторная работа № 6: Файлы, потоки, NIO и кодировки
│   ├── src/...              # CSV TransactionFileRepository, Path/Files, try-with-resources
│   ├── test/...             # Автопроверки round-trip, UTF-8, автосоздания директорий
│   ├── REPORT_LAB06.docx    # Отчет в формате Microsoft Word
│   ├── REPORT_LAB06.md      # Отчет в формате Markdown
│   ├── run.bat              # Запуск программы в 1 клик
│   └── run_tests.bat        # Запуск тестов в 1 клик
│
├── lab07/                   # Лабораторная работа № 7: Функциональное программирование и Stream API
│   ├── src/...              # 8 аналитических конвейеров, лямбды, ленивость, parallelStream
│   ├── test/...             # Автопроверки всех 8 stream pipelines и чистоты редукции
│   ├── REPORT_LAB07.docx    # Отчет в формате Microsoft Word
│   ├── REPORT_LAB07.md      # Отчет в формате Markdown
│   ├── run.bat              # Запуск программы в 1 клик
│   └── run_tests.bat        # Запуск тестов в 1 клик
│
├── build_all.bat            # Скрипт сборки и тестирования ВСЕХ лабораторных работ подряд
├── run_all_tests.bat        # Пакетный прогон тестов по всем лабораторным
└── .gitignore               # Исключение артефактов компиляции и временных папок
```

---

## 📋 Сводная таблица лабораторных работ

| № Лабы | Тема | Ключевые концепции | Главный класс | Отчет Word |
| :---: | :--- | :--- | :--- | :---: |
| **III** | Наследование, интерфейсы и полиморфизм | Абстрактный класс `Transaction`, интерфейс `Commissionable`, `Late Binding` через `invokevirtual`, контракт `Object` (`equals/hashCode`), индивидуальное расширение `CashbackTransaction`. | `TransactionApp` | [REPORT_LAB03.docx](lab03/REPORT_LAB03.docx) |
| **IV** | Исключения, диагностика и устойчивое поведение | `Checked` (`InsufficientFundsException`) vs `Unchecked` (`InvalidTransactionException`), разделение ответственности (SRP), логирование `java.util.logging`, аудит в `finally`, цепочки исключений (Chaining). | `WalletApp` | [REPORT_LAB04.docx](lab04/REPORT_LAB04.docx) |
| **V** | Collections Framework и Generics | Обобщенный `Repository<T, ID>`, возврат `Optional<T>`, принцип `PECS` (`? extends / ? super`), бенчмарк `ArrayList` vs `LinkedList`, поиск на 100 000 элементах (`List` vs `Map`), `Type Erasure`. | `RepositoryApp` | [REPORT_LAB05.docx](lab05/REPORT_LAB05.docx) |
| **VI** | Ввод-вывод в Java: файлы, NIO и кодировки | `Path` и `Files` (NIO.2), сериализация/десериализация CSV, `try-with-resources` (`AutoCloseable`, `addSuppressed`), эксперименты «Байты или символы» (PNG) и «Кракозябры» (UTF-8 vs ISO-8859-1). | `FileApp` | [REPORT_LAB06.docx](lab06/REPORT_LAB06.docx) |
| **VII** | Функциональное программирование и Stream API | Функциональные интерфейсы (`Predicate`, `Function`, `Consumer`, `Supplier`), 8 конвейеров Stream API, эксперимент на ленивость вычислений, устранение мутабельного состояния, особенности `parallelStream`. | `StreamApp` | [REPORT_LAB07.docx](lab07/REPORT_LAB07.docx) |

---

## 🚀 Быстрый запуск

### Запуск конкретной лабораторной работы
Перейдите в папку нужной работы (например, `lab03` или `lab07`) и дважды кликните по:
- **`run.bat`** — запускает консольное демонстрационное приложение (с поддержкой кириллицы UTF-8 и паузой окна);
- **`run_tests.bat`** — запускает автоматические верификационные тесты.

### Пакетная сборка и проверка всего репозитория
В корневой папке дважды кликните по:
- **`build_all.bat`** — последовательно компилирует, тестирует и собирает исполняемые JAR-файлы для всех 5 лабораторных работ.
- **`run_all_tests.bat`** — последовательно прогоняет тестовые наборы по всем работам.
