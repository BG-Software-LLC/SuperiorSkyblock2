package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;


/**
 * IslandUncoopPlayerEvent is called when a player is making another player no longer coop on their island.
 */
public class IslandUncoopPlayerEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer player;
    private final SuperiorPlayer target;
    private final UncoopReason uncoopReason;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island       The island that the leadership of it is transferred.
     * @param player       The player who cooped the target, if exists.
     * @param target       The player that will no longer be coop.
     * @param uncoopReason The reason for the action.
     */
    public IslandUncoopPlayerEvent(Island island, @Nullable SuperiorPlayer player, SuperiorPlayer target, UncoopReason uncoopReason) {
        super(island);
        this.player = player;
        this.target = target;
        this.uncoopReason = uncoopReason;
    }

    /**
     * Get the player who cooped the target, if exists.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return player;
    }

    /**
     * Get the player that will no longer be coop.
     */
    public SuperiorPlayer getTarget() {
        return target;
    }

    /**
     * Get the reason for the action.
     */
    public UncoopReason getUncoopReason() {
        return uncoopReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public enum UncoopReason {

        PLAYER,
        SERVER_LEAVE

    }

}
