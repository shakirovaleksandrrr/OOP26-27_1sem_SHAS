package edu.course.lab06.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Модель финансовой транзакции для сохранения в CSV файл.
 */
public class Transaction {

    private final UUID id;
    private final LocalDate date;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String category;
    private final String description;

    public Transaction(UUID id, LocalDate date, TransactionType type, BigDecimal amount, String category, String description) {
        if (id == null) throw new IllegalArgumentException("ID не может быть null");
        if (date == null) throw new IllegalArgumentException("Дата не может быть null");
        if (type == null) throw new IllegalArgumentException("Тип операции не может быть null");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма должна быть положительной: " + amount);
        }
        if (category == null || category.isBlank()) throw new IllegalArgumentException("Категория не может быть пустой");

        // Проверка учебного ограничения упрощенного CSV
        if (category.contains(";") || (description != null && description.contains(";"))) {
            throw new IllegalArgumentException("Упрощенный CSV парсер запрещает символ ';' внутри значений полей");
        }

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
    public String getCategory() { return category; }
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
        return String.format("[%s | %s | %-8s | %10s руб. | %-12s | %s]",
                id.toString().substring(0, 8), date, type.getTitle(), amount, category, description);
    }
}
