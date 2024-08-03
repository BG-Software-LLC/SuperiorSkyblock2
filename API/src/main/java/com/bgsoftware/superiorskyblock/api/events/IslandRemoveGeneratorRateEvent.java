package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.World;
import org.bukkit.event.Cancellable;


/**
 * IslandRemoveGeneratorRateEvent is called when a generator-rate of an island is removed.
 */
public class IslandRemoveGeneratorRateEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Key block;
    private final Dimension dimension;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the generator-rate of an island.
     *                       If set to null, it means the rate was removed via the console.
     * @param island         The island that the generator-rate was removed for.
     * @param block          The block that the rate was removed for.
     * @param environment    The environment of the world that the rate was removed for.
     */
    @Deprecated
    public IslandRemoveGeneratorRateEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block,
                                          World.Environment environment) {
        this(superiorPlayer, island, block, Dimension.getByName(environment.name()));
    }

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the generator-rate of an island.
     *                       If set to null, it means the rate was removed via the console.
     * @param island         The island that the generator-rate was removed for.
     * @param block          The block that the rate was removed for.
     * @param dimension      The dimension of the world that the rate was removed for.
     */
    public IslandRemoveGeneratorRateEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Key block,
                                          Dimension dimension) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.block = block;
        this.dimension = dimension;
    }

    /**
     * Get the player that removed the generator-rate.
     * If null, it means the level was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the block that the generator-rate was removed for.
     */
    public Key getBlock() {
        return block;
    }

    /**
     * Get the environment of the world that the rate was removed for.
     */
    @Deprecated
    public World.Environment getEnvironment() {
        return this.dimension.getEnvironment();
    }

    /**
     * Get the environment of the world that the rate was removed for.
     */
    public Dimension getDimension() {
        return dimension;
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
