package com.bgsoftware.superiorskyblock.api.service.bossbar;

import org.bukkit.entity.Player;

public interface BossBar {

    /**
     * Display this boss-bar to a player.
     *
     * @param player The player to display the boss-bar to.
     */
    void addPlayer(Player player);

    /**
     * Stop displaying this boss-bar to all the players.
     */
    void removeAll();

    /**
     * Set the progress bar of this boss-bar.
     *
     * @param progress The progress to set.
     */
    void setProgress(double progress);

    /**
     * Get the progress bar of this boss-bar.
     */
    double getProgress();

    enum Color {

        PINK,
        BLUE,
        RED,
        GREEN,
        YELLOW,
        PURPLE,
        WHITE

    }

}
