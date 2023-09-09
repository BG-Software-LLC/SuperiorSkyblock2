package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * IslandCreateWarpEvent is called when a new warp is created on an island.
 */
public class IslandCreateWarpEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final String warpName;
    private final Location location;
    private final boolean openToPublic;
    @Nullable
    private final WarpCategory warpCategory;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that created the warp.
     * @param island         The island that the warp was created on.
     * @param warpName       The name of the new warp.
     * @param location       The location of the new warp.
     * @param openToPublic   Whether the island is open to the public.
     * @param warpCategory   The category of the new warp.
     *                       If null, it means the warp will be added to the first found category;
     *                       if no category exists, new one will be created for the warp.
     */
    public IslandCreateWarpEvent(SuperiorPlayer superiorPlayer, Island island, String warpName, Location location,
                                 boolean openToPublic, @Nullable WarpCategory warpCategory) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.warpName = warpName;
        this.location = location.clone();
        this.openToPublic = openToPublic;
        this.warpCategory = warpCategory;
    }

    /**
     * Get the player that created the warp.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the name of the new warp.
     */
    public String getWarpName() {
        return warpName;
    }

    /**
     * Get the location of the new warp.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Get whether the warp is opened to the public.
     */
    public boolean isOpenToPublic() {
        return openToPublic;
    }

    /**
     * Get the category of the new warp.
     * If null, it means the warp will be added to the first found category;
     * if no category exists, new one will be created for the warp.
     */
    @Nullable
    public WarpCategory getWarpCategory() {
        return warpCategory;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
