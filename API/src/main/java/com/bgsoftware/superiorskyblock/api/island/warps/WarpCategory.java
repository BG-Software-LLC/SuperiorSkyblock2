package com.bgsoftware.superiorskyblock.api.island.warps;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface WarpCategory {

    /**
     * Get the island of this category.
     */
    Island getIsland();

    /**
     * Get the name of the warp category.
     */
    String getName();

    /**
     * Set a new name to the category.
     * Do not call this method - use Island#renameCategory instead!
     *
     * @param name The new name to set.
     */
    void setName(String name);

    /**
     * Get all the warps of this category.
     */
    List<IslandWarp> getWarps();

    /**
     * Get the slot of the category.
     */
    int getSlot();

    /**
     * Set the slot of the category.
     *
     * @param slot The slot to set.
     */
    void setSlot(int slot);

    /**
     * Get the icon of the category.
     */
    ItemStack getRawIcon();

    /**
     * Get the icon of the category after all placeholders are parsed.
     *
     * @param superiorPlayer The player to parse the placeholders for
     */
    ItemStack getIcon(@Nullable SuperiorPlayer superiorPlayer);

    /**
     * Set the icon of the category.
     *
     * @param icon The icon to set. If null, default icon will be set.
     */
    void setIcon(@Nullable ItemStack icon);

}
