package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandFlagRegisterEvent is called when an island flag is registered.
 */
public class IslandFlagRegisterEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final IslandFlag islandFlag;

    /**
     * The constructor of the event.
     *
     * @param islandFlag The island flag that was registered.
     */
    public IslandFlagRegisterEvent(IslandFlag islandFlag) {
        this.islandFlag = Preconditions.checkNotNull(islandFlag, "islandFlag parameter cannot be null.");
    }

    /**
     * Get the island flag that was registered.
     */
    public IslandFlag getIslandFlag() {
        return islandFlag;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
