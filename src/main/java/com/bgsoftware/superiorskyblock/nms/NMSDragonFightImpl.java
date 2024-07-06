package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;

public class NMSDragonFightImpl implements NMSDragonFight {

    @Override
    public void prepareEndWorld(World bukkitWorld) {
        // Do nothing.
    }

    @Nullable
    @Override
    public EnderDragon getEnderDragon(Island island, Dimension dimension) {
        return null;
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        // Do nothing.
    }

    @Override
    public void removeDragonBattle(Island island, Dimension dimension) {
        // Do nothing.
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        // Do nothing.
    }

}
