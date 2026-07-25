package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.handlers.StackedBlocksManager;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.database.DBColumn;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.core.stackedblocks.StackedBlock;

import java.util.function.Consumer;

public class StackedBlocksDatabaseBridge {

    private StackedBlocksDatabaseBridge() {
    }

    public static void saveStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        runOperationIfRunning(stackedBlocks.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("stacked_blocks",
                        pool.obtain().withNameAndValue("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation())),
                        pool.obtain().withNameAndValue("amount", stackedBlock.getAmount()),
                        pool.obtain().withNameAndValue("block_type", stackedBlock.getBlockKey().toString())
                );
            }
        });
    }

    public static void deleteStackedBlock(StackedBlocksManager stackedBlocks, StackedBlock stackedBlock) {
        runOperationIfRunning(stackedBlocks.getDatabaseBridge(), databaseBridge -> databaseBridge.deleteObject("stacked_blocks",
                DatabaseFilter.fromFilter("location", Serializers.LOCATION_SPACED_SERIALIZER.serialize(stackedBlock.getLocation()))));
    }

    private static void runOperationIfRunning(DatabaseBridge databaseBridge, Consumer<DatabaseBridge> databaseBridgeConsumer) {
        if (databaseBridge.getDatabaseBridgeMode() == DatabaseBridgeMode.SAVE_DATA)
            databaseBridgeConsumer.accept(databaseBridge);
    }

}
