package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerOpenMenuEvent is called when a player opens a menu.
 */
public class PlayerOpenMenuEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final ISuperiorMenu superiorMenu;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that opened the menu.
     * @param superiorMenu   The menu that was opened
     */
    public PlayerOpenMenuEvent(SuperiorPlayer superiorPlayer, ISuperiorMenu superiorMenu) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.superiorMenu = superiorMenu;
    }

    /**
     * Get the player that opened the menu.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the menu that was opened by the player.
     */
    public ISuperiorMenu getSuperiorMenu() {
        return superiorMenu;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
