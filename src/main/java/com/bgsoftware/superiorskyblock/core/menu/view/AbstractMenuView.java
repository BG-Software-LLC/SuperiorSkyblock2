package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuBlank;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;

public abstract class AbstractMenuView<V extends MenuView<V, A>, A extends ViewArgs> extends BaseMenuView<V, A> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Inventory inventory;

    private boolean closeButton = false;
    private boolean nextMove = false;
    private boolean closed = false;
    private boolean refreshing = false;

    protected AbstractMenuView(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView, Menu<V, A> menu) {
        super(inventoryViewer, menu, previousMenuView);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void refreshView() {
        if (refreshing)
            return;

        refreshing = true;
        previousMove = false;

        ((AbstractMenu) menu).refreshView(this).whenComplete((view, error) -> {
            if (error != null) {
                ((Throwable) error).printStackTrace();
            } else {
                refreshing = false;
                previousMove = true;
            }
        });
    }

    @Override
    public void closeView() {
        inventoryViewer.runIfOnline(player -> {
            previousMove = false;
            player.closeInventory();
        });
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void setInventory(Inventory inventory) {
        if (closed || this.inventory != inventory) {
            this.inventory = inventory;
            this.openView();
        }
    }

    public boolean isRefreshing() {
        return refreshing;
    }

    public void setClickedCloseButton() {
        closeButton = true;
    }

    public String replaceTitle(String title) {
        return title;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void openView() {
        Player player = inventoryViewer.asPlayer();

        if (player == null)
            return;

        if (player.isSleeping()) {
            Message.OPEN_MENU_WHILE_SLEEPING.send(inventoryViewer);
            return;
        }

        AbstractMenu menu = (AbstractMenu) getMenu();

        if (!plugin.getEventsBus().callPlayerOpenMenuEvent(inventoryViewer, this))
            return;

        Log.debug(Debug.OPEN_MENU, inventoryViewer.getName());

        if (inventory == null || menu.getLayout() == null) {
            if (!(menu instanceof MenuBlank)) {
                Menus.MENU_BLANK.createView(inventoryViewer, EmptyViewArgs.INSTANCE, previousMenuView);
            }
            return;
        }

        MenuView<?, ?> currentOpenedView = inventoryViewer.getOpenedView();
        if (currentOpenedView instanceof AbstractMenuView) {
            ((AbstractMenuView<?, ?>) currentOpenedView).nextMove = true;
        }

        if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), inventory.getContents()))
            return;

        if (previousMenuView != null)
            previousMenuView.setPreviousMove(false);

        if (currentOpenedView != null && previousMenuView != currentOpenedView)
            currentOpenedView.setPreviousMove(false);

        player.openInventory(inventory);

        if (closed) {
            // If the view was closed before, we want to register it again.
            closed = false;
            menu.addView(this);
        }

        GameSoundImpl.playSound(player, menu.getOpeningSound());

        this.previousMenuView = previousMenuView != null ? previousMenuView : previousMove ? currentOpenedView : null;
    }

    public void onClose() {
        closed = true;

        if (!nextMove && !closeButton && plugin.getSettings().isOnlyBackButton()) {
            BukkitExecutor.sync(this::openView);
        } else if (this.previousMenuView != null && this.menu.isPreviousMoveAllowed()) {
            EventResult<MenuView<?, ?>> eventResult = plugin.getEventsBus().callPlayerCloseMenuEvent(this.inventoryViewer,
                    this, previousMove ? this.previousMenuView : null);

            if (previousMove) {
                if (!eventResult.isCancelled()) {
                    MenuView<?, ?> newMenu = eventResult.getResult();
                    if (newMenu != null)
                        BukkitExecutor.sync(newMenu::refreshView);

                }
            } else if (eventResult.isCancelled()) {
                BukkitExecutor.sync(this::openView);
            } else {
                previousMove = true;
            }
        }

        closeButton = false;
        nextMove = false;
    }

}
