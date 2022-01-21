package com.bgsoftware.superiorskyblock.api.hooks;

import org.bukkit.entity.Player;

public interface VanishProvider {

    /**
     * Check whether a player is vanished from online players.
     *
     * @param player The player to check
     */
    boolean isVanished(Player player);

}
