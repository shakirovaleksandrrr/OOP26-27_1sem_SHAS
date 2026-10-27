package edu.course.lab03.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Индивидуальное расширение: Транзакция начисления кэшбэка за покупки.
 * Демонстрирует соблюдение принципа открытости/закрытости (OCP):
 * новый подтип транзакции добавляется без модификации базового алгоритма подсчета баланса.
 * Влияние на баланс: положительное (+cashbackAmount).
 */
public final class CashbackTransaction extends Transaction {

    private final BigDecimal rate; // ставка кэшбэка (например, 0.05 для 5%)
    private final BigDecimal originalPurchaseAmount;

    public CashbackTransaction(UUID id, LocalDate date, BigDecimal originalPurchaseAmount, BigDecimal rate) {
        // Базовой суммой транзакции является рассчитанный кэшбэк
        super(id, date, calculateCashback(originalPurchaseAmount, rate));
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Ставка кэшбэка должна находиться в диапазоне (0, 1]: " + rate);
        }
        this.originalPurchaseAmount = originalPurchaseAmount;
        this.rate = rate;
    }

    private static BigDecimal calculateCashback(BigDecimal purchaseAmount, BigDecimal rate) {
        if (purchaseAmount == null || purchaseAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма покупки должна быть положительной");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Ставка кэшбэка должна быть положительной");
        }
        return purchaseAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getOriginalPurchaseAmount() {
        return originalPurchaseAmount;
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount(); // Кэшбэк пополняет баланс счета
    }

    @Override
    public String getTypeName() {
        return "Кэшбэк";
    }

    @Override
    public String toString() {
        return String.format("%s [Покупка: %s, Ставка: %.1f%%]",
                super.toString(), originalPurchaseAmount, rate.multiply(BigDecimal.valueOf(100)));
    }
}
