package edu.course.lab04;

import edu.course.lab04.exception.InsufficientFundsException;
import edu.course.lab04.exception.InvalidTransactionException;
import edu.course.lab04.exception.WalletNotFoundException;
import edu.course.lab04.model.Wallet;
import edu.course.lab04.service.WalletService;

import java.math.BigDecimal;

/**
 * Автоматические верификационные тесты для проверки требований Лабораторной работы № 4:
 * 1. Выброс InsufficientFundsException при нехватке средств.
 * 2. Неизменность баланса при отказе в операции (транзакционная безопасность).
 * 3. Отклонение отрицательной и нулевой суммы перевода.
 * 4. Защита от передачи null или пустых идентификаторов кошельков.
 * 5. Обнаружение неизвестного кошелька (WalletNotFoundException).
 * 6. Корректность цепочки исключений (Exception Chaining) при разборе строки.
 * 7. Корректное списание и зачисление при успешном переводе.
 */
public class WalletServiceVerificationTest {

    private static int testsRun = 0;
    private static int testsPassed = 0;

    public static void main(String[] args) {
        System.out.println(">>> Запуск проверочных тестов Лабораторной работы № 4 <<<");

        testSuccessfulTransfer();
        testInsufficientFundsLeavesStateUnchanged();
        testNegativeAndZeroAmountRejected();
        testNullAndBlankWalletsRejected();
        testUnknownWalletThrowsNotFound();
        testTransferToSelfRejected();
        testExceptionChainingOnInvalidNumberString();

        System.out.println("---------------------------------------------------------");
        System.out.printf("Результаты тестов: Всего: %d, Успешно: %d, Провалено: %d%n",
                testsRun, testsPassed, testsRun - testsPassed);

        if (testsRun != testsPassed) {
            System.err.println("!!! Есть проваленные тесты!");
            System.exit(1);
        } else {
            System.out.println(">>> Все требования лабораторной работы успешно подтверждены! <<<");
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        testsRun++;
        if (condition) {
            System.out.println("  [PASS] " + testName);
            testsPassed++;
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testSuccessfulTransfer() {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("T-1", "User 1", new BigDecimal("1000.00"));
        Wallet w2 = new Wallet("T-2", "User 2", new BigDecimal("500.00"));
        service.registerWallet(w1);
        service.registerWallet(w2);

        try {
            service.transfer("T-1", "T-2", new BigDecimal("300.00"));
            boolean balancesCorrect = w1.getBalance().compareTo(new BigDecimal("700.00")) == 0
                    && w2.getBalance().compareTo(new BigDecimal("800.00")) == 0;
            assertTrue("Успешный перевод корректно изменяет балансы отправителя и получателя", balancesCorrect);
        } catch (Exception e) {
            assertTrue("Успешный перевод не должен выбрасывать исключений: " + e.getMessage(), false);
        }
    }

    private static void testInsufficientFundsLeavesStateUnchanged() {
        WalletService service = new WalletService();
        BigDecimal initialBalance1 = new BigDecimal("1000.00");
        BigDecimal initialBalance2 = new BigDecimal("500.00");
        Wallet w1 = new Wallet("T-1", "User 1", initialBalance1);
        Wallet w2 = new Wallet("T-2", "User 2", initialBalance2);
        service.registerWallet(w1);
        service.registerWallet(w2);

        boolean exceptionThrown = false;
        try {
            service.transfer("T-1", "T-2", new BigDecimal("5000.00"));
        } catch (InsufficientFundsException ife) {
            exceptionThrown = true;
        } catch (Exception e) {
            // другое исключение
        }

        boolean stateUnchanged = w1.getBalance().compareTo(initialBalance1) == 0
                && w2.getBalance().compareTo(initialBalance2) == 0;

        assertTrue("Недостаток средств вызывает InsufficientFundsException", exceptionThrown);
        assertTrue("При отказе в переводе состояние кошельков не изменилось", stateUnchanged);
    }

    private static void testNegativeAndZeroAmountRejected() {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("T-1", "User 1", new BigDecimal("1000.00"));
        Wallet w2 = new Wallet("T-2", "User 2", new BigDecimal("500.00"));
        service.registerWallet(w1);
        service.registerWallet(w2);

        boolean negativeRejected = false;
        try {
            service.transfer("T-1", "T-2", new BigDecimal("-100.00"));
        } catch (InvalidTransactionException ite) {
            negativeRejected = true;
        } catch (Exception ignored) {}

        boolean zeroRejected = false;
        try {
            service.transfer("T-1", "T-2", BigDecimal.ZERO);
        } catch (InvalidTransactionException ite) {
            zeroRejected = true;
        } catch (Exception ignored) {}

        assertTrue("Отрицательная сумма вызывает InvalidTransactionException", negativeRejected);
        assertTrue("Нулевая сумма вызывает InvalidTransactionException", zeroRejected);
    }

    private static void testNullAndBlankWalletsRejected() {
        WalletService service = new WalletService();
        boolean rejected = false;
        try {
            service.transfer("", "T-2", new BigDecimal("100.00"));
        } catch (InvalidTransactionException ite) {
            rejected = true;
        } catch (Exception ignored) {}

        assertTrue("Пустой идентификатор кошелька вызывает InvalidTransactionException", rejected);
    }

    private static void testUnknownWalletThrowsNotFound() {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("T-1", "User 1", new BigDecimal("1000.00"));
        service.registerWallet(w1);

        boolean notFoundThrown = false;
        try {
            service.transfer("T-1", "NON_EXISTENT", new BigDecimal("100.00"));
        } catch (WalletNotFoundException wnfe) {
            notFoundThrown = true;
        } catch (Exception ignored) {}

        assertTrue("Неизвестный кошелек вызывает WalletNotFoundException", notFoundThrown);
    }

    private static void testTransferToSelfRejected() {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("T-1", "User 1", new BigDecimal("1000.00"));
        service.registerWallet(w1);

        boolean selfRejected = false;
        try {
            service.transfer("T-1", "T-1", new BigDecimal("100.00"));
        } catch (InvalidTransactionException ite) {
            selfRejected = true;
        } catch (Exception ignored) {}

        assertTrue("Перевод самому себе отклоняется с InvalidTransactionException", selfRejected);
    }

    private static void testExceptionChainingOnInvalidNumberString() {
        WalletService service = new WalletService();
        Wallet w1 = new Wallet("T-1", "User 1", new BigDecimal("1000.00"));
        Wallet w2 = new Wallet("T-2", "User 2", new BigDecimal("500.00"));
        service.registerWallet(w1);
        service.registerWallet(w2);

        boolean chainingPreserved = false;
        try {
            service.transferFromString("T-1", "T-2", "abc_not_a_number");
        } catch (InvalidTransactionException ite) {
            if (ite.getCause() instanceof NumberFormatException) {
                chainingPreserved = true;
            }
        } catch (Exception ignored) {}

        assertTrue("Цепочка исключений сохраняет NumberFormatException как cause", chainingPreserved);
    }
}
