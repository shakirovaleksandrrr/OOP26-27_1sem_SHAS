package edu.course.lab07.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Финансовая транзакция для аналитической обработки через Stream API.
 */
public class Transaction {

    private final UUID id;
    private final LocalDate date;
    private final TransactionType type;
    private final BigDecimal amount;
    private final Category category;
    private final String description;

    public Transaction(UUID id, LocalDate date, TransactionType type, BigDecimal amount, Category category, String description) {
        if (id == null) throw new IllegalArgumentException("ID не может быть null");
        if (date == null) throw new IllegalArgumentException("Дата не может быть null");
        if (type == null) throw new IllegalArgumentException("Тип не может быть null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть > 0: " + amount);
        }
        if (category == null) throw new IllegalArgumentException("Категория не может быть null");

        this.id = id;
        this.date = date;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.description = description == null ? "" : description;
    }

    public UUID getId() { return id; }
    public LocalDate getDate() { return date; }
    public TransactionType getType() { return type; }
    public BigDecimal getAmount() { return amount; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }

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
        return String.format("[%s | %s | %-7s | %9s руб. | %-12s | %s]",
                id.toString().substring(0, 8), date, type.getTitle(), amount, category.getName(), description);
    }
}
