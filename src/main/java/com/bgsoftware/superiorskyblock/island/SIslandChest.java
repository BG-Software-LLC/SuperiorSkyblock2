package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

public class SIslandChest implements IslandChest {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);
    private final Island island;
    private final int index;
    private Inventory inventory = Bukkit.createInventory(this, 9, plugin.getSettings().getIslandChests().getChestTitle());
    private int contentsUpdateCounter = 0;

    public SIslandChest(Island island, int index) {
        this.island = island;
        this.index = index;
    }

    public static SIslandChest createChest(Island island, int index, ItemStack[] contents) {
        SIslandChest islandChest = new SIslandChest(island, index);
        islandChest.inventory = Bukkit.createInventory(islandChest, contents.length, plugin.getSettings().getIslandChests().getChestTitle());
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
        return inventory.getSize() / 9;
    }

    @Override
    public void setRows(int rows) {
        BukkitExecutor.ensureMain(() -> {
            try {
                updateFlag.set(true);
                ItemStack[] oldContents = inventory.getContents();
                Inventory oldInventory = inventory;
                inventory = Bukkit.createInventory(this, 9 * rows, plugin.getSettings().getIslandChests().getChestTitle());
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
        return inventory.getContents();
    }

    @Override
    public void openChest(SuperiorPlayer superiorPlayer) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        superiorPlayer.runIfOnline(player -> player.openInventory(getInventory()));
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean isUpdating() {
        return updateFlag.get();
    }

    public void updateContents() {
        if (++contentsUpdateCounter >= 50) {
            contentsUpdateCounter = 0;
            IslandsDatabaseBridge.saveIslandChest(island, this);
        } else {
            IslandsDatabaseBridge.markIslandChestsToBeSaved(island, this);
        }
    }

}
