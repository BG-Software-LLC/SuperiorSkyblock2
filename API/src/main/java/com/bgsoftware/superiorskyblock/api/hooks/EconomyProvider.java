package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.math.BigDecimal;

public interface EconomyProvider {

    /**
     * Get the amount of money a specific user has in his bank.
     *
     * @param superiorPlayer The player to check.
     */
    BigDecimal getBalance(SuperiorPlayer superiorPlayer);

    /**
     * Deposit money into a player's bank.
     *
     * @param superiorPlayer The player to deposit money to.
     * @param amount         The amount to deposit.
     * @return A result object for the transaction.
     */
    EconomyResult depositMoney(SuperiorPlayer superiorPlayer, double amount);

    /**
     * Withdraw money from a player's bank.
     *
     * @param superiorPlayer The player to withdraw money from.
     * @param amount         The amount to withdraw.
     * @return A result object for the transaction.
     */
    EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, double amount);

    class EconomyResult {

        @Nullable
        private final String errorMessage;
        private final double transactionMoney;

        public EconomyResult(String errorMessage) {
            this(errorMessage, 0);
        }

        public EconomyResult(double transactionMoney) {
            this("", transactionMoney);
        }

        public EconomyResult(@Nullable String errorMessage, double transactionMoney) {
            this.errorMessage = errorMessage;
            this.transactionMoney = transactionMoney;
        }

        /**
         * Get the error that occurred.
         */
        @Nullable
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * Get the amount of money involved in the transaction.
         */
        public double getTransactionMoney() {
            return transactionMoney;
        }

        /**
         * Check if the transaction has failed.
         */
        public boolean hasFailed() {
            return errorMessage != null && !errorMessage.isEmpty();
        }

    }

}
