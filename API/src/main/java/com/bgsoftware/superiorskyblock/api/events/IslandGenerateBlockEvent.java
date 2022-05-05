package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;

/**
 * IslandGenerateBlockEvent is called when a cobblestone generator generates a block.
 */
public class IslandGenerateBlockEvent extends IslandEvent implements Cancellable {

    private final Location location;

    private Key block;
    private boolean placeBlock = true;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island   The island that the block was generated in.
     * @param location The location of the generated block.
     * @param block    The block that was generated.
     */
    public IslandGenerateBlockEvent(Island island, Location location, Key block) {
        super(island);
        this.block = block;
        this.location = location.clone();
    }

    /**
     * Get the location of the block.
     */
    public Location getLocation() {
        return location.clone();
    }

    /**
     * Get the block that was generated.
     */
    public Key getBlock() {
        return block;
    }

    /**
     * Set the block to be generated.
     *
     * @param block The new block.
     */
    public void setBlock(Key block) {
        Preconditions.checkNotNull(block, "Cannot set block to null.");
        this.block = block;
    }

    /**
     * Check whether the generated block should be set manually in the world.
     */
    public boolean isPlaceBlock() {
        return placeBlock;
    }

    /**
     * Set whether the generated block should be set manually in the world.
     */
    public void setPlaceBlock(boolean placeBlock) {
        this.placeBlock = placeBlock;
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
