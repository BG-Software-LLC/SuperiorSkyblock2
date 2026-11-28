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

    default void restoreKilledDragonBattle(Island island, Location location) {
        // Do nothing.
    }

    void removeDragonBattle(Island island, Dimension dimension);

    default void replaceEnderDragon(Island island, EnderDragon enderDragon) {
        // Do nothing.
    }

    void awardTheEndAchievement(Player player);

}
