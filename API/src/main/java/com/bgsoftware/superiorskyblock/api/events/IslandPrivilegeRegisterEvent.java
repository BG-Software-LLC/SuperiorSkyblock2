package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.google.common.base.Preconditions;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandPrivilegeRegisterEvent is called when an island privilege is registered.
 */
public class IslandPrivilegeRegisterEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final IslandPrivilege islandPrivilege;

    /**
     * The constructor of the event.
     *
     * @param islandPrivilege The island privilege that was registered.
     */
    public IslandPrivilegeRegisterEvent(IslandPrivilege islandPrivilege) {
        this.islandPrivilege = Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
    }

    /**
     * Get the island privilege that was registered.
     */
    public IslandPrivilege getIslandPrivilege() {
        return islandPrivilege;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
