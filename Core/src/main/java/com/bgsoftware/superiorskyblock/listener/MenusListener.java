package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class MenusListener extends AbstractGameEventListener {

    private final Int2ObjectMapView<ItemStack> latestClickedItem = CollectionsFactory.createInt2ObjectArrayMap();

    public MenusListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);

        registerCallback(GameEventType.INVENTORY_CLICK_EVENT, GameEventPriority.MONITOR, false, this::onInventoryClickDupePatch);
        registerCallback(GameEventType.INVENTORY_CLOSE_EVENT, GameEventPriority.MONITOR, false, this::onInventoryCloseDupePatch);
        registerCallback(GameEventType.INVENTORY_CLICK_EVENT, GameEventPriority.NORMAL, this::onInventoryMenuClick);
        registerCallback(GameEventType.INVENTORY_CLOSE_EVENT, GameEventPriority.NORMAL, this::onInventoryMenuClose);
        registerCallback(GameEventType.DIALOG_CLICK_EVENT, GameEventPriority.NORMAL, this::onDialogMenuClick);
        registerCallback(GameEventType.DIALOG_CLOSE_EVENT, GameEventPriority.NORMAL, this::onDialogMenuClose);
    }

    /*
     * The following two events are here for patching a dupe glitch caused
     * by shift clicking and closing the inventory at the same time.
     */

    private void onInventoryClickDupePatch(GameEvent<GameEventArgs.InventoryClickEvent> e) {
        if (!e.isCancelled())
            return;

        ItemStack clickedItem = e.getArgs().bukkitEvent.getCurrentItem();
        Inventory inventory = e.getArgs().bukkitEvent.getClickedInventory();

        if (clickedItem != null && inventory != null && inventory.getHolder() instanceof MenuView) {
            int entityId = e.getArgs().bukkitEvent.getWhoClicked().getEntityId();
            latestClickedItem.put(entityId, clickedItem);
            BukkitExecutor.sync(() -> latestClickedItem.remove(entityId), 20L);
        }
    }

    private void onInventoryCloseDupePatch(GameEvent<GameEventArgs.InventoryCloseEvent> e) {
        Player player = (Player) e.getArgs().bukkitEvent.getPlayer();
        ItemStack clickedItem = latestClickedItem.remove(player.getEntityId());
        if (clickedItem != null) {
            BukkitExecutor.sync(() -> {
                player.getInventory().removeItem(clickedItem);
                player.updateInventory();
            }, 1L);
        }
    }

    /* MENU INTERACTIONS HANDLING */

    private void onInventoryMenuClick(GameEvent<GameEventArgs.InventoryClickEvent> e) {
        InventoryView inventoryView = e.getArgs().bukkitEvent.getView();
        Inventory clickedInventory = e.getArgs().bukkitEvent.getClickedInventory();

        Inventory topInventory = inventoryView.getTopInventory();

        if (topInventory == null || clickedInventory == null)
            return;

        InventoryHolder inventoryHolder = topInventory.getHolder();

        if (inventoryHolder instanceof MenuView) {
            e.setCancelled();

            if (clickedInventory.equals(topInventory)) {
                MenuView menuView = (MenuView) inventoryHolder;
                try (ButtonClickContextImpl ctx = ButtonClickContextImpl.obtain(menuView, e.getArgs().bukkitEvent)) {
                    menuView.getMenu().onClick(ctx);
                }
            }
        } else if (inventoryHolder instanceof StackedBlocksDepositMenu) {
            ((StackedBlocksDepositMenu) inventoryHolder).onInteract(e.getArgs().bukkitEvent);
        }
    }

    private void onInventoryMenuClose(GameEvent<GameEventArgs.InventoryCloseEvent> e) {
        Inventory topInventory = e.getArgs().bukkitEvent.getView().getTopInventory();
        InventoryHolder inventoryHolder = topInventory == null ? null : topInventory.getHolder();

        if (inventoryHolder instanceof MenuView) {
            MenuView menuView = (MenuView) inventoryHolder;
            menuView.getMenu().onClose(menuView);
        } else if (inventoryHolder instanceof StackedBlocksDepositMenu) {
            ((StackedBlocksDepositMenu) inventoryHolder).onClose(e.getArgs().bukkitEvent);
        }
    }

    private void onDialogMenuClick(GameEvent<GameEventArgs.DialogClickEvent> e) {
        MenuView menuView = e.getArgs().dialog.getMenuView();
        try (ButtonClickContextImpl ctx = ButtonClickContextImpl.obtain(menuView, e.getArgs())) {
            menuView.getMenu().onClick(ctx);
        }
    }

    private void onDialogMenuClose(GameEvent<GameEventArgs.DialogCloseEvent> e) {
        MenuView menuView = e.getArgs().dialog.getMenuView();
        e.getArgs().dialog.onCloseDialog();
        menuView.getMenu().onClose(menuView);
    }

}
