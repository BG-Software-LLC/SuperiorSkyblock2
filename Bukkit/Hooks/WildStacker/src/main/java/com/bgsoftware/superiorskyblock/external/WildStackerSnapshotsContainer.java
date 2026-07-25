package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Chunk;

import java.util.function.Function;

public class WildStackerSnapshotsContainer {

    private static final Synchronized<Chunk2ObjectMap<RefCount<StackedSnapshot>>> cachedSnapshots = Synchronized.of(new Chunk2ObjectMap<>());

    private WildStackerSnapshotsContainer() {

    }

    public static void takeSnapshot(Chunk chunk) {
        try (ChunkPosition chunkPosition = ChunkPosition.of(chunk)) {
            cachedSnapshots.write(cachedSnapshots ->
                    takeSnapshotInternal(chunk, chunkPosition, cachedSnapshots));
        }
    }

    private static void takeSnapshotInternal(Chunk chunk, ChunkPosition chunkPosition, Chunk2ObjectMap<RefCount<StackedSnapshot>> cachedSnapshots) {
        RefCount<StackedSnapshot> refCountBase = cachedSnapshots.get(chunkPosition);
        if (refCountBase != null) {
            refCountBase.incRef();
            return;
        }

        refCountBase = new RefCount<>();

        try {
            StackedSnapshot stackedSnapshot;

            try {
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk);
            } catch (Throwable ex) {
                //noinspection deprecation
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false);
            }

            if (stackedSnapshot != null) {
                refCountBase.set(stackedSnapshot);
                cachedSnapshots.put(chunkPosition, refCountBase);
            }
        } catch (Throwable error) {
            Log.error(error, "Received an unexpected error while taking a snapshot for WildStacker:");
        }
    }

    public static void releaseSnapshot(ChunkPosition chunkPosition) {
        cachedSnapshots.write(m -> {
            RefCount<StackedSnapshot> refCountBase = m.get(chunkPosition);
            if (refCountBase != null) {
                if (refCountBase.defRef())
                    m.remove(chunkPosition);
            }
        });
    }

    public static <R> R accessStackedSnapshot(ChunkPosition chunkPosition, Function<StackedSnapshot, R> function) {
        return cachedSnapshots.readAndGet(cachedSnapshots -> {
            RefCount<StackedSnapshot> refCountBase = cachedSnapshots.get(chunkPosition);
            if (refCountBase == null)
                return null;
            return refCountBase.readAndGet(function);
        });
    }

    private static class RefCount<T> {

        private int refCount;
        private Synchronized<T> handle;

        synchronized void incRef() {
            if (this.handle == null)
                throw new IllegalStateException("Inc ref on null RefCount object " + this);

            int ref = this.handle.writeAndGet(unused -> {
                return ++this.refCount;
            });

            if (ref <= 0)
                throw new IllegalStateException("Inc ref on null RefCount object " + this);
        }

        synchronized boolean defRef() {
            if (this.handle == null)
                throw new IllegalStateException("Dec ref on null RefCount object " + this);

            int ref = this.handle.writeAndGet(unused -> {
                return --this.refCount;
            });

            if (ref == 0) {
                this.handle = null;
                return true;
            } else if (ref < 0) {
                throw new IllegalStateException("Dec ref on null RefCount object " + this);
            }

            return false;
        }

        synchronized <R> R readAndGet(Function<T, R> function) {
            if (this.handle == null)
                throw new IllegalStateException("Access null RefCount object " + this);

            return this.handle.writeAndGet(handle -> {
                if (this.refCount <= 0)
                    throw new IllegalStateException("Access null RefCount object " + this);

                return function.apply(handle);
            });
        }

        synchronized void set(T handle) {
            this.handle = Synchronized.of(handle);
            incRef();
        }

        @Override
        public synchronized String toString() {
            if (this.handle == null) {
                return "{ ref = " + this.refCount + ", handle = " + this.handle + "}";
            } else {
                return this.handle.readAndGet(handle -> {
                    return "{ ref = " + this.refCount + ", handle = " + handle + "}";
                });
            }

        }
    }

}
