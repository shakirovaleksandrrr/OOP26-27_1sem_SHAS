package edu.course.lab04.exception;

import java.math.BigDecimal;

/**
 * Проверяемое (Checked) исключение: недостаточно средств на балансе кошелька.
 * Наследуется от java.lang.Exception.
 * Клиентский код обязан либо обработать его в блоке try-catch, либо объявить в throws.
 */
public class InsufficientFundsException extends Exception {

    private final String walletId;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientFundsException(String walletId, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format("Недостаточно средств на кошельке [%s]: текущий баланс %s руб., запрошено к списанию %s руб.",
                walletId, currentBalance, requestedAmount));
        this.walletId = walletId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public String getWalletId() {
        return walletId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
