package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlock;

import java.util.Arrays;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class StackedBlocksDatabaseBridge {

    public static void saveStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        runOperationIfRunning(stackedBlocks.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("stacked_blocks",
                new Pair<>("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation())),
                new Pair<>("amount", stackedBlock.getAmount()),
                new Pair<>("block_type", stackedBlock.getBlockKey().toString())
        ));
    }

    public static void deleteStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        runOperationIfRunning(stackedBlocks.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("stacked_blocks",
                createFilter(new Pair<>("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation())))
        ));
    }

    public static void deleteStackedBlocks(StackedBlocksManager stackedBlocks) {
        runOperationIfRunning(stackedBlocks.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("stacked_blocks", null));
    }

    private static DatabaseFilter createFilter(Pair<String, Object>... others) {
        return DatabaseFilter.fromFilters(Arrays.asList(others));
    }

    private static void runOperationIfRunning(DatabaseBridge databaseBridge, Consumer<DatabaseBridge> databaseBridgeConsumer) {
        if (databaseBridge.getDatabaseBridgeMode() == DatabaseBridgeMode.SAVE_DATA)
            databaseBridgeConsumer.accept(databaseBridge);
    }

}
