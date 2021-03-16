package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.data.SIslandDataHandler;
import com.bgsoftware.superiorskyblock.utils.database.Query;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SIslandChest implements IslandChest {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Inventory inventory = Bukkit.createInventory(this, 9, plugin.getSettings().islandChestTitle);
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);
    private int contentsUpdateCounter = 0;

    private final Island island;
    private final int index;

    public SIslandChest(Island island, int index){
        this.island = island;
        this.index = index;
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
        Executor.ensureMain(() -> {
            try {
                updateFlag.set(true);
                ItemStack[] oldContents = inventory.getContents();
                List<HumanEntity> toUpdate = new ArrayList<>(inventory.getViewers());
                Inventory oldInventory = inventory;
                inventory = Bukkit.createInventory(this, 9 * rows, plugin.getSettings().islandChestTitle);
                inventory.setContents(Arrays.copyOf(oldContents, 9 * rows));
                toUpdate.forEach(humanEntity -> {
                    if(humanEntity.getOpenInventory().getTopInventory().equals(oldInventory))
                        humanEntity.openInventory(inventory);
                });
            }finally {
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

    public boolean isUpdating(){
        return updateFlag.get();
    }

    public void updateContents(){
        ((SIslandDataHandler) island.getDataHandler()).setModified(Query.ISLAND_SET_ISLAND_CHEST);
        if(++contentsUpdateCounter >= 50){
            contentsUpdateCounter = 0;
            island.getDataHandler().saveIslandChest();
        }
    }

    public static SIslandChest createChest(Island island, int index, ItemStack[] contents){
        SIslandChest islandChest = new SIslandChest(island, index);
        islandChest.inventory = Bukkit.createInventory(islandChest, contents.length, plugin.getSettings().islandChestTitle);
        islandChest.inventory.setContents(contents);
        return islandChest;
    }

}
