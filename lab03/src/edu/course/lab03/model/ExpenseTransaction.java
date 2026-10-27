package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Расходная транзакция (покупка, оплата услуг и т.д.).
 * Уменьшает баланс на величину суммы операции (-amount).
 */
public final class ExpenseTransaction extends Transaction {

    public ExpenseTransaction(UUID id, LocalDate date, BigDecimal amount) {
        super(id, date, amount);
    }

    @Override
    public BigDecimal balanceImpact() {
        return getAmount().negate();
    }

    @Override
    public String getTypeName() {
        return "Расход";
    }
}
