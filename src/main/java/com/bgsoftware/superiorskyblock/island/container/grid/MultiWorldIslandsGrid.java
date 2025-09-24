package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;

public class MultiWorldIslandsGrid implements IslandsGrid {

    private volatile EnumerateMap<Dimension, SingleWorldIslandsGrid> store = new EnumerateMap<>(Dimension.values());

    public MultiWorldIslandsGrid() {
        for (Dimension dimension : Dimension.values()) {
            this.store.put(dimension, new SingleWorldIslandsGrid());
        }
    }

    @Override
    public void addIsland(Dimension dimension, long packedPos, Island island) {
        ensureCapacity(dimension);
        this.store.get(dimension).addIsland(null, packedPos, island);
    }

    @Override
    public Island removeIslandAt(Dimension dimension, long packedPos) {
        ensureCapacity(dimension);
        return this.store.get(dimension).removeIslandAt(null, packedPos);
    }

    @Override
    public Island getIslandAt(Dimension dimension, long packedPos) {
        ensureCapacity(dimension);
        return this.store.get(dimension).getIslandAt(null, packedPos);
    }

    private void ensureCapacity(Dimension dimension) {
        if (dimension.ordinal() >= this.store.size()) {
            synchronized (this) {
                if (dimension.ordinal() >= this.store.size()) {
                    // Need to increase capacity
                    EnumerateMap<Dimension, SingleWorldIslandsGrid> newStore = new EnumerateMap<>(Dimension.values());
                    for (Dimension dim : Dimension.values()) {
                        SingleWorldIslandsGrid currLookupObject = this.store.get(dim);
                        newStore.put(dim, currLookupObject == null ? new SingleWorldIslandsGrid() : currLookupObject);
                    }
                    this.store = newStore;
                }
            }
        }
    }

}
