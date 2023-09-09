package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.event.Cancellable;

/**
 * IslandEnterPortalEvent is called when a player enters a portal on an island.
 */
public class IslandEnterPortalEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final PortalType portalType;
    private World.Environment destination;
    @Nullable
    private Schematic schematic;
    private boolean ignoreInvalidSchematic;

    private boolean cancelled = false;

    /**
     * Constructor of the event
     *
     * @param island                 The island that the player entered the portal at.
     * @param superiorPlayer         The player that entered the portal.
     * @param portalType             The type of the portal used.
     * @param destination            The destination of the portal.
     * @param schematic              The schematic to be placed, if exists.
     * @param ignoreInvalidSchematic Whether to ignore invalid schematics.
     */
    public IslandEnterPortalEvent(Island island, SuperiorPlayer superiorPlayer, PortalType portalType,
                                  World.Environment destination, @Nullable Schematic schematic,
                                  boolean ignoreInvalidSchematic) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.portalType = portalType;
        this.destination = destination;
        this.schematic = schematic;
        this.ignoreInvalidSchematic = ignoreInvalidSchematic;
    }

    /**
     * Get the player that entered the portal.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the type of the portal.
     */
    public PortalType getPortalType() {
        return portalType;
    }

    /**
     * Get the destination world of the portal.
     */
    public World.Environment getDestination() {
        return destination;
    }

    /**
     * Set the destination of the teleportation.
     *
     * @param destination The destination to set.
     */
    public void setDestination(World.Environment destination) {
        this.destination = destination;
    }

    /**
     * Get the schematic that will be placed before teleporting.
     *
     * @return The schematic that will be placed, or null if no schematic should be placed.
     */
    @Nullable
    public Schematic getSchematic() {
        return schematic;
    }

    /**
     * Set the schematic that will be placed.
     * Warning: If a schematic was already been placed and you set this to non-null, a new schematic will be placed
     * on top of the already blocks.
     *
     * @param schematic The schematic to be placed.
     *                  If null, no schematic will be used and {@link #isIgnoreInvalidSchematic()} will be set to true.
     */
    public void setSchematic(@Nullable Schematic schematic) {
        this.schematic = schematic;
        if (schematic == null)
            setIgnoreInvalidSchematic(true);
    }

    /**
     * Check whether the plugin should not send a warning if {@link #getSchematic()} is null.
     */
    public boolean isIgnoreInvalidSchematic() {
        return ignoreInvalidSchematic;
    }

    /**
     * Set whether the plugin should send a warning to the player if {@link #getSchematic()} is null.
     *
     * @param ignoreInvalidSchematic Whether to send a warning or not.
     */
    public void setIgnoreInvalidSchematic(boolean ignoreInvalidSchematic) {
        this.ignoreInvalidSchematic = ignoreInvalidSchematic;
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
