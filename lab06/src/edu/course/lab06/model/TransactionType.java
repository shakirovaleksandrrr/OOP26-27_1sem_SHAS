package edu.course.lab06.model;

/**
 * Тип финансовой операции.
 */
public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход"),
    TRANSFER("Перевод");

    private final String title;

    TransactionType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
