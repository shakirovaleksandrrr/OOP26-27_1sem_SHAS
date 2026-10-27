package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Транзакция перевода денежных средств.
 * Реализует контракт Commissionable.
 * Влияние на баланс: списание суммы перевода и комиссии (-(amount + commission)).
 */
public final class TransferTransaction extends Transaction implements Commissionable {

    private final BigDecimal commissionFee;
    private final String recipientAccount;

    public TransferTransaction(UUID id, LocalDate date, BigDecimal amount, BigDecimal commissionFee, String recipientAccount) {
        super(id, date, amount);
        if (commissionFee == null || commissionFee.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Размер комиссии не может быть отрицательным или null");
        }
        if (recipientAccount == null || recipientAccount.isBlank()) {
            throw new IllegalArgumentException("Счет получателя не может быть пустым");
        }
        this.commissionFee = commissionFee;
        this.recipientAccount = recipientAccount;
    }

    @Override
    public BigDecimal commission() {
        return commissionFee;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    @Override
    public BigDecimal balanceImpact() {
        // Списание суммы перевода вместе с комиссией
        return getAmount().add(commissionFee).negate();
    }

    @Override
    public String getTypeName() {
        return "Перевод";
    }

    @Override
    public String toString() {
        return String.format("%s [Получатель: %s, Комиссия: %s]",
                super.toString(), recipientAccount, commissionFee);
    }
}
