package com.bgsoftware.superiorskyblock.island.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import com.bgsoftware.superiorskyblock.core.ByteArrayDataInput;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileIslandsCache extends IslandsCache {

    private final RandomAccessFile cacheFile;

    public FileIslandsCache(SuperiorSkyblockPlugin plugin, IslandsContainer islandsContainer, byte[] islandsTableBytes,
                            byte[] islandsDataBytes, int islandsTableElementsCount) throws IOException {
        super(islandsContainer, islandsTableBytes, islandsTableElementsCount);
        File cacheFile = new File(plugin.getDataFolder(), "datastore/.cache");

        if (!cacheFile.exists()) {
            cacheFile.getParentFile().mkdirs();
            cacheFile.createNewFile();
        }

        try (FileWriter writer = new FileWriter(cacheFile)) {
            writer.write(new String(islandsDataBytes));
        }

        this.cacheFile = new RandomAccessFile(cacheFile, "r");
    }

    @Override
    public <T extends IslandBase> T loadIsland(IslandBase islandBase, IslandLoadLevel<T> loadLevel) {
        int islandsTableIndex = getIslandIndex(islandBase.getUniqueId());

        if (islandsTableIndex == -1) {
            throw new IllegalStateException("Cannot find island " + islandBase.getUniqueId() + " in cache.");
        }

        Island island;

        try {
            int islandsDataIndex = this.islandsTableBytes[islandsTableIndex + ISLANDS_TABLE_UUID_SIZE];
            this.cacheFile.seek(islandsDataIndex);
            island = deserializeIsland(islandBase, new ByteArrayDataInput(cacheFile));
        } catch (IOException error) {
            throw new RuntimeException(error);
        }

        // Load the new island to the container.
        this.islandsContainer.addIsland(island);

        return loadLevel.getIslandType().cast(island);
    }

}
