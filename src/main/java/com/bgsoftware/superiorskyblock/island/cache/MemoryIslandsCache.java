package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataInput;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.cache.CachedIslandInfo;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class MemoryIslandsCache extends IslandsCache {

    private final byte[] islandsDataBytes;

    @SuppressWarnings("UnstableApiUsage")
    protected static MemoryIslandsCache create(IslandsContainer islandsContainer, Collection<CachedIslandInfo> islands) {
        ByteArrayOutputStream islandsDataStream = new ByteArrayOutputStream();
        ByteArrayDataOutput islandsData = ByteStreams.newDataOutput(islandsDataStream);
        ByteArrayOutputStream islandsTableStream = new ByteArrayOutputStream();
        ByteArrayDataOutput islandsTable = ByteStreams.newDataOutput(islandsTableStream);

        // Sort the islands by their uuids.
        List<CachedIslandInfo> sortedIslands = new SequentialListBuilder<CachedIslandInfo>()
                .sorted(Comparator.comparing(o -> o.uuid))
                .build(islands);

        // Start serializing the islands.
        sortedIslands.forEach(island -> {
            int islandDataIndex = islandsDataStream.size();

            islandsTable.writeLong(island.uuid.getMostSignificantBits());
            islandsTable.writeLong(island.uuid.getLeastSignificantBits());
            islandsTable.writeInt(islandDataIndex);

            islandsData.write(serializeIsland(island));
        });

        return new MemoryIslandsCache(islandsContainer, islandsTable.toByteArray(), islandsData.toByteArray(),
                islandsTableStream.size() / ISLANDS_TABLE_ENTRY_SIZE);
    }

    public MemoryIslandsCache(IslandsContainer islandsContainer, byte[] islandsTableBytes, byte[] islandsDataBytes,
                              int islandsTableElementsCount) {
        super(islandsContainer, islandsTableBytes, islandsTableElementsCount);
        this.islandsDataBytes = islandsDataBytes;
    }

    @Override
    public <T extends IslandBase> T loadIsland(IslandBase islandBase, IslandLoadLevel<T> loadLevel) {
        int islandsTableIndex = getIslandIndex(islandBase.getUniqueId());

        if (islandsTableIndex == -1) {
            throw new IllegalStateException("Cannot find island " + islandBase.getUniqueId() + " in cache.");
        }

        int islandsDataIndex = this.islandsTableBytes[islandsTableIndex + ISLANDS_TABLE_UUID_SIZE];

        Island island = deserializeIsland(islandBase, new ByteArrayDataInput(this.islandsDataBytes, islandsDataIndex));

        // Load the new island to the container.
        this.islandsContainer.addIsland(island);

        return loadLevel.getIslandType().cast(island);
    }

    public int getBytesLength() {
        return this.islandsDataBytes.length;
    }

    public FileIslandsCache toFileCache(SuperiorSkyblockPlugin plugin) throws IOException {
        return new FileIslandsCache(plugin, this.islandsContainer, this.islandsTableBytes,
                this.islandsDataBytes, this.islandsTableElementsCount);
    }

}
