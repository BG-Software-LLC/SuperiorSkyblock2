package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class SIslandChest implements IslandChest {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private Inventory inventory = Bukkit.createInventory(this, 9, plugin.getSettings().islandChestTitle);
    private final AtomicBoolean updateFlag = new AtomicBoolean(false);
    private int contentsUpdateCounter = 0;

    private final Island island;

    public SIslandChest(Island island){
        this.island = island;
    }

    @Override
    public int getRows() {
        return inventory.getSize() / 9;
    }

    @Override
    public void setRows(int rows) {
        Executor.ensureMain(() -> {
            updateFlag.set(true);
            ItemStack[] oldContents = inventory.getContents();
            List<HumanEntity> toUpdate = inventory.getViewers();
            inventory = Bukkit.createInventory(this, 9 * rows, plugin.getSettings().islandChestTitle);
            inventory.setContents(Arrays.copyOf(oldContents, 9 * rows));
            toUpdate.forEach(humanEntity -> humanEntity.openInventory(inventory));
            updateFlag.set(false);
        });
    }

    @Override
    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    @Override
    public void openChest(SuperiorPlayer superiorPlayer) {
        if(superiorPlayer.isOnline())
            superiorPlayer.asPlayer().openInventory(getInventory());
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public boolean isUpdating(){
        return updateFlag.get();
    }

    public void updateContents(){
        if(++contentsUpdateCounter >= Bukkit.getOnlinePlayers().size() * 10){
            contentsUpdateCounter = 0;
            ((SIsland) island).saveIslandChests();
        }
    }

    public static SIslandChest createChest(Island island, ItemStack[] contents){
        SIslandChest islandChest = new SIslandChest(island);
        islandChest.inventory = Bukkit.createInventory(islandChest, contents.length, plugin.getSettings().islandChestTitle);
        islandChest.inventory.setContents(contents);
        return islandChest;
    }

}
