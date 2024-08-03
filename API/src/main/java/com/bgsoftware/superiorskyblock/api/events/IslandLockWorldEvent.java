package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;
import org.bukkit.event.Cancellable;

/**
 * IslandLockWorldEvent is called when a world is locked to an island.
 */
public class IslandLockWorldEvent extends IslandEvent implements Cancellable {

    private final Dimension dimension;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island      The island that the world was locked for.
     * @param environment The environment of the world that is locked.
     */
    @Deprecated
    public IslandLockWorldEvent(Island island, World.Environment environment) {
        this(island, Dimension.getByName(environment.name()));
    }

    /**
     * The constructor of the event.
     *
     * @param island    The island that the world was locked for.
     * @param dimension The dimension of the world that is locked.
     */
    public IslandLockWorldEvent(Island island, Dimension dimension) {
        super(island);
        this.dimension = dimension;
    }

    /**
     * Get the environment of the world that is being locked.
     */
    @Deprecated
    public World.Environment getEnvironment() {
        return this.dimension.getEnvironment();
    }

    /**
     * Get the environment of the world that is being locked.
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
