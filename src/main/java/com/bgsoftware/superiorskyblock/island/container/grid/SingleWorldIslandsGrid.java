package com.bgsoftware.superiorskyblock.island.container.grid;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Long2ObjectMapView;

import java.util.concurrent.locks.StampedLock;

public class SingleWorldIslandsGrid implements IslandsGrid {

    private final Long2ObjectMapView<Island> store = CollectionsFactory.createLong2ObjectLinkedHashMap();
    private final StampedLock lock = new StampedLock();

    @Override
    public void addIsland(String unused, long packedPos, Island island) {
        long stamp = this.lock.writeLock();
        try {
            this.store.put(packedPos, island);
        } finally {
            this.lock.unlockWrite(stamp);
        }
    }

    @Override
    public Island removeIslandAt(String unused, long packedPos) {
        long stamp = this.lock.writeLock();
        Island oldValue;
        try {
            oldValue = this.store.remove(packedPos);
        } finally {
            this.lock.unlockWrite(stamp);
        }
        return oldValue;
    }

    @Override
    public Island getIslandAt(String unused, long packedPos) {
        long stamp = this.lock.tryOptimisticRead();
        Island island = this.store.get(packedPos);

        // Validate that no write occurred while reading
        if (!lock.validate(stamp)) {
            // Fallback to a proper read lock
            stamp = this.lock.readLock();
            try {
                island = this.store.get(packedPos);
            } finally {
                this.lock.unlockRead(stamp);
            }
        }
        return island;
    }

}
