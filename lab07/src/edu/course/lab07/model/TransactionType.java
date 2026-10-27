package edu.course.lab07.model;

public enum TransactionType {
    INCOME("Доход"),
    EXPENSE("Расход");

    private final String title;

    TransactionType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
