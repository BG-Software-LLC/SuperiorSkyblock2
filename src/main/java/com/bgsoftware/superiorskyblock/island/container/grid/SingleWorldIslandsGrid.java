package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;

public class SingleWorldIslandsGrid implements IslandsGrid {

    private Synchronized<Long2ObjectMapView<Island>> store;

    public SingleWorldIslandsGrid() {
        this.store = Synchronized.of(CollectionsFactory.createLong2ObjectLinkedHashMap());
    }

    @Override
    public void addIsland(Dimension unused, long packedPos, Island island) {
        this.store.write(store -> store.put(packedPos, island));
    }

    @Override
    public Island removeIslandAt(Dimension unused, long packedPos) {
        return this.store.writeAndGet(store -> store.remove(packedPos));
    }

    @Override
    public Island getIslandAt(Dimension unused, long packedPos) {
        return this.store.readAndGet(store -> store.get(packedPos));
    }

}
