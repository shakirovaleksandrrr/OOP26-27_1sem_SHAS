package edu.course.lab05.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Финансовая транзакция.
 * Реализует Identifiable<UUID> для обобщенного репозитория
 * и Comparable<Transaction> для задания естественного порядка (по дате, затем id).
 */
public class Transaction implements Identifiable<UUID>, Comparable<Transaction> {

    private final UUID id;
    private final LocalDate date;
    private final BigDecimal amount;
    private final Category category;
    private final String description;

    public Transaction(UUID id, LocalDate date, BigDecimal amount, Category category, String description) {
        if (id == null) {
            throw new IllegalArgumentException("Идентификатор транзакции не может быть null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Дата транзакции не может быть null");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма транзакции должна быть строго положительной: " + amount);
        }
        if (category == null) {
            throw new IllegalArgumentException("Категория транзакции не может быть null");
        }
        this.id = id;
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.description = description == null ? "" : description;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Category getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Естественный порядок: хронологический по дате, при совпадении - по UUID.
     */
    @Override
    public int compareTo(Transaction other) {
        int dateCmp = this.date.compareTo(other.date);
        if (dateCmp != 0) {
            return dateCmp;
        }
        return this.id.compareTo(other.id);
    }

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
        return String.format("[%s | %s | %10s руб. | %-15s | %s]",
                id.toString().substring(0, 8), date, amount, category.getName(), description);
    }
}
