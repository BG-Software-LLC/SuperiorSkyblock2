package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.collections.Chunk2ObjectMap;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.objects.StackedSnapshot;
import org.bukkit.Chunk;

public class WildStackerSnapshotsContainer {

    private static final Chunk2ObjectMap<StackedSnapshot> cachedSnapshots = new Chunk2ObjectMap<>();

    private WildStackerSnapshotsContainer() {

    }

    public static void takeSnapshot(Chunk chunk) {
        try (ChunkPosition chunkPosition = ChunkPosition.of(chunk)) {
            takeSnapshotInternal(chunk, chunkPosition);
        }
    }

    private static void takeSnapshotInternal(Chunk chunk, ChunkPosition chunkPosition) {
        if (cachedSnapshots.containsKey(chunkPosition))
            return;

        try {
            StackedSnapshot stackedSnapshot;

            try {
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk);
            } catch (Throwable ex) {
                //noinspection deprecation
                stackedSnapshot = WildStackerAPI.getWildStacker().getSystemManager().getStackedSnapshot(chunk, false);
            }

            if (stackedSnapshot != null) {
                cachedSnapshots.put(chunkPosition, stackedSnapshot);
            }
        } catch (Throwable error) {
            Log.error(error, "Received an unexpected error while taking a snapshot for WildStacker:");
        }
    }

    public static void releaseSnapshot(ChunkPosition chunkPosition) {
        cachedSnapshots.remove(chunkPosition);
    }

    public static StackedSnapshot getSnapshot(ChunkPosition chunkPosition) {
        StackedSnapshot stackedSnapshot = cachedSnapshots.get(chunkPosition);

        if (stackedSnapshot == null) {
            throw new RuntimeException("Chunk " + chunkPosition + " is not cached.");
        }

        return stackedSnapshot;
    }

}
