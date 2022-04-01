package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public interface NMSDragonFight {

    default void prepareEndWorld(World bukkitWorld) {

    }

    void startDragonBattle(Island island, Location location);

    void removeDragonBattle(Island island);

    default void tickBattles() {

    }

    default void setDragonPhase(EnderDragon enderDragon, Object phase) {

    }

    void awardTheEndAchievement(Player player);

}
