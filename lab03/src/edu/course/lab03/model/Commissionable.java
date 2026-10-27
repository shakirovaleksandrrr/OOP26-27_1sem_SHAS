package edu.course.lab03.model;

import java.math.BigDecimal;

/**
 * Интерфейс-контракт для транзакций, облагаемых комиссией.
 * Выделен в отдельный тип, поскольку комиссия присуща не всем операциям.
 */
public interface Commissionable {

    /**
     * Возвращает размер комиссии, взимаемой за операцию.
     *
     * @return комиссия в денежном выражении
     */
    BigDecimal commission();
}
