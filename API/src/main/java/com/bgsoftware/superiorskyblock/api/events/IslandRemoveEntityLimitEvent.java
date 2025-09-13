package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandRemoveEntityLimitEvent is called when an entity-limit of an island is removed.
 */
public class IslandRemoveEntityLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Key entity;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the entity limit of an island.
     *                       If set to null, it means the limit was removed via the console.
     * @param island         The island that the entity limit was removed for.
     * @param entity         The entity that the limit was removed for.
     */
    public IslandRemoveEntityLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key entity) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.entity = entity;
    }

    /**
     * Get the player that removed the entity limit.
     * If null, it means the limit was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the entity that the limit was removed for.
     */
    public Key getEntity() {
        return entity;
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
