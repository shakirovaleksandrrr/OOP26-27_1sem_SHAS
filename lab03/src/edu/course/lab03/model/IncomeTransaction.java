package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Доходная транзакция (пополнение счета, зарплата и т.д.).
 * Увеличивает баланс на величину суммы операции (+amount).
 */
public final class IncomeTransaction extends Transaction {

    public IncomeTransaction(UUID id, LocalDate date, BigDecimal amount) {
        super(id, date, amount);
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount();
    }

    @Override
    public String getTypeName() {
        return "Доход";
    }
}
