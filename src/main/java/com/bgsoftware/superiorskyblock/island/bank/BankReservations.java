package com.bgsoftware.superiorskyblock.island.bank;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

final class BankReservations {

    private final AtomicReference<BigDecimal> balance;
    private BigDecimal withdrawals = BigDecimal.ZERO;
    private BigDecimal deposits = BigDecimal.ZERO;

    BankReservations(AtomicReference<BigDecimal> balance) {
        this.balance = balance;
    }

    synchronized BigDecimal reserveWithdrawal(BigDecimal requested) {
        BigDecimal amount = balance.get().subtract(withdrawals).max(BigDecimal.ZERO).min(requested);
        withdrawals = withdrawals.add(amount);
        return amount;
    }

    synchronized void releaseWithdrawal(BigDecimal amount) {
        withdrawals = withdrawals.subtract(amount);
    }

    synchronized boolean reserveDeposit(BigDecimal amount, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.valueOf(-1)) > 0 && balance.get().add(deposits).add(amount).compareTo(limit) > 0)
            return false;
        deposits = deposits.add(amount);
        return true;
    }

    synchronized void releaseDeposit(BigDecimal amount) {
        deposits = deposits.subtract(amount);
    }
}
