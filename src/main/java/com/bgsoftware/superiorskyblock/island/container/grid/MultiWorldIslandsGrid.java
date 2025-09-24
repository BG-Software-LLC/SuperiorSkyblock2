package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiWorldIslandsGrid implements IslandsGrid {

    private final Map<String, SingleWorldIslandsGrid> store = new ConcurrentHashMap<>();

    @Override
    public void addIsland(String worldName, long packedPos, Island island) {
        this.store.computeIfAbsent(worldName, d -> new SingleWorldIslandsGrid())
                .addIsland(null, packedPos, island);
    }

    @Override
    public Island removeIslandAt(String worldName, long packedPos) {
        SingleWorldIslandsGrid worldGrid = this.store.get(worldName);
        return worldGrid == null ? null : worldGrid.removeIslandAt(null, packedPos);
    }

    @Override
    public Island getIslandAt(String worldName, long packedPos) {
        SingleWorldIslandsGrid worldGrid = this.store.get(worldName);
        return worldGrid == null ? null : worldGrid.getIslandAt(null, packedPos);
    }

}
