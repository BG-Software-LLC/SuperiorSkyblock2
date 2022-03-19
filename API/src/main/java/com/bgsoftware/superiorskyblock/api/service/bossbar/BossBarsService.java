package com.bgsoftware.superiorskyblock.api.service.bossbar;

import org.bukkit.entity.Player;

public interface BossBarsService {

    /**
     * Create a new boss-bar.
     *
     * @param player     The player to create the boss-bar for.
     * @param message    The message to display in the boss-bar.
     * @param color      The color of the boss-bar.
     * @param ticksToRun The time to run the boss-bar.
     *                   If set to 0 or below, it will stay forever.
     */
    BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun);

}
