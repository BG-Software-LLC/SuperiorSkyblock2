package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandRenameEvent is called when an island changes its name.
 */
public class IslandRenameEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private String islandName;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that was renamed.
     * @param superiorPlayer The player that renamed the island.
     *                       If null, the island was renamed by console.
     * @param islandName     The new name of the island.
     */
    public IslandRenameEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, String islandName) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.islandName = islandName;
    }

    /**
     * Get the player that changed the privilege to the other player.
     * If null, the privilege was changed by the console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new name of the island.
     */
    public String getIslandName() {
        return islandName;
    }

    /**
     * Set the new name of the island.
     *
     * @param islandName The new name to set.
     */
    public void setIslandName(String islandName) {
        Preconditions.checkNotNull(islandName, "Island names cannot be null.");
        this.islandName = islandName;
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
