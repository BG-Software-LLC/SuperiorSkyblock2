package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import com.bgsoftware.superiorskyblock.island.chest.FoliaIslandChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
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

public class MenusListener extends AbstractGameEventListener implements Listener {

    private final Map<Integer, ItemStack> latestClickedItem = new ConcurrentHashMap<>();

    public MenusListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        if (BukkitExecutor.isFolia())
            plugin.getServer().getPluginManager().registerEvents(this, plugin);

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
            BukkitExecutor.sync(e.getArgs().bukkitEvent.getWhoClicked(), () -> latestClickedItem.remove(entityId, clickedItem), 20L);
        }
    }

    private void onInventoryCloseDupePatch(GameEvent<GameEventArgs.InventoryCloseEvent> e) {
        Player player = (Player) e.getArgs().bukkitEvent.getPlayer();
        ItemStack clickedItem = latestClickedItem.remove(player.getEntityId());
        if (clickedItem != null) {
            BukkitExecutor.sync(player, () -> {
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
        } else if (inventoryHolder instanceof SIslandChest) {
            FoliaIslandChest chest = ((SIslandChest) inventoryHolder).getFoliaChest();
            if (chest != null)
                chest.close((Player) e.getArgs().bukkitEvent.getPlayer(), topInventory);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIslandChestOpen(InventoryOpenEvent event) {
        FoliaIslandChest chest = getFoliaChest(event.getInventory());
        if (chest != null && event.getInventory() == chest.getInventory()) {
            event.setCancelled(true);
            Player player = (Player) event.getPlayer();
            BukkitExecutor.sync(player, () -> chest.open(player));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIslandChestClick(InventoryClickEvent event) {
        FoliaIslandChest chest = getFoliaChest(event.getView().getTopInventory());
        if (chest != null)
            chest.click(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onIslandChestDrag(InventoryDragEvent event) {
        FoliaIslandChest chest = getFoliaChest(event.getView().getTopInventory());
        if (chest != null)
            chest.drag(event);
    }

    private FoliaIslandChest getFoliaChest(Inventory inventory) {
        InventoryHolder holder = inventory == null ? null : inventory.getHolder();
        return holder instanceof SIslandChest ? ((SIslandChest) holder).getFoliaChest() : null;
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
