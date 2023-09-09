package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public interface NMSDragonFight {

    void prepareEndWorld(World bukkitWorld);

    @Nullable
    EnderDragon getEnderDragon(Island island);

    void startDragonBattle(Island island, Location location);

    void removeDragonBattle(Island island);

    void awardTheEndAchievement(Player player);

}
