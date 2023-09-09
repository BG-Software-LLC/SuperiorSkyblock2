package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;


/**
 * IslandChangeMembersLimitEvent is called when the members limit of an island is changed.
 */
public class IslandChangeMembersLimitEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private int membersLimit;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the members limit of an island.
     *                       If set to null, it means the limit was changed via the console.
     * @param island         The island that the members limit was changed for.
     * @param membersLimit   The new members limit of an island.
     */
    public IslandChangeMembersLimitEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, int membersLimit) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.membersLimit = membersLimit;
    }

    /**
     * Get the player that changed the members limit.
     * If null, it means the limit was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new members limit of the island.
     */
    public int getMembersLimit() {
        return membersLimit;
    }

    /**
     * Set the new members limit of the island.
     *
     * @param membersLimit The new members limit to set.
     */
    public void setMembersLimit(int membersLimit) {
        Preconditions.checkArgument(membersLimit >= 0, "Cannot set the members limit to a negative limit.");
        this.membersLimit = membersLimit;
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
