package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.World;
import org.bukkit.event.Cancellable;

/**
 * IslandUnlockWorldEvent is called when a world is unlocked to an island.
 */
public class IslandUnlockWorldEvent extends IslandEvent implements Cancellable {

    private final Dimension dimension;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island      The island that the world was unlocked for.
     * @param environment The environment of the world that is unlocked.
     */
    @Deprecated
    public IslandUnlockWorldEvent(Island island, World.Environment environment) {
        this(island, Dimension.getByName(environment.name()));
    }

    /**
     * The constructor of the event.
     *
     * @param island    The island that the world was unlocked for.
     * @param dimension The dimension of the world that is unlocked.
     */
    public IslandUnlockWorldEvent(Island island, Dimension dimension) {
        super(island);
        this.dimension = dimension;
    }

    /**
     * Get the environment of the world that is being unlocked.
     */
    @Deprecated
    public World.Environment getEnvironment() {
        return this.dimension.getEnvironment();
    }

    /**
     * Get the dimension of the world that is being unlocked.
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
