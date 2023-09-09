package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeRoleLimitEvent is called when a role-limit of an island is changed.
 */
public class IslandChangeRoleLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final PlayerRole playerRole;

    private int roleLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the role limit of an island.
     *                       If set to null, it means the limit was changed via the console.
     * @param island         The island that the role limit was changed for.
     * @param playerRole     The role that the limit was changed for.
     * @param roleLimit      The new role limit of the role.
     */
    public IslandChangeRoleLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, PlayerRole playerRole, int roleLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.playerRole = playerRole;
        this.roleLimit = roleLimit;
    }

    /**
     * Get the player that changed the role limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the role that the limit was changed for.
     */
    public PlayerRole getPlayerRole() {
        return playerRole;
    }

    /**
     * Get the new role limit of the role.
     */
    public int getRoleLimit() {
        return roleLimit;
    }

    /**
     * Set the new role limit for the role.
     *
     * @param roleLimit The new role limit to set.
     */
    public void setRoleLimit(int roleLimit) {
        Preconditions.checkArgument(roleLimit >= 0, "Cannot set the role limit to a negative limit.");
        this.roleLimit = roleLimit;
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
