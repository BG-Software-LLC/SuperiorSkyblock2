package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandCreateEvent is called when a new island is created.
 */
public class IslandCreateEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final String schematic;
    private boolean teleport = true;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who created the island.
     * @param island         The island object that was created.
     * @deprecated See IslandCreateEvent(SuperiorPlayer, Island, String)
     */
    @Deprecated
    public IslandCreateEvent(SuperiorPlayer superiorPlayer, Island island) {
        this(superiorPlayer, island, "");
    }

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who created the island.
     * @param island         The island object that was created.
     * @param schematic      The schematic that was used.
     */
    public IslandCreateEvent(SuperiorPlayer superiorPlayer, Island island, String schematic) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.schematic = schematic;
    }

    /**
     * Get the player who created the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the schematic that was used.
     */
    public String getSchematic() {
        return schematic;
    }

    /**
     * Check if the player should get teleported when the process finishes.
     */
    public boolean canTeleport() {
        return teleport;
    }

    /**
     * Set whether or not the player should be teleported to the island when the process finishes.
     */
    public void setTeleport(boolean teleport) {
        this.teleport = teleport;
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
