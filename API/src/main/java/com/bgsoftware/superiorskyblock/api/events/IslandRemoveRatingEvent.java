package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRemoveRatingEvent is called when a rating of a player is removed from an island.
 */
public class IslandRemoveRatingEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final SuperiorPlayer ratingPlayer;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the rating of the other player.
     *                       If null, the rating was removed by console.
     * @param ratingPlayer   The player that its rating was removed.
     * @param island         The island that was rated.
     */
    public IslandRemoveRatingEvent(@Nullable SuperiorPlayer superiorPlayer, SuperiorPlayer ratingPlayer, Island island) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.ratingPlayer = ratingPlayer;
    }

    /**
     * Get the player that removed the rating of the other player.
     * If null, the rating was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the player that its rating was removed.
     */
    public SuperiorPlayer getRatingPlayer() {
        return ratingPlayer;
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
