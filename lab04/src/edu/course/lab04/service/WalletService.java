package edu.course.lab04.service;

import edu.course.lab04.exception.InsufficientFundsException;
import edu.course.lab04.exception.InvalidTransactionException;
import edu.course.lab04.exception.WalletNotFoundException;
import edu.course.lab04.model.Wallet;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Сервис управления кошельками и выполнения денежных переводов.
 * Демонстрирует:
 * - разделение Checked (InsufficientFundsException) и Unchecked (InvalidTransactionException) исключений;
 * - транзакционную безопасность (состояние не изменяется при ошибках);
 * - диагностическое логирование вместо e.printStackTrace() и пустых catch;
 * - цепочки исключений (Exception Chaining).
 */
public class WalletService {

    private static final Logger logger = Logger.getLogger(WalletService.class.getName());
    private final Map<String, Wallet> wallets = new ConcurrentHashMap<>();

    public void registerWallet(Wallet wallet) {
        if (wallet == null) {
            throw new IllegalArgumentException("Кошелек не может быть null");
        }
        wallets.put(wallet.getId(), wallet);
        logger.info(() -> String.format("Зарегистрирован кошелек: %s", wallet));
    }

    public Wallet getWallet(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidTransactionException("Идентификатор кошелька не может быть пустым");
        }
        Wallet wallet = wallets.get(id);
        if (wallet == null) {
            throw new WalletNotFoundException(id);
        }
        return wallet;
    }

    /**
     * Основной метод межбанковского перевода между кошельками.
     *
     * @param sourceId идентификатор кошелька отправителя
     * @param targetId идентификатор кошелька получателя
     * @param amount   сумма перевода
     * @throws InsufficientFundsException проверяемое исключение при нехватке средств
     * @throws InvalidTransactionException непроверяемое исключение при нарушении бизнес-правил
     * @throws WalletNotFoundException     непроверяемое исключение при отсутствии кошелька
     */
    public void transfer(String sourceId, String targetId, BigDecimal amount)
            throws InsufficientFundsException {

        logger.fine(() -> String.format("Попытка перевода: [%s -> %s], сумма: %s", sourceId, targetId, amount));

        // 1. Валидация входных параметров (Unchecked исключения)
        if (sourceId == null || sourceId.isBlank() || targetId == null || targetId.isBlank()) {
            logger.warning("Отклонена операция: один из идентификаторов кошельков пуст.");
            throw new InvalidTransactionException("Идентификаторы отправителя и получателя обязательны к заполнению");
        }

        if (sourceId.equals(targetId)) {
            logger.warning(() -> String.format("Отклонена операция: кошелек отправителя совпадает с получателем [%s]", sourceId));
            throw new InvalidTransactionException("Перевод средств на тот же самый кошелек запрещен: " + sourceId);
        }

        if (amount == null) {
            logger.warning("Отклонена операция: сумма перевода null");
            throw new InvalidTransactionException("Сумма перевода не может быть null");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warning(() -> String.format("Отклонена операция: некорректная сумма %s <= 0", amount));
            throw new InvalidTransactionException("Сумма перевода должна быть строго положительной (запрошено: " + amount + ")");
        }

        // 2. Получение кошельков
        Wallet source = getWallet(sourceId);
        Wallet target = getWallet(targetId);

        // 3. Проверка достаточности средств (Checked исключение)
        if (source.getBalance().compareTo(amount) < 0) {
            logger.warning(() -> String.format(
                    "Отказ в транзакции: на кошельке [%s] баланс %s меньше суммы перевода %s",
                    sourceId, source.getBalance(), amount));
            throw new InsufficientFundsException(sourceId, source.getBalance(), amount);
        }

        // 4. Выполнение перевода с гарантией атомарности
        try {
            source.withdraw(amount);
            target.deposit(amount);
            logger.info(() -> String.format("Успешный перевод: %s руб. [%s -> %s]. Новый баланс отправителя: %s",
                    amount, sourceId, targetId, source.getBalance()));
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Критический сбой при изменении балансов кошельков", ex);
            throw new InvalidTransactionException("Внутренний сбой при выполнении перевода", ex);
        }
    }

    /**
     * Перевод со строковым вводом суммы.
     * Демонстрирует цепочку исключений (Exception Chaining):
     * технический NumberFormatException оборачивается в осмысленный InvalidTransactionException.
     */
    public void transferFromString(String sourceId, String targetId, String amountStr)
            throws InsufficientFundsException {
        try {
            if (amountStr == null || amountStr.isBlank()) {
                throw new InvalidTransactionException("Строка с суммой не может быть пустой");
            }
            BigDecimal parsedAmount = new BigDecimal(amountStr.trim().replace(',', '.'));
            transfer(sourceId, targetId, parsedAmount);
        } catch (NumberFormatException nfe) {
            logger.log(Level.WARNING, () -> String.format("Не удалось распарсить сумму '%s': %s", amountStr, nfe.getMessage()));
            throw new InvalidTransactionException("Некорректный числовой формат суммы: '" + amountStr + "'", nfe);
        }
    }
}
