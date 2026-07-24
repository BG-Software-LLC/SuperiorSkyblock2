package com.bgsoftware.superiorskyblock.core.menu.view;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
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

    private Either<Inventory, DialogWrapper<V>> backedMenu;

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
            this.backedMenu.ifRight(dialog -> {
                plugin.getNMSDialogs().get().closeDialog(inventoryViewer, dialog);
            }).ifLeft(inventory -> {
                player.closeInventory();
            });
        });
    }

    @Override
    public Inventory getInventory() {
        Preconditions.checkState(this.backedMenu.isLeft(), "MenuView#getInventory can only be called on inventory-based menu views");
        return this.backedMenu.getLeft();
    }

    public void setInventory(Inventory inventory) {
        setBackedMenu(Either.left(inventory));
    }

    public DialogWrapper<V> getDialog() {
        Preconditions.checkState(this.backedMenu.isRight(), "MenuView#getDialog can only be called on dialog-based menu views");
        return this.backedMenu.getRight();
    }

    public void setDialog(DialogWrapper<V> dialog) {
        setBackedMenu(Either.right(dialog));
    }

    private void setBackedMenu(Either<Inventory, DialogWrapper<V>> backedMenu) {
        if (closed || !areBackedMenusSimilar(this.backedMenu, backedMenu)) {
            this.backedMenu = backedMenu;
            this.openView();
        }
    }

    private boolean areBackedMenusSimilar(@Nullable Either<Inventory, DialogWrapper<V>> o1,
                                          @Nullable Either<Inventory, DialogWrapper<V>> o2) {
        if (Objects.equals(o1, o2))
            return true;
        if (o1 == null || o2 == null || o1.isLeft() != o2.isLeft())
            return false;
        return o1.isLeft() ? o1.getLeft() == o2.getLeft() : o1.getRight() == o2.getRight();
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

        Object backedMenu = this.backedMenu == null ? null : this.backedMenu.isLeft() ? this.backedMenu.getLeft() :
                this.backedMenu.getRight();

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

        if (this.backedMenu.isLeft()) {
            if (Arrays.equals(player.getOpenInventory().getTopInventory().getContents(), ((Inventory) backedMenu).getContents()))
                return false;
        }

        if (previousMenuView != null)
            previousMenuView.setPreviousMove(false);

        if (currentOpenedView != null && previousMenuView != currentOpenedView)
            currentOpenedView.setPreviousMove(false);

        this.backedMenu.ifLeft(inventory -> {
            player.openInventory(inventory);
        }).ifRight(dialog -> {
            plugin.getNMSDialogs().ifPresent(nmsDialogs -> nmsDialogs.openDialog(inventoryViewer, dialog));
        });

        if (closed) {
            // If the view was closed before, we want to register it again.
            closed = false;
            menu.addView(this);
        }

        GameSoundImpl.playSound(player, menu.getOpeningSound());

        this.previousMenuView = previousMenuView != null ? previousMenuView : previousMove ? currentOpenedView : null;

        return true;
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
