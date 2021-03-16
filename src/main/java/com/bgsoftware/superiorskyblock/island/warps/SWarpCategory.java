package com.bgsoftware.superiorskyblock.island.warps;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class SWarpCategory implements WarpCategory {

    private static final ItemStack DEFAULT_WARP_ICON = new ItemBuilder(Material.BOOK)
            .withName("&6{0}").build();

    private final List<IslandWarp> islandWarps = new ArrayList<>();
    private final Island island;

    private String name;
    private int slot = 0;
    private ItemStack icon = DEFAULT_WARP_ICON.clone();

    public SWarpCategory(Island island, String name){
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
        SuperiorSkyblockPlugin.debug("Action: Update Warp-Category Name, Island: " + getOwnerName() + ", Category: " + this.name + ", New Name: " + name);
        this.name = name;
        island.getDataHandler().saveWarps();
        island.getDataHandler().saveWarpCategories();
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
        SuperiorSkyblockPlugin.debug("Action: Update Warp-Category Slot, Island: " + getOwnerName() + ", Category: " + this.name + ", New Slot: " + slot);
        this.slot = slot;
        island.getDataHandler().saveWarpCategories();
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
        SuperiorSkyblockPlugin.debug("Action: Update Warp-Category Icon, Island: " + getOwnerName() + ", Category: " + this.name);
        this.icon = icon == null ? DEFAULT_WARP_ICON.clone() : icon.clone();
        island.getDataHandler().saveWarpCategories();
    }

    private String getOwnerName(){
        SuperiorPlayer superiorPlayer = island.getOwner();
        return superiorPlayer == null ? "None" : superiorPlayer.getName();
    }

}
