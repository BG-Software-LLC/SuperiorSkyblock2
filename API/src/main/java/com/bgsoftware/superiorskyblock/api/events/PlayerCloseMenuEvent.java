package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import javax.annotation.Nullable;

/**
 * PlayerCloseMenuEvent is called when a player closes a menu.
 */
public class PlayerCloseMenuEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final ISuperiorMenu superiorMenu;
    @Nullable
    private ISuperiorMenu newMenu;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that closed the menu.
     * @param superiorMenu   The menu that was closed.
     * @param newMenu        The new menu that will be opened.
     *                       If null, no menu will be opened.
     */
    public PlayerCloseMenuEvent(SuperiorPlayer superiorPlayer, ISuperiorMenu superiorMenu, @Nullable ISuperiorMenu newMenu) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.superiorMenu = superiorMenu;
        this.newMenu = newMenu;
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

    /**
     * Get the new menu that will be opened.
     * If null, no menu will be opened.
     */
    @Nullable
    public ISuperiorMenu getNewMenu() {
        return newMenu;
    }

    /**
     * Set the new menu that will be opened.
     *
     * @param newMenu The new menu that will be opened.
     *                If null, no menu will be opened.
     */
    public void setNewMenu(@Nullable ISuperiorMenu newMenu) {
        this.newMenu = newMenu;
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
