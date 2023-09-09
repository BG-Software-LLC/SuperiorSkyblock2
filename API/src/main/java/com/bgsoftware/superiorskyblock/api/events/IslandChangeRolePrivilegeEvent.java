package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeRolePrivilegeEvent is called when a privilege is changed for a role on an island.
 */
public class IslandChangeRolePrivilegeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final PlayerRole playerRole;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the privilege was changed in.
     * @param superiorPlayer The player that changed the privilege to the other role.
     *                       If null, the privilege was changed by the console.
     * @param playerRole     The role that the privilege was changed for.
     */
    public IslandChangeRolePrivilegeEvent(Island island, @Nullable SuperiorPlayer superiorPlayer, PlayerRole playerRole) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.playerRole = playerRole;
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
     * Get the role that the privilege was changed for.
     */
    public PlayerRole getPlayerRole() {
        return playerRole;
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
