package com.bgsoftware.superiorskyblock.core.zmenu;

import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import org.bukkit.entity.Player;

public class PlayerCache {

    private final Player player;
    private String islandName;
    private SuperiorPlayer targetPlayer;
    private SortingType sortingType;

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

    public SortingType getSortingType() {
        return this.sortingType == null ? SortingTypes.BY_WORTH : this.sortingType;
    }

    public void setSortingType(SortingType sortingType) {
        this.sortingType = sortingType;
    }
}
