package edu.course.lab03.service;

import edu.course.lab03.model.Commissionable;
import edu.course.lab03.model.Transaction;

import java.math.BigDecimal;

/**
 * Сервис учета банковских операций счета.
 * Демонстрирует Полиморфизм подтипов и позднее связывание (Late Binding).
 * Расчет баланса не использует instanceof, type-коды и switch!
 */
public class AccountService {

    /**
     * Полиморфный расчет общего изменения баланса счета.
     * Реализация открыта для расширения новыми типами транзакций без модификации данного метода.
     *
     * @param transactions массив транзакций
     * @return суммарное влияние всех транзакций на баланс
     */
    public BigDecimal calculateTotalBalanceImpact(Transaction[] transactions) {
        if (transactions == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            // Динамическая диспетчеризация (invokevirtual в байт-коде)
            total = total.add(transaction.balanceImpact());
        }
        return total;
    }

    /**
     * Подсчет суммарной комиссии по всем операциям, реализующим контракт Commissionable.
     * Показывает работу с интерфейсом как с отдельным контрактом типа.
     *
     * @param transactions массив транзакций
     * @return общая сумма уплаченных комиссий
     */
    public BigDecimal calculateTotalCommission(Transaction[] transactions) {
        if (transactions == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCommission = BigDecimal.ZERO;
        for (Transaction transaction : transactions) {
            if (transaction instanceof Commissionable commissionable) {
                totalCommission = totalCommission.add(commissionable.commission());
            }
        }
        return totalCommission;
    }
}
