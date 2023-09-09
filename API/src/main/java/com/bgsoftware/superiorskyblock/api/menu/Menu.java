package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.handlers.MenusManager;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

/**
 * Represents a menu of the plugin.
 * You can get an instance of a menu by its identifier using {@link MenusManager#getMenu(String)}
 * The instances of the menus are singleton, and there will be only one instance of each menu per session.
 */
public interface Menu<V extends MenuView<V, A>, A extends ViewArgs> {

    /**
     * Get the identifier of the menu.
     */
    String getIdentifier();

    /**
     * Get the layout of the menu.
     */
    MenuLayout<V> getLayout();

    /**
     * Get the sound to play when opening the menu.
     */
    @Nullable
    GameSound getOpeningSound();

    /**
     * Get whether it is possible to open the previous opened menu after closing this one.
     */
    boolean isPreviousMoveAllowed();

    /**
     * Get whether this menu should be skipped when it only contains one item.
     * This is only useful for menus that have their buttons open other menus.
     */
    boolean isSkipOneItem();

    /**
     * Create a new menu view for a player.
     * If the player already has a view opened, make sure you call {@link MenuView#setPreviousMove(boolean)} on
     * the opened view and pass 'false' as an argument, otherwise unexpected behavior may occur.
     *
     * @param superiorPlayer The player to open the menu for.
     * @param args           The arguments for the linked {@link V} menu view.
     */
    CompletableFuture<V> createView(SuperiorPlayer superiorPlayer, A args);

    /**
     * Create a new menu view for a player.
     * If the player already has a view opened, make sure you call {@link MenuView#setPreviousMove(boolean)} on
     * the opened view and pass 'false' as an argument, otherwise unexpected behavior may occur.
     *
     * @param superiorPlayer The player to open the menu for.
     * @param args           The arguments for the linked {@link V} menu view.
     * @param previousMenu   The previous menu to use. In most cases, this will be null.
     */
    CompletableFuture<V> createView(SuperiorPlayer superiorPlayer, A args, @Nullable MenuView<?, ?> previousMenu);

    void refreshViews();

    void refreshViews(Predicate<V> viewFilter);

    void closeViews();

    void closeViews(Predicate<V> viewFilter);

    /**
     * Callback method for when a player clicks on a button in a view of this menu.
     * This method should do necessary checks, and finally call {@link MenuViewButton#onButtonClick(InventoryClickEvent)}
     * on the button that was clicked (can be retrieved by using {@link InventoryClickEvent#getRawSlot()} as a slot
     * passed to {@link MenuLayout#getButton(int)})
     *
     * @param clickEvent The event associated with the click.
     * @param menuView   The menu view that was clicked.
     */
    void onClick(InventoryClickEvent clickEvent, V menuView);

    /**
     * Callback method for when a player closes a view of this menu.
     * The method has no limits on what can be done inside it, and it depends on your custom {@link V} implementation
     * of what to do inside it.
     *
     * @param closeEvent The associated event.
     * @param menuView   The menu view that was closed.
     */
    void onClose(InventoryCloseEvent closeEvent, V menuView);

}
