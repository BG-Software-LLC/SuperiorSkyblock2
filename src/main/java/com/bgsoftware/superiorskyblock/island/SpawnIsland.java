package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

public final class SpawnIsland extends SIsland {

    public SpawnIsland(SBlockPosition wrappedLocation){
        super(null, wrappedLocation);
    }

    @Override
    public boolean isMember(SuperiorPlayer superiorPlayer) {
        return false;
    }

    @Override
    public SSuperiorPlayer getOwner() {
        return null;
    }

    @Override
    public boolean isSpawn() {
        return true;
    }

    @Override
    public int getIslandSize() {
        return plugin.getSettings().maxIslandSize;
    }

    @Override
    public boolean hasPermission(SuperiorPlayer superiorPlayer, IslandPermission islandPermission) {
        return !plugin.getSettings().spawnProtection || super.hasPermission(superiorPlayer, islandPermission);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
