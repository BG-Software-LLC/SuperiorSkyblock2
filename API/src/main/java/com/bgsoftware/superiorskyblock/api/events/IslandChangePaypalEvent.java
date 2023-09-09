package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangePaypalEvent is called when the paypal of the island is changed.
 */
public class IslandChangePaypalEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;

    private String paypal;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the paypal of the island.
     * @param island         The island that the paypal was changed for.
     * @param paypal         The new paypal of the island
     */
    public IslandChangePaypalEvent(SuperiorPlayer superiorPlayer, Island island, String paypal) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.paypal = paypal;
    }

    /**
     * Get the player that changed the paypal of the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new paypal of the island.
     */
    public String getPaypal() {
        return paypal;
    }

    /**
     * Set the new paypal for the island.
     *
     * @param paypal The new paypal to set.
     */
    public void setPaypal(String paypal) {
        Preconditions.checkNotNull(paypal, "Cannot set the discord of the island to null.");
        this.paypal = paypal;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
