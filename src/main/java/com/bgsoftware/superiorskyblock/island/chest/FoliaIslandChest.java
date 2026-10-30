package com.bgsoftware.superiorskyblock.island.chest;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.SIslandChest;
import org.bukkit.GameMode;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public final class FoliaIslandChest {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final SIslandChest chest;
    private final Map<UUID, View> views = new ConcurrentHashMap<>();
    private Inventory storage;
    private long revision;

    public FoliaIslandChest(SIslandChest chest, Inventory storage) {
        this.chest = chest;
        this.storage = storage;
    }

    public synchronized Inventory getInventory() {
        return storage;
    }

    public synchronized ItemStack[] getContents() {
        return copy(storage.getContents());
    }

    public synchronized int getRows() {
        return storage.getSize() / 9;
    }

    public synchronized void initialize(ItemStack[] contents) {
        storage = createInventory(contents.length);
        storage.setContents(copy(contents));
    }

    public void resize(int rows) {
        synchronized (this) {
            ItemStack[] contents = Arrays.copyOf(storage.getContents(), rows * 9);
            storage = createInventory(rows * 9);
            storage.setContents(copy(contents));
            revision++;
        }
        publish();
    }

    public void open(Player player) {
        BukkitExecutor.ensureMain(player, () -> {
            Inventory inventory;
            long currentRevision;
            synchronized (this) {
                inventory = createInventory(storage.getSize());
                inventory.setContents(copy(storage.getContents()));
                currentRevision = revision;
            }
            View view = new View(player, inventory, currentRevision);
            views.put(player.getUniqueId(), view);
            player.openInventory(inventory);
            if (player.getOpenInventory().getTopInventory() != inventory)
                views.remove(player.getUniqueId(), view);
        });
    }

    public void close(Player player, Inventory inventory) {
        View view = views.get(player.getUniqueId());
        if (view != null && view.inventory == inventory)
            views.remove(player.getUniqueId(), view);
    }

    public void click(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        transact(player, event.getView().getTopInventory(), transaction -> {
            ChestTransaction prepared = prepareBundleClick(player, event, transaction);
            if (prepared != null)
                return prepared;
            return transaction.click(event.getRawSlot(), event.getSlot(), event.getAction(),
                    event.getHotbarButton(), event.getClick().name().equals("SWAP_OFFHAND"),
                    player.getGameMode() == GameMode.CREATIVE) ? transaction : null;
        });
    }

    public void drag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        Inventory inventory = event.getView().getTopInventory();
        ItemStack oldCursor = copy(event.getOldCursor());
        ItemStack newCursor = copy(event.getCursor());
        Map<Integer, ItemStack> items = new HashMap<>();
        Map<Integer, Integer> slots = new HashMap<>();
        event.getNewItems().forEach((slot, item) -> {
            items.put(slot, copy(item));
            slots.put(slot, event.getView().convertSlot(slot));
        });
        boolean creative = player.getGameMode() == GameMode.CREATIVE;
        BukkitExecutor.sync(player, () -> {
            if (!java.util.Objects.equals(player.getItemOnCursor(), oldCursor))
                return;
            transact(player, inventory, transaction ->
                    transaction.drag(items, newCursor, slots::get, creative) ? transaction : null);
        });
    }

    private ChestTransaction prepareBundleClick(Player player, InventoryClickEvent event, ChestTransaction transaction) {
        String click = event.getClick().name();
        if (!click.equals("LEFT") && !click.equals("RIGHT"))
            return null;
        int rawSlot = event.getRawSlot();
        ItemStack[] top = transaction.getTopContents();
        ItemStack[] bottom = transaction.getPlayerContents();
        int slot = rawSlot < top.length ? rawSlot : event.getSlot();
        ItemStack[] contents = rawSlot < top.length ? top : bottom;
        if (rawSlot < 0 || slot < 0 || slot >= contents.length)
            return null;
        Pair<Boolean, ItemStack[]> result = plugin.getNMSAlgorithms().processBundleClick(player, contents[slot],
                transaction.getCursor(), click.equals("RIGHT"));
        if (result == null)
            return null;
        contents[slot] = result.getValue()[0];
        ChestTransaction prepared = new ChestTransaction(top, bottom, result.getValue()[1]);
        if (!result.getKey())
            prepared.click(rawSlot, event.getSlot(), event.getAction(), event.getHotbarButton(),
                    false, player.getGameMode() == GameMode.CREATIVE);
        return prepared;
    }

    private void transact(Player player, Inventory inventory,
                          Function<ChestTransaction, ChestTransaction> action) {
        View view = views.get(player.getUniqueId());
        if (view == null || view.inventory != inventory)
            return;
        ItemStack[] originalTop;
        ItemStack[] originalPlayer = copy(player.getInventory().getContents());
        ItemStack originalCursor = copy(player.getItemOnCursor());
        long expectedRevision;
        synchronized (this) {
            if (view.revision != revision || inventory.getSize() != storage.getSize() ||
                    !Arrays.equals(inventory.getContents(), storage.getContents())) {
                refresh(view);
                return;
            }
            originalTop = copy(storage.getContents());
            expectedRevision = revision;
        }
        ChestTransaction transaction = action.apply(new ChestTransaction(originalTop, originalPlayer, originalCursor));
        if (transaction == null)
            return;
        List<Item> dropped = prepareDrops(player, transaction.getDrops());
        if (dropped == null)
            return;
        boolean committed = false;
        try {
            synchronized (this) {
                if (revision == expectedRevision && views.get(player.getUniqueId()) == view &&
                        player.getOpenInventory().getTopInventory() == inventory &&
                        Arrays.equals(storage.getContents(), originalTop) &&
                        Arrays.equals(player.getInventory().getContents(), originalPlayer) &&
                        java.util.Objects.equals(player.getItemOnCursor(), originalCursor)) {
                    storage.setContents(transaction.getTopContents());
                    player.getInventory().setContents(transaction.getPlayerContents());
                    player.setItemOnCursor(transaction.getCursor());
                    revision++;
                    committed = true;
                }
            }
        } finally {
            if (!committed)
                dropped.forEach(Item::remove);
        }
        if (committed)
            publish();
        else
            refresh(view);
    }

    private List<Item> prepareDrops(Player player, List<ItemStack> items) {
        List<Item> dropped = new ArrayList<>();
        try {
            for (ItemStack stack : items) {
                Item item = player.getWorld().dropItem(player.getLocation(), stack.clone());
                item.setPickupDelay(40);
                dropped.add(item);
                PlayerDropItemEvent event = new PlayerDropItemEvent(player, item);
                plugin.getServer().getPluginManager().callEvent(event);
                if (event.isCancelled()) {
                    dropped.forEach(Item::remove);
                    return null;
                }
            }
            return dropped;
        } catch (Throwable error) {
            dropped.forEach(Item::remove);
            throw error;
        }
    }

    private Inventory createInventory(int size) {
        return plugin.getProviders().getUIProvider().createInventory(chest, size,
                plugin.getSettings().getIslandChests().getChestTitle());
    }

    private void publish() {
        chest.updateContents();
        views.values().forEach(this::refresh);
    }

    private void refresh(View view) {
        if (!view.refreshQueued.compareAndSet(false, true))
            return;
        plugin.getTaskScheduler().entity(view.player, () -> {
            view.refreshQueued.set(false);
            if (views.get(view.player.getUniqueId()) != view)
                return;
            if (view.player.getOpenInventory().getTopInventory() != view.inventory) {
                views.remove(view.player.getUniqueId(), view);
                return;
            }
            synchronized (this) {
                if (view.inventory.getSize() != storage.getSize()) {
                    BukkitExecutor.sync(view.player, () -> {
                        if (views.get(view.player.getUniqueId()) == view &&
                                view.player.getOpenInventory().getTopInventory() == view.inventory)
                            open(view.player);
                    });
                    return;
                }
                view.inventory.setContents(copy(storage.getContents()));
                view.revision = revision;
            }
            view.player.updateInventory();
        }, () -> views.remove(view.player.getUniqueId(), view), 1L, 0L);
    }

    private static ItemStack copy(ItemStack item) {
        return item == null ? null : item.clone();
    }

    private static ItemStack[] copy(ItemStack[] contents) {
        ItemStack[] result = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++)
            result[i] = copy(contents[i]);
        return result;
    }

    private static final class View {

        private final Player player;
        private final Inventory inventory;
        private final AtomicBoolean refreshQueued = new AtomicBoolean();
        private volatile long revision;

        private View(Player player, Inventory inventory, long revision) {
            this.player = player;
            this.inventory = inventory;
            this.revision = revision;
        }
    }
}
