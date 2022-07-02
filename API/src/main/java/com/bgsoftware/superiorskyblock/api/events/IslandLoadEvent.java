package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * IslandLoadEvent is called when an island is loaded from cache.
 * This may happen when a player joins, a plugin trying to access an island or using placeholders on an island.
 */
public class IslandLoadEvent<T extends IslandBase> extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final IslandBase originalIslandBase;
    private final T newIsland;
    private final IslandLoadLevel<T> loadLevel;

    /**
     * The constructor of the event.
     *
     * @param originalIslandBase The original {@link IslandBase} that was in memory.
     * @param newIsland          The new island object that was created.
     * @param loadLevel          The load level of the new island.
     */
    public IslandLoadEvent(IslandBase originalIslandBase, T newIsland, IslandLoadLevel<T> loadLevel) {
        super(!Bukkit.isPrimaryThread());
        this.originalIslandBase = originalIslandBase;
        this.newIsland = newIsland;
        this.loadLevel = loadLevel;
    }

    /**
     * Get the original {@link IslandBase} that was in memory.
     */
    public IslandBase getOriginalIslandBase() {
        return originalIslandBase;
    }

    /**
     * Get the new island object that was created.
     * The returned island is an instance of {@link IslandLoadLevel#getIslandType()} returned by {@link #getLoadLevel()}
     */
    public T getIsland() {
        return newIsland;
    }

    /**
     * Get the load level of the island.
     */
    public IslandLoadLevel<T> getLoadLevel() {
        return loadLevel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
