package com.bgsoftware.superiorskyblock.core.zmenu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;

public class PlayerCache {

    private final Player player;
    private String islandName;
    private SuperiorPlayer targetPlayer;

    public PlayerCache(Player player) {
        this.player = player;
    }

    public String getIslandName() {
        return islandName;
    }

    public void setIslandName(String islandName) {
        this.islandName = islandName;
    }

    public Player getPlayer() {
        return player;
    }

    public SuperiorPlayer getTargetPlayer() {
        return targetPlayer;
    }

    public void setTargetPlayer(SuperiorPlayer targetPlayer) {
        this.targetPlayer = targetPlayer;
    }
}
