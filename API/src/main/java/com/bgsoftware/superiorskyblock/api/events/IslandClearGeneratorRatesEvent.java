package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.World;
import org.bukkit.event.Cancellable;


/**
 * IslandClearGeneratorRatesEvent is called when clearing generator-rates of an island.
 */
public class IslandClearGeneratorRatesEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final Dimension dimension;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that cleared the generator-rates of an island.
     *                       If set to null, it means the rates were cleared via the console.
     * @param island         The island that the generator-rates were cleared for.
     * @param environment    The environment of the world that the rates were cleared for.
     */
    @Deprecated
    public IslandClearGeneratorRatesEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, World.Environment environment) {
        this(superiorPlayer, island, Dimension.getByName(environment.name()));
    }

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that cleared the generator-rates of an island.
     *                       If set to null, it means the rates were cleared via the console.
     * @param island         The island that the generator-rates were cleared for.
     * @param dimension      The dimension of the world that the rates were cleared for.
     */
    public IslandClearGeneratorRatesEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, Dimension dimension) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.dimension = dimension;
    }

    /**
     * Get the player that cleared the generator-rates.
     * If null, it means the rates were cleared by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the environment of the world that the rates were cleared for.
     */
    @Deprecated
    public World.Environment getEnvironment() {
        return this.dimension.getEnvironment();
    }

    /**
     * Get the environment of the world that the rates were cleared for.
     */
    public Dimension getDimension() {
        return this.dimension;
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
