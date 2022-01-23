package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Player;

public interface AFKProvider {

    /**
     * Check whether a player is considered AFK.
     *
     * @param player The player to check.
     */
    boolean isAFK(Player player);

}
