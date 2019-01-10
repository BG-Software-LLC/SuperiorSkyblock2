package com.ome_r.superiorskyblock.island;

import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnIsland extends Island {

    public SpawnIsland(World world){
        super(null, new Location(world, 0, 100, 0));
    }

    @Override
    public boolean isMember(WrappedPlayer wrappedPlayer) {
        return false;
    }

    @Override
    public WrappedPlayer getOwner() {
        return null;
    }

    @Override
    public int getIslandLevel() {
        return plugin.getSettings().maxIslandSize;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
