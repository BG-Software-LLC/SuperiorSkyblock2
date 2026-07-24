package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.BaseMenu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.layout.InventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
import com.bgsoftware.superiorskyblock.core.menu.dialog.DialogWrapper;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularDialogMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractMenu<V extends AbstractMenuView<V, A>, A extends ViewArgs> extends BaseMenu<V, A> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected boolean isInventoryMenu;

    protected AbstractMenu(String identifier, MenuParseResult<V> parseResult) {
        super(identifier, parseResult.getLayoutBuilder().build(), parseResult.getOpeningSound(),
                parseResult.isPreviousMoveAllowed(), parseResult.isSkipOneItem());
        this.isInventoryMenu = this.menuLayout instanceof InventoryMenuLayout;
    }

    @Override
    public final CompletableFuture<V> createView(SuperiorPlayer superiorPlayer, A args) {
        return super.createView(superiorPlayer, args);
    }

    @Override
    public final CompletableFuture<V> createView(SuperiorPlayer superiorPlayer, A args, @Nullable MenuView<?, ?> previousMenu) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkState(superiorPlayer.isOnline(), "Cannot create view for offline player: " + superiorPlayer.getName());
        Preconditions.checkNotNull(args, "args parameter cannot be null.");
        V view = createViewInternal(superiorPlayer, args, previousMenu);
        addView(view);
        return refreshView(view);
    }

    @Override
    @Deprecated
    public void onClick(InventoryClickEvent clickEvent, V menuView) {
        try (ButtonClickContextImpl ctx = ButtonClickContextImpl.obtain(menuView, clickEvent)) {
            this.onClick(ctx);
        }
    }

    @Override
    public void onClick(ButtonClickContext<V> context) {
        if (!context.getMenuView().isRefreshing())
            super.onClick(context);
    }

    @Override
    protected void onCloseInternal(V menuView) {
        menuView.onClose();
    }

    protected abstract V createViewInternal(SuperiorPlayer superiorPlayer, A args, @Nullable MenuView<?, ?> previousMenu);

    public CompletableFuture<V> refreshView(V view) {
        view.updateTitleArgs();

        if (this.isInventoryMenu) {
            return refreshInventoryViewInternal(view);
        } else {
            return refreshDialogViewInternal(view);
        }
    }

    private CompletableFuture<V> refreshInventoryViewInternal(V view) {
        CompletableFuture<V> result = new CompletableFuture<>();
        buildInventory(view).whenComplete((inventory, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                BukkitExecutor.sync(() -> {
                    view.setInventory(inventory);
                    result.complete(view);
                });
            }
        });
        return result;
    }

    private CompletableFuture<V> refreshDialogViewInternal(V view) {
        CompletableFuture<V> result = new CompletableFuture<>();
        buildDialog(view).whenComplete((dialog, error) -> {
            if (error != null) {
                result.completeExceptionally(error);
            } else {
                BukkitExecutor.sync(() -> {
                    view.setDialog(dialog);
                    result.complete(view);
                });
            }
        });
        return result;
    }

    private CompletableFuture<Inventory> buildInventory(V menuView) {
        if (!Bukkit.isPrimaryThread()) {
            return CompletableFuture.completedFuture(((InventoryMenuLayout<V>) this.menuLayout).buildInventory(menuView));
        }

        CompletableFuture<Inventory> inventoryFuture = new CompletableFuture<>();
        BukkitExecutor.async(() -> inventoryFuture.complete(((InventoryMenuLayout<V>) this.menuLayout).buildInventory(menuView)));
        return inventoryFuture;
    }

    private CompletableFuture<DialogWrapper> buildDialog(V menuView) {
        if (!Bukkit.isPrimaryThread()) {
            return CompletableFuture.completedFuture(((RegularDialogMenuLayoutImpl<V>) this.menuLayout).buildDialog(menuView));
        }

        CompletableFuture<DialogWrapper> dialogFuture = new CompletableFuture<>();
        BukkitExecutor.async(() -> dialogFuture.complete(((RegularDialogMenuLayoutImpl<V>) this.menuLayout).buildDialog(menuView)));
        return dialogFuture;
    }

    @Override
    protected void onButtonClickLackPermission(MenuViewButton<V> menuButton, ButtonClickContext<V> context) {
        if (menuButton instanceof AbstractMenuViewButton)
            ((AbstractMenuViewButton<V>) menuButton).onButtonClickLackPermission(context);
    }

    @Override
    protected boolean onPreButtonClick(MenuViewButton<V> menuButton, ButtonClickContext<V> context) {
        return true;
    }

}
