package edu.course.lab04;

import edu.course.lab04.model.Wallet;
import edu.course.lab04.service.WalletService;
import edu.course.lab04.ui.WalletConsoleController;

import java.math.BigDecimal;

/**
 * Главный класс приложения.
 * Демонстрация устойчивого поведения программы (Resilience) при возникновении
 * различных категорий исключений и соблюдения принципа единственной ответственности (SRP).
 */
public class WalletApp {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println(" ЛАБОРАТОРНАЯ РАБОТА № 4: ОБРАБОТКА ИСКЛЮЧЕНИЙ И ДИАГНОСТИКА     ");
        System.out.println("==================================================================");

        // 1. Инициализация сервиса и кошельков
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("W-100", "Иван Иванов", new BigDecimal("10000.00"));
        Wallet w2 = new Wallet("W-200", "Анна Смирнова", new BigDecimal("5000.00"));

        service.registerWallet(w1);
        service.registerWallet(w2);

        WalletConsoleController controller = new WalletConsoleController(service);

        System.out.println("\n--- ИСХОДНОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ---");
        System.out.println("  " + w1);
        System.out.println("  " + w2);

        // 2. Серия сценариев (пользователь выполняет несколько операций подряд)
        System.out.println("\n--- ВЫПОЛНЕНИЕ СЕРИИ ОПЕРАЦИЙ (ДЕМОНСТРАЦИЯ УСТОЙЧИВОСТИ) ---");

        // Сценарий 1: Успешный перевод
        controller.processTransfer("W-100", "W-200", "2500.00");

        // Сценарий 2: Некорректный формат строки с числом (NumberFormatException)
        controller.processTransfer("W-100", "W-200", "три_тысячи_рублей");

        // Сценарий 3: Недостаточно средств (Checked: InsufficientFundsException)
        controller.processTransfer("W-100", "W-200", "50000.00");

        // Сценарий 4: Отрицательная сумма перевода (Unchecked: InvalidTransactionException)
        controller.processTransfer("W-100", "W-200", "-500.00");

        // Сценарий 5: Неизвестный кошелек (Unchecked: WalletNotFoundException)
        controller.processTransfer("W-100", "W-999_UNKNOWN", "1000.00");

        // Сценарий 6: Перевод самому себе (Unchecked: InvalidTransactionException)
        controller.processTransfer("W-100", "W-100", "100.00");

        // Сценарий 7: Повторный успешный перевод (доказывает, что ошибки выше не сломали приложение)
        controller.processTransfer("W-200", "W-100", "1500.00");

        // 3. Итоговое состояние
        System.out.println("\n--- ИТОГОВОЕ СОСТОЯНИЕ КОШЕЛЬКОВ ПОСЛЕ ВСЕХ ТРАНЗАКЦИЙ ---");
        System.out.println("  " + w1);
        System.out.println("  " + w2);

        System.out.println("\n==================================================================");
        System.out.println(" Программа успешно обработала все сбои и не завершилась аварийно!");
        System.out.println("==================================================================");
    }
}
