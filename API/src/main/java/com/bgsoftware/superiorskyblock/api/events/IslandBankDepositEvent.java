package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * IslandBankDepositEvent is called when money is deposited to the bank.
 */
public class IslandBankDepositEvent extends IslandEvent {

    private final SuperiorPlayer superiorPlayer;
    private final BigDecimal amount;

    /**
     * The constructor of the event.
     * @param superiorPlayer The player who deposited the money.
     * @param island The island that the money was deposited to.
     * @param amount The amount that was deposited.
     */
    public IslandBankDepositEvent(SuperiorPlayer superiorPlayer, Island island, BigDecimal amount){
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.amount = amount;
    }

    /**
     * Get the player who deposited the money.
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
}
