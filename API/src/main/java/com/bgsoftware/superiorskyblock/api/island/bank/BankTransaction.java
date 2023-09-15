package com.bgsoftware.superiorskyblock.api.island.bank;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankTransaction {

    /**
     * Get the player that made the transaction.
     * Can be null if the console has made the transaction.
     */
    @Nullable
    UUID getPlayer();

    /**
     * Get the transaction action.
     */
    BankAction getAction();

    /**
     * Get the position of the transaction
     */
    int getPosition();

    /**
     * Get the time the transaction was made.
     */
    long getTime();

    /**
     * Get formatted time of the time the transaction was made.
     */
    String getDate();

    /**
     * Get the reason for failure of the transaction.
     * If succeed, an empty string will be returned.
     * <p>
     * Some fail reasons, such as "Not enough money", will not be logged.
     */
    String getFailureReason();

    /**
     * Get the amount that was withdrawn or deposited.
     * If the transaction failed, -1 will be returned.
     */
    BigDecimal getAmount();

}
