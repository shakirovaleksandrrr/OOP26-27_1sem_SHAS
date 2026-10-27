package edu.course.lab03.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Абстрактный базовый класс транзакции.
 * Инкапсулирует общие неизменяемые атрибуты: идентификатор, дату и сумму операции.
 */
public abstract class Transaction {

    private final UUID id;
    private final LocalDate date;
    private final BigDecimal amount;

    protected Transaction(UUID id, LocalDate date, BigDecimal amount) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор транзакции не может быть null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата транзакции не может быть null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Сумма транзакции не может быть null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть строго положительной: " + amount);
        }
        this.id = id;
        this.date = date;
        this.amount = amount;
    }

    public final UUID getId() {
        return id;
    }

    public final LocalDate getDate() {
        return date;
    }

    public final BigDecimal getAmount() {
        return amount;
    }

    /**
     * Абстрактный метод определения влияния транзакции на баланс счета.
     * Каждый конкретный тип операции определяет свою логику расчета.
     *
     * @return величина изменения баланса (+ или -)
     */
    public abstract BigDecimal balanceImpact();

    /**
     * Человекочитаемое наименование типа транзакции.
     */
    public abstract String getTypeName();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String sign = balanceImpact().compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        return String.format("%s | %s | %-8s | Сумма: %s | Влияние: %s%s",
                id, date, getTypeName(), amount, sign, balanceImpact());
    }
}
