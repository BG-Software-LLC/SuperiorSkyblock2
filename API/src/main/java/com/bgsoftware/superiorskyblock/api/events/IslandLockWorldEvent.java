package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.World;
import org.bukkit.event.Cancellable;

/**
 * IslandLockWorldEvent is called when a world is locked to an island.
 */
public class IslandLockWorldEvent extends IslandEvent implements Cancellable {

    private final World.Environment environment;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island      The island that the world was locked for.
     * @param environment The environment of the world that is locked.
     */
    public IslandLockWorldEvent(Island island, World.Environment environment) {
        super(island);
        this.environment = environment;
    }

    /**
     * Get the environment of the world that is being locked.
     */
    public World.Environment getEnvironment() {
        return environment;
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
