package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


/**
 * PlayerCloseMenuEvent is called when a player closes a menu.
 */
public class PlayerCloseMenuEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final MenuView<?, ?> openedMenuView;
    @Nullable
    private MenuView<?, ?> newMenuView;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that closed the menu.
     * @param superiorMenu   The menu that was closed.
     * @param newMenu        The new menu that will be opened.
     *                       If null, no menu will be opened.
     * @deprecated See {@link #PlayerCloseMenuEvent(SuperiorPlayer, MenuView, MenuView)}
     */
    @Deprecated
    public PlayerCloseMenuEvent(SuperiorPlayer superiorPlayer, ISuperiorMenu superiorMenu, @Nullable ISuperiorMenu newMenu) {
        this(superiorPlayer, (MenuView<?, ?>) superiorMenu, newMenu);
    }

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that closed the menu.
     * @param openedMenuView The menu view that is opened for the player.
     * @param newMenuView    The new menu view that will be opened.
     *                       If null, no menu will be opened.
     */
    public PlayerCloseMenuEvent(SuperiorPlayer superiorPlayer, MenuView<?, ?> openedMenuView, @Nullable MenuView<?, ?> newMenuView) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.openedMenuView = openedMenuView;
        this.newMenuView = newMenuView;
    }

    /**
     * Get the player that opened the menu.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the menu that was opened by the player.
     *
     * @deprecated See {@link #getOpenedMenuView()}
     */
    @Deprecated
    public ISuperiorMenu getSuperiorMenu() {
        return ISuperiorMenu.convertFromView(this.getOpenedMenuView());
    }

    /**
     * Get the menu view that is opened for the player.
     */
    public MenuView<?, ?> getOpenedMenuView() {
        return this.openedMenuView;
    }

    /**
     * Get the new menu that will be opened.
     * If null, no menu will be opened.
     *
     * @deprecated See {@link #getNewMenuView()}
     */
    @Nullable
    @Deprecated
    public ISuperiorMenu getNewMenu() {
        return ISuperiorMenu.convertFromView(this.getNewMenuView());
    }

    /**
     * Get the new menu view that will be opened.
     * If null, no menu will be opened.
     */
    @Nullable
    public MenuView<?, ?> getNewMenuView() {
        return this.newMenuView;
    }

    /**
     * Set the new menu that will be opened.
     *
     * @param newMenu The new menu that will be opened.
     *                If null, no menu will be opened.
     * @deprecated See {@link #setNewMenuView(MenuView)}
     */
    @Deprecated
    public void setNewMenu(@Nullable ISuperiorMenu newMenu) {
        this.setNewMenuView(newMenu);
    }

    /**
     * Set the new menu view that will be opened.
     *
     * @param newMenuView The new menu that will be opened.
     *                    If null, no menu will be opened.
     */
    public void setNewMenuView(@Nullable MenuView<?, ?> newMenuView) {
        this.newMenuView = newMenuView;
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
