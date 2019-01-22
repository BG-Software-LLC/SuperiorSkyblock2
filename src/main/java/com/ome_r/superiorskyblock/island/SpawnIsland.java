package com.ome_r.superiorskyblock.island;

import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

public class SpawnIsland extends Island {

    public SpawnIsland(WrappedLocation wrappedLocation){
        super(null, wrappedLocation);
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
