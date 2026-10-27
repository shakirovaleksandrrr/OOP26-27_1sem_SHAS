package edu.course.lab04.exception;

/**
 * Непроверяемое (Unchecked) исключение: некорректные параметры транзакции или логическая ошибка.
 * Наследуется от java.lang.RuntimeException.
 * Отражает ошибки программирования или невалидные входные данные.
 * Поддерживает сохранение первопричины через механизм Chaining (cause).
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
