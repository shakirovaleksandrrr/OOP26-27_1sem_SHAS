package edu.course.lab04.ui;

import edu.course.lab04.exception.InsufficientFundsException;
import edu.course.lab04.exception.InvalidTransactionException;
import edu.course.lab04.exception.WalletNotFoundException;
import edu.course.lab04.service.WalletService;

import java.math.BigDecimal;
import java.util.logging.Logger;

/**
 * Пользовательский слой (UI/Controller).
 * Отвечает за:
 * 1. Получение ввода от пользователя;
 * 2. Перехват исключений предметной области (try/catch/finally);
 * 3. Формирование понятных и дружелюбных сообщений пользователю (соблюдение SRP);
 * 4. Устойчивость: программа не прекращает работу при ошибках ввода;
 * 5. Эксперимент с блоком finally (гарантированная фиксация аудита транзакции).
 */
public class WalletConsoleController {

    private static final Logger logger = Logger.getLogger(WalletConsoleController.class.getName());
    private final WalletService walletService;

    public WalletConsoleController(WalletService walletService) {
        this.walletService = walletService;
    }

    /**
     * Обработка запроса на перевод денежных средств.
     *
     * @param sourceId   кошелек отправителя
     * @param targetId   кошелек получателя
     * @param rawAmount  строка с суммой (как введено пользователем)
     */
    public void processTransfer(String sourceId, String targetId, String rawAmount) {
        System.out.printf("%n>>> Запрос на перевод: [%s -> %s], введенная сумма: '%s'%n", sourceId, targetId, rawAmount);
        boolean success = false;

        try {
            // Преобразование строки в число (может вызвать NumberFormatException)
            BigDecimal amount = new BigDecimal(rawAmount.trim().replace(',', '.'));

            // Вызов бизнес-сервиса (может выбросить InsufficientFundsException или InvalidTransactionException)
            walletService.transfer(sourceId, targetId, amount);

            System.out.printf("  [УСПЕХ] Перевод на сумму %s руб. успешно завершен!%n", amount);
            success = true;

        } catch (NumberFormatException nfe) {
            // Обработка неверного формата числа
            System.err.printf("  [ОШИБКА ВВОДА] Значение '%s' не является корректным числом! Пожалуйста, используйте формат: 1500.50%n", rawAmount);

        } catch (InsufficientFundsException ife) {
            // Обработка нехватки средств (Checked)
            System.err.printf("  [ОТКАЗ БАНКА] %s%n", ife.getMessage());
            System.err.printf("  [СОВЕТ] Пополните кошелек или укажите сумму меньше либо равную %s руб.%n", ife.getCurrentBalance());

        } catch (WalletNotFoundException wnfe) {
            // Обработка отсутствия кошелька
            System.err.printf("  [ОШИБКА РЕКВИЗИТОВ] %s. Проверьте правильность номера кошелька.%n", wnfe.getMessage());

        } catch (InvalidTransactionException ite) {
            // Обработка нарушения бизнес-правил (отрицательная сумма, перевод самому себе и т.д.)
            System.err.printf("  [НЕДОПУСТИМАЯ ОПЕРАЦИЯ] %s%n", ite.getMessage());
            if (ite.getCause() != null) {
                System.err.printf("  [ПРИЧИНА (CHAINING)] Исходный сбой: %s%n", ite.getCause().toString());
            }

        } finally {
            // Эксперимент с finally: гарантированное выполнение независимо от успеха или исключения
            System.out.printf("  [FINALLY-АУДИТ] Завершена обработка запроса [%s -> %s]. Результат операции: %s.%n",
                    sourceId, targetId, success ? "УСПЕШНО" : "ОШИБКА / ОТКЛОНЕНО");
        }
    }
}
