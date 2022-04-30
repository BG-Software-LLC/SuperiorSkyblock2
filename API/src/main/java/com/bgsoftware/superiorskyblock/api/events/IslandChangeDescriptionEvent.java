package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeDescriptionEvent is called when an island changes its description.
 */
public class IslandChangeDescriptionEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private String description;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that its description was changed.
     * @param superiorPlayer The player that changed the description of the island.
     * @param description    The new description of the island.
     */
    public IslandChangeDescriptionEvent(Island island, SuperiorPlayer superiorPlayer, String description) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.description = description;
    }

    /**
     * Get the player that changed the description of the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new description of the island.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the new description of the island.
     *
     * @param description The new description to set.
     */
    public void setIslandName(String description) {
        Preconditions.checkNotNull(description, "Island descriptions cannot be null.");
        this.description = description;
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
