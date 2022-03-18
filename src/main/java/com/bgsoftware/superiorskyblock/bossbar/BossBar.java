package com.bgsoftware.superiorskyblock.bossbar;

import org.bukkit.entity.Player;

public interface BossBar {

    void addPlayer(Player player);

    void removeAll();

    void setProgress(double progress);

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
