package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.core.menu.hologram.HologramMenuWrapper;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuBlank;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.Objects;

public abstract class AbstractMenuView<V extends MenuView<V, A>, A extends ViewArgs> extends BaseMenuView<V, A> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    // Can be one of Inventory, DialogWrapper or HologramMenuWrapper
    @Nullable
    private Object backedMenu;

    private boolean closeButton = false;
    private boolean nextMove = false;
    private boolean closed = false;
    private boolean refreshing = false;

    protected Object[] cachedTitleArgs = null;

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

        updateTitleArgs();

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
            if (this.backedMenu instanceof DialogWrapper) {
                plugin.getNMSDialogs().get().closeDialog(inventoryViewer, (DialogWrapper<?>) this.backedMenu);
            } else if (this.backedMenu instanceof HologramMenuWrapper) {
                ((HologramMenuWrapper<?>) this.backedMenu).close(true);
            } else {
                player.closeInventory();
            }
        });
    }

    @Override
    public Inventory getInventory() {
        Preconditions.checkState(this.backedMenu instanceof Inventory, "MenuView#getInventory can only be called on inventory-based menu views");
        return (Inventory) this.backedMenu;
    }

    public void setInventory(Inventory inventory) {
        setBackedMenu(inventory);
    }

    @SuppressWarnings("unchecked")
    public DialogWrapper<V> getDialog() {
        Preconditions.checkState(this.backedMenu instanceof DialogWrapper, "MenuView#getDialog can only be called on dialog-based menu views");
        return (DialogWrapper<V>) this.backedMenu;
    }

    public void setDialog(DialogWrapper<V> dialog) {
        setBackedMenu(dialog);
    }

    @SuppressWarnings("unchecked")
    public HologramMenuWrapper<V> getHologramMenu() {
        Preconditions.checkState(this.backedMenu instanceof HologramMenuWrapper, "MenuView#getHologramMenu can only be called on hologram-based menu views");
        return (HologramMenuWrapper<V>) this.backedMenu;
    }

    public void setHologramMenu(HologramMenuWrapper<V> hologramMenu) {
        setBackedMenu(hologramMenu);
    }

    private void setBackedMenu(Object backedMenu) {
        if (closed || !Objects.equals(this.backedMenu, backedMenu)) {
            this.backedMenu = backedMenu;
            this.openView();
        }
    }


    public boolean isRefreshing() {
        return refreshing;
    }

    public void setClickedCloseButton() {
        closeButton = true;
    }

    @Nullable
    public Object[] getTitleArgs() {
        return this.cachedTitleArgs;
    }

    public void updateTitleArgs() {
        // Do nothing
    }

    private void openView() {
        boolean success = openViewInternal();
        if (!success) {
            AbstractMenu menu = (AbstractMenu) getMenu();
            menu.removeView(this);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean openViewInternal() {
        Player player = inventoryViewer.asPlayer();

        if (player == null)
            return false;

        if (player.isSleeping()) {
            Message.OPEN_MENU_WHILE_SLEEPING.send(inventoryViewer);
            return false;
        }

        AbstractMenu menu = (AbstractMenu) getMenu();

        if (!PluginEventsFactory.callPlayerOpenMenuEvent(inventoryViewer, this))
            return false;

        Log.debug(Debug.OPEN_MENU, inventoryViewer.getName());

        Object backedMenu = this.backedMenu;

        if (backedMenu == null || menu.getLayout() == null) {
            if (!(menu instanceof MenuBlank)) {
                Menus.MENU_BLANK.createView(inventoryViewer, EmptyViewArgs.INSTANCE, previousMenuView);
            }
            return false;
        }

        MenuView<?, ?> currentOpenedView = inventoryViewer.getOpenedView();
        if (currentOpenedView instanceof AbstractMenuView) {
            ((AbstractMenuView<?, ?>) currentOpenedView).nextMove = true;
        }

        boolean isHologramMenu = backedMenu instanceof HologramMenuWrapper;

        if (isHologramMenu && HologramMenuWrapper.getByPlayer(player.getUniqueId()) == null &&
                (player.isInsideVehicle() || !isStandingOnBlock(player))) {
            // While in a hologram menu, the player only sends his rotation to the server.
            // Doing that in mid-air is considered by the server as flying.
            Message.OPEN_HOLOGRAM_MENU_WHILE_NOT_ON_GROUND.send(inventoryViewer);
            return false;
        }

        if (backedMenu instanceof Inventory) {
            if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), ((Inventory) backedMenu).getContents()))
                return false;
        }

        if (previousMenuView != null)
            previousMenuView.setPreviousMove(false);

        if (currentOpenedView != null && previousMenuView != currentOpenedView)
            currentOpenedView.setPreviousMove(false);

        if (isHologramMenu) {
            // Hologram menus cannot be shown together with inventories.
            if (currentOpenedView != null)
                player.closeInventory();
            if (!((HologramMenuWrapper<?>) backedMenu).open(player))
                return false;
        } else {
            // The view of the player should not stay locked on a hologram menu when opening another type of menu.
            HologramMenuWrapper<?> openedHologramMenu = HologramMenuWrapper.getByPlayer(player.getUniqueId());
            if (openedHologramMenu != null)
                openedHologramMenu.close(false);

            if (backedMenu instanceof Inventory) {
                player.openInventory((Inventory) backedMenu);
            } else {
                plugin.getNMSDialogs().ifPresent(nmsDialogs ->
                        nmsDialogs.openDialog(inventoryViewer, (DialogWrapper<?>) backedMenu));
            }
        }

        if (closed) {
            // If the view was closed before, we want to register it again.
            closed = false;
            menu.addView(this);
        }

        GameSoundImpl.playSound(player, menu.getOpeningSound());

        this.previousMenuView = previousMenuView != null ? previousMenuView : previousMove ? currentOpenedView : null;

        return true;
    }

    private static boolean isStandingOnBlock(Player player) {
        // Similar to the check done by the server for detecting flying players.
        return player.getLocation().subtract(0, 0.0625, 0).getBlock().getType().isSolid();
    }

    public void onClose() {
        closed = true;

        if (!nextMove && !closeButton && plugin.getSettings().isOnlyBackButton()) {
            BukkitExecutor.sync(this::openView);
        } else if (this.previousMenuView != null && this.menu.isPreviousMoveAllowed()) {
            PluginEvent<PluginEventArgs.PlayerCloseMenu> event = PluginEventsFactory.callPlayerCloseMenuEvent(
                    this.inventoryViewer, this, previousMove ? this.previousMenuView : null);

            if (previousMove) {
                if (!event.isCancelled()) {
                    MenuView<?, ?> newMenu = event.getArgs().newMenuView;
                    if (newMenu != null)
                        BukkitExecutor.sync(newMenu::refreshView);

                }
            } else if (event.isCancelled()) {
                BukkitExecutor.sync(this::openView);
            } else {
                previousMove = true;
            }
        }

        closeButton = false;
        nextMove = false;
    }

}
