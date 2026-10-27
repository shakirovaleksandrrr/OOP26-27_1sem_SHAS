package edu.course.lab03.service;

import edu.course.lab03.model.Transaction;

/**
 * Сервис печати транзакций.
 * Демонстрирует концепцию Overloading (перегрузки методов):
 * компилятор на этапе компиляции (static/early binding) определяет нужную версию метода
 * на основе статического типа переданного аргумента.
 */
public class TransactionPrinter {

    /**
     * Перегруженная версия для одиночной транзакции.
     */
    public void print(Transaction transaction) {
        if (transaction == null) {
            System.out.println("null transaction");
            return;
        }
        System.out.println("  [Единичная транзакция] " + transaction);
    }

    /**
     * Перегруженная версия для массива транзакций.
     */
    public void print(Transaction[] transactions) {
        if (transactions == null || transactions.length == 0) {
            System.out.println("Список транзакций пуст.");
            return;
        }
        System.out.printf("  [Список из %d транзакций]:%n", transactions.length);
        for (Transaction tx : transactions) {
            System.out.println("    * " + tx);
        }
    }
}
