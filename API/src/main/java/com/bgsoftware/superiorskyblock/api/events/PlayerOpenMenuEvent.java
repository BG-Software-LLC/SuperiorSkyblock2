package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
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
    private final MenuView<?, ?> menuView;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that opened the menu.
     * @param superiorMenu   The menu that was opened
     * @deprecated See {@link #PlayerOpenMenuEvent(SuperiorPlayer, MenuView)}
     */
    @Deprecated
    public PlayerOpenMenuEvent(SuperiorPlayer superiorPlayer, ISuperiorMenu superiorMenu) {
        this(superiorPlayer, (MenuView<?, ?>) superiorMenu);
    }

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that opened the menu.
     * @param menuView       The menu that was opened
     */
    public PlayerOpenMenuEvent(SuperiorPlayer superiorPlayer, MenuView<?, ?> menuView) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.menuView = menuView;
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
     * @deprecated See {@link #getMenuView()}
     */
    @Deprecated
    public ISuperiorMenu getSuperiorMenu() {
        return ISuperiorMenu.convertFromView(menuView);
    }

    /**
     * Get the menu view that was opened by the player.
     */
    public MenuView<?, ?> getMenuView() {
        return menuView;
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
