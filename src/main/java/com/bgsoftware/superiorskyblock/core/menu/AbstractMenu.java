package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.BaseMenu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractMenu<V extends AbstractMenuView<V, A>, A extends ViewArgs> extends BaseMenu<V, A> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected AbstractMenu(String identifier, MenuParseResult<V> parseResult) {
        super(identifier, parseResult.getLayoutBuilder().build(), parseResult.getOpeningSound(),
                parseResult.isPreviousMoveAllowed(), parseResult.isSkipOneItem());
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
    public void onClick(InventoryClickEvent clickEvent, V menuView) {
        if (!menuView.isRefreshing())
            super.onClick(clickEvent, menuView);
    }

    @Override
    protected void onCloseInternal(InventoryCloseEvent closeEvent, V menuView) {
        menuView.onClose();
    }

    protected abstract V createViewInternal(SuperiorPlayer superiorPlayer, A args, @Nullable MenuView<?, ?> previousMenu);

    public CompletableFuture<V> refreshView(V view) {
        CompletableFuture<V> res = new CompletableFuture<>();
        buildInventory(view).whenComplete((inventory, error) -> {
            if (error != null) {
                res.completeExceptionally(error);
            } else {
                BukkitExecutor.sync(() -> {
                    view.setInventory(inventory);
                    res.complete(view);
                });
            }
        });
        return res;
    }

    public CompletableFuture<Inventory> buildInventory(V menuView) {
        if (!Bukkit.isPrimaryThread()) {
            return CompletableFuture.completedFuture(this.menuLayout.buildInventory(menuView));
        }

        CompletableFuture<Inventory> inventoryFuture = new CompletableFuture<>();
        BukkitExecutor.async(() -> inventoryFuture.complete(this.menuLayout.buildInventory(menuView)));
        return inventoryFuture;
    }

    @Override
    protected void onButtonClickLackPermission(MenuViewButton<V> menuButton, InventoryClickEvent clickEvent) {
        if (menuButton instanceof AbstractMenuViewButton)
            ((AbstractMenuViewButton<V>) menuButton).onButtonClickLackPermission(clickEvent);
    }

    protected boolean onPreButtonClick(MenuViewButton<V> menuButton, InventoryClickEvent clickEvent) {
        return true;
    }

}
