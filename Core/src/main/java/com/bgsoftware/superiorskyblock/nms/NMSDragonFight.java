package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public interface NMSDragonFight {

    void prepareEndWorld(World bukkitWorld);

    @Nullable
    EnderDragon getEnderDragon(Island island, Dimension dimension);

    void startDragonBattle(Island island, Location location);

    void removeDragonBattle(Island island, Dimension dimension);

    void awardTheEndAchievement(Player player);

}
