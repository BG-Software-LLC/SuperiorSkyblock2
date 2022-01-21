package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Player;

public interface PermissionsProvider {

    /**
     * Check whether a player has permission.
     *
     * @param player     The player to check permissions for.
     * @param permission The permission to check.
     * @return whether the player has permission excluding his operator status.
     * This means that the permission must be given explicitly to the player for the method to return true.
     */
    boolean hasPermission(Player player, String permission);

}
