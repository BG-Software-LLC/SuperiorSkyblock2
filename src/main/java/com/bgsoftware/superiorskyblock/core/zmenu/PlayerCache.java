package com.bgsoftware.superiorskyblock.core.zmenu;

import org.bukkit.entity.Player;

public class PlayerCache {

    private final Player player;
    private String islandName;

    public PlayerCache(Player player) {
        this.player = player;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }
}
