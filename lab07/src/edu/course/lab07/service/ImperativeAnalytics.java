package edu.course.lab07.service;

import edu.course.lab07.model.Category;
import edu.course.lab07.model.Transaction;
import edu.course.lab07.model.TransactionType;

import java.math.BigDecimal;
import java.util.*;

/**
 * Исходный императивный подход к аналитике финансовых операций.
 * Характеризуется явным управлением циклами (for), созданием промежуточных изменяемых коллекций
 * и ручным накоплением состояния (аккумуляторов).
 */
public class ImperativeAnalytics {

    public List<Transaction> findExpenses(List<Transaction> transactions) {
        List<Transaction> expenses = new ArrayList<>();
        if (transactions == null) return expenses;

        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE) {
                expenses.add(tx);
            }
        }
        return expenses;
    }

    public BigDecimal totalByCategory(List<Transaction> transactions, Category category) {
        if (transactions == null || category == null) return BigDecimal.ZERO;

        BigDecimal total = BigDecimal.ZERO;
        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE && category.equals(tx.getCategory())) {
                total = total.add(tx.getAmount());
            }
        }
        return total;
    }

    public Map<Category, BigDecimal> totals(List<Transaction> transactions) {
        Map<Category, BigDecimal> result = new HashMap<>();
        if (transactions == null) return result;

        for (Transaction tx : transactions) {
            if (tx.getType() == TransactionType.EXPENSE) {
                Category cat = tx.getCategory();
                BigDecimal current = result.get(cat);
                if (current == null) {
                    result.put(cat, tx.getAmount());
                } else {
                    result.put(cat, current.add(tx.getAmount()));
                }
            }
        }
        return result;
    }
}
