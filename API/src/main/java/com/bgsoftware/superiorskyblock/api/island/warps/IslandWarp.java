package com.bgsoftware.superiorskyblock.api.island.warps;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.wrappers.WorldPosition;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public interface IslandWarp {

    /**
     * Get the island of this warp.
     */
    Island getIsland();

    /**
     * Get the name of the warp.
     */
    String getName();

    /**
     * Set the name of the warp.
     * Do not call this method - use Island#renameWarp instead!
     *
     * @param name The new name to set.
     */
    void setName(String name);

    /**
     * Get the location of the warp.
     */
    Location getLocation();

    /**
     * Get the location of the warp.
     *
     * @param location Location object to re-use.
     */
    @Nullable
    Location getLocation(@Nullable Location location);

    /**
     * Set the location of the warp.
     *
     * @param location The new location to set.
     */
    void setLocation(Location location);

    /**
     * Get the position of the warp.
     */
    WorldPosition getPosition();

    /**
     * Get the dimension of the position of the warp.
     */
    Dimension getPositionDimension();

    /**
     * Set the position of the warp.
     *
     * @param dimension     The dimension of the new position to set.
     * @param worldPosition The new position to set.
     */
    void setPosition(Dimension dimension, WorldPosition worldPosition);

    /**
     * Check if the warp is private or public to visitors.
     */
    boolean hasPrivateFlag();

    /**
     * Set the private flag of the island warp.
     *
     * @param privateFlag The flag to set.
     */
    void setPrivateFlag(boolean privateFlag);

    /**
     * Get the icon of the warp.
     * May be null if the warp has no special icon.
     */
    @Nullable
    ItemStack getRawIcon();

    /**
     * Get the icon of the warp after all placeholders are parsed.
     * May be null if the warp has no special icon.
     *
     * @param superiorPlayer The player to parse the placeholders for
     */
    @Nullable
    ItemStack getIcon(@Nullable SuperiorPlayer superiorPlayer);

    /**
     * Set the icon of the warp.
     * May be null if the warp should have no special icon.
     *
     * @param icon The icon to set.
     */
    void setIcon(@Nullable ItemStack icon);

    /**
     * Get the category of the warp.
     */
    WarpCategory getCategory();

}
