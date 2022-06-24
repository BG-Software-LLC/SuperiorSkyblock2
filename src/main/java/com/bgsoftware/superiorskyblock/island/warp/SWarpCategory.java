package com.bgsoftware.superiorskyblock.island.warp;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.database.bridge.IslandsDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public class SWarpCategory implements WarpCategory {

    public static final ItemStack DEFAULT_WARP_ICON = new ItemBuilder(Material.BOOK)
            .withName("&6{0}").build();

    private final List<IslandWarp> islandWarps = new LinkedList<>();
    private final Island island;

    private String name;
    private int slot = 0;
    private ItemStack icon = DEFAULT_WARP_ICON.clone();

    public SWarpCategory(Island island, String name) {
        this.island = island;
        this.name = name;
    }

    @Override
    public Island getIsland() {
        return island;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        PluginDebugger.debug("Action: Update Warp-Category Name, Island: " + getOwnerName() + ", Category: " + this.name + ", New Name: " + name);
        String oldName = this.name;
        this.name = name;
        for (IslandWarp islandWarp : islandWarps)
            IslandsDatabaseBridge.updateWarpCategory(island, islandWarp, oldName);
        IslandsDatabaseBridge.updateWarpCategoryName(island, this, oldName);
    }

    @Override
    public List<IslandWarp> getWarps() {
        return islandWarps;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        PluginDebugger.debug("Action: Update Warp-Category Slot, Island: " + getOwnerName() + ", Category: " + this.name + ", New Slot: " + slot);
        this.slot = slot;
        IslandsDatabaseBridge.updateWarpCategorySlot(island, this);
    }

    @Override
    public ItemStack getRawIcon() {
        return icon.clone();
    }

    @Override
    public ItemStack getIcon(@Nullable SuperiorPlayer superiorPlayer) {
        ItemBuilder itemBuilder = new ItemBuilder(icon)
                .replaceAll("{0}", name);
        return superiorPlayer == null ? itemBuilder.build() : itemBuilder.build(superiorPlayer);
    }

    @Override
    public void setIcon(@Nullable ItemStack icon) {
        PluginDebugger.debug("Action: Update Warp-Category Icon, Island: " + getOwnerName() + ", Category: " + this.name);
        this.icon = icon == null ? DEFAULT_WARP_ICON.clone() : icon.clone();
        IslandsDatabaseBridge.updateWarpCategoryIcon(island, this);
    }

    private String getOwnerName() {
        SuperiorPlayer superiorPlayer = island.getOwner();
        return superiorPlayer == null ? "None" : superiorPlayer.getName();
    }

}
