package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRateEvent is called when a player rates an island.
 */
public class IslandRateEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final SuperiorPlayer ratingPlayer;
    private final Rating rating;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the rating of the other player.
     *                       If null, the rating was changed by console.
     * @param ratingPlayer   The player that its rating was changed.
     * @param island         The island that was rated.
     * @param rating         The rating given to the island.
     */
    public IslandRateEvent(@Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer, Island island, Rating rating) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.ratingPlayer = ratingPlayer;
        this.rating = rating;
    }

    /**
     * Get the player that changed the rating of the other player.
     * If null, the rating was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that its rating was changed.
     */
    public SuperiorPlayer getRatingPlayer() {
        return ratingPlayer;
    }

    /**
     * Get the rating given to the island.
     */
    public Rating getRating() {
        return rating;
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
