package com.bgsoftware.superiorskyblock.database.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.serialization.Serializers;
import com.bgsoftware.superiorskyblock.world.blocks.stacked.StackedBlock;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public final class StackedBlocksDatabaseBridge {

    public static void saveStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        stackedBlocks.getDatabaseBridge().insertObject("stacked_blocks",
                new Pair<>("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation())),
                new Pair<>("amount", stackedBlock.getAmount()),
                new Pair<>("block_type", stackedBlock.getBlockKey().toString())
        );
    }

    public static void deleteStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        stackedBlocks.getDatabaseBridge().deleteObject("stacked_blocks",
                createFilter(new Pair<>("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation()))));
    }

    public static void deleteStackedBlocks(StackedBlocksManager stackedBlocks) {
        stackedBlocks.getDatabaseBridge().deleteObject("stacked_blocks", null);
    }

    private static DatabaseFilter createFilter(Pair<String, Object>... others) {
        return new DatabaseFilter(Arrays.asList(others));
    }

}
