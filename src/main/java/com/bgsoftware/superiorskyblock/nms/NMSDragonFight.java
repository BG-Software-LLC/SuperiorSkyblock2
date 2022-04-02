package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface NMSDragonFight {

    void prepareEndWorld(World bukkitWorld);

    void startDragonBattle(Island island, Location location);

    void removeDragonBattle(Island island);

    void awardTheEndAchievement(Player player);

}
