package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

import java.math.BigDecimal;

/**
 * IslandBankDepositEvent is called when money is deposited to the bank.
 */
public class IslandBankWithdrawEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final BigDecimal amount;
    private String failureReason = null;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player who withdrawn the money.
     * @param island         The island that the money was withdrawn from.
     * @param amount         The amount that was withdrawn.
     */
    public IslandBankWithdrawEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.amount = amount;
    }

    /**
     * Get the player who withdrawn the money.
     * When null, then the console deposited the money using the admin command.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the amount that was deposited.
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Get the failure reason of the transaction.
     */
    @Nullable
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Set a failure reason for the transaction.
     *
     * @param failureReason The new failure reason to set.
     */
    public void setFailureReason(@Nullable String failureReason) {
        this.failureReason = failureReason != null && failureReason.isEmpty() ? null : failureReason;
    }

    @Override
    public boolean isCancelled() {
        return this.failureReason != null;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        setFailureReason(cancelled ? "Generic event cancel." : null);
    }


}
