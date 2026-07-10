package com.bgsoftware.superiorskyblock.api.service.bossbar;

import org.bukkit.entity.Player;

import java.util.Locale;

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
        WHITE;

        public static Color getSafe(String name) {
            try {
                return Color.valueOf(name.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException exception) {
                return PINK;
            }
        }

    }

    enum Style {

        SOLID("PROGRESS"),
        SEGMENTED_6("NOTCHED_6"),
        SEGMENTED_10("NOTCHED_10"),
        SEGMENTED_12("NOTCHED_12"),
        SEGMENTED_20("NOTCHED_20");

        private final String overlayName;

        Style(String overlayName) {
            this.overlayName = overlayName;
        }

        public String getOverlayName() {
            return overlayName;
        }

        public static Style getSafe(String name) {
            switch (name.toUpperCase(Locale.ENGLISH)) {
                case "SEGMENTED_6":
                case "NOTCHED_6":
                    return SEGMENTED_6;
                case "SEGMENTED_10":
                case "NOTCHED_10":
                    return SEGMENTED_10;
                case "SEGMENTED_12":
                case "NOTCHED_12":
                    return SEGMENTED_12;
                case "SEGMENTED_20":
                case "NOTCHED_20":
                    return SEGMENTED_20;
                default:
                    return SOLID;
            }
        }

    }

}
