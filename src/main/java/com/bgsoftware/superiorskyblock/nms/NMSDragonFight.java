package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public interface NMSDragonFight {

    void startDragonBattle(Island island, Location location);

    void removeDragonBattle(Island island);

    void tickBattles();

    void setDragonPhase(EnderDragon enderDragon, Object phase);

    void awardTheEndAchievement(Player player);

}
