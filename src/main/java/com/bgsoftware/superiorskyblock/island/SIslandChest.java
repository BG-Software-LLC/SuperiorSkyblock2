package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.chest.FoliaIslandChest;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SIslandChest implements IslandChest {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);
    private final Island island;
    private final int index;
    private Inventory inventory = plugin.getProviders().getUIProvider().createInventory(
            this, 9, plugin.getSettings().getIslandChests().getChestTitle());
    private final AtomicInteger contentsUpdateCounter = new AtomicInteger();
    private final FoliaIslandChest foliaChest;

    public SIslandChest(Island island, int index) {
        this.island = island;
        this.index = index;
        this.foliaChest = BukkitExecutor.isFolia() ? new FoliaIslandChest(this, inventory) : null;
    }

    public static SIslandChest createChest(Island island, int index, ItemStack[] contents) {
        SIslandChest islandChest = new SIslandChest(island, index);
        if (islandChest.foliaChest != null) {
            islandChest.foliaChest.initialize(contents);
            return islandChest;
        }
        islandChest.inventory = plugin.getProviders().getUIProvider().createInventory(
                islandChest, contents.length, plugin.getSettings().getIslandChests().getChestTitle());
        islandChest.inventory.setContents(contents);
        return islandChest;
    }

    @Override
    public Island getIsland() {
        return island;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public int getRows() {
        return foliaChest == null ? inventory.getSize() / 9 : foliaChest.getRows();
    }

    @Override
    public void setRows(int rows) {
        if (foliaChest != null) {
            foliaChest.resize(rows);
            return;
        }
        BukkitExecutor.ensureMain(() -> {
            try {
                updateFlag.set(true);
                ItemStack[] oldContents = inventory.getContents();
                Inventory oldInventory = inventory;
                inventory = plugin.getProviders().getUIProvider().createInventory(this, 9 * rows, plugin.getSettings().getIslandChests().getChestTitle());
                inventory.setContents(Arrays.copyOf(oldContents, 9 * rows));
                inventory.getViewers().forEach(humanEntity -> {
                    if (humanEntity.getOpenInventory().getTopInventory().equals(oldInventory))
                        humanEntity.openInventory(inventory);
                });
            } finally {
                updateFlag.set(false);
            }
        });
    }

    @Override
    public ItemStack[] getContents() {
        return foliaChest == null ? inventory.getContents() : foliaChest.getContents();
    }

    @Override
    public void openChest(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        superiorPlayer.runIfOnline(player -> {
            if (foliaChest == null)
                player.openInventory(getInventory());
            else
                foliaChest.open(player);
        });
    }

    @Override
    public Inventory getInventory() {
        return foliaChest == null ? inventory : foliaChest.getInventory();
    }

    public FoliaIslandChest getFoliaChest() {
        return foliaChest;
    }

    public boolean isUpdating() {
        return updateFlag.get();
    }

    public void updateContents() {
        if (contentsUpdateCounter.incrementAndGet() % 50 == 0) {
            IslandsDatabaseBridge.saveIslandChest(island, this);
        } else {
            IslandsDatabaseBridge.markIslandChestsToBeSaved(island, this);
        }
    }

}
