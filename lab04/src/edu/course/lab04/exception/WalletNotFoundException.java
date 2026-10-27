package edu.course.lab04.exception;

/**
 * Непроверяемое исключение: указанный кошелек не найден в системе.
 */
public class WalletNotFoundException extends RuntimeException {

    private final String walletId;

    public WalletNotFoundException(String walletId) {
        super(String.format("Кошелек с идентификатором [%s] не зарегистрирован в системе", walletId));
        this.walletId = walletId;
    }

    public String getWalletId() {
        return walletId;
    }
}
