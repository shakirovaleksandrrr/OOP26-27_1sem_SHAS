package edu.course.lab04.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Модель банковского кошелька / счета.
 * Инкапсулирует состояние баланса и гарантирует транзакционную целостность.
 */
public class Wallet {

    private final String id;
    private final String ownerName;
    private BigDecimal balance;

    public Wallet(String id, String ownerName, BigDecimal initialBalance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Идентификатор кошелька не может быть пустым");
        }
        if (ownerName == null || ownerName.isBlank()) {
            throw new IllegalArgumentException("Имя владельца кошелька не может быть пустым");
        }
        if (initialBalance == null || initialBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Начальный баланс не может быть отрицательным");
        }
        this.id = id;
        this.ownerName = ownerName;
        this.balance = initialBalance;
    }

    public String getId() {
        return id;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    /**
     * Пополнение баланса.
     */
    public synchronized void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма пополнения должна быть строго положительной: " + amount);
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Списание средств (предварительно проверенное сервисом).
     */
    public synchronized void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Сумма списания должна быть строго положительной: " + amount);
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Внутренняя несогласованность: баланс меньше запрашиваемой суммы");
        }
        this.balance = this.balance.subtract(amount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Objects.equals(id, wallet.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Кошелек [%s | %s | Баланс: %s руб.]", id, ownerName, balance);
    }
}
