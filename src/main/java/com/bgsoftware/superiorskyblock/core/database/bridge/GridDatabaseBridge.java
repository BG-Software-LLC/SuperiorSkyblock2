package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.database.DBColumn;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;

import java.util.function.Consumer;

public class GridDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GridDatabaseBridge() {
    }

    public static void saveLastIsland(GridManager gridManager, BlockPosition lastIsland) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue("last_island", Serializers.BLOCK_POSITION_SERIALIZER.serialize(lastIsland));
                databaseBridge.updateObject("grid", null, column);
            }
        });
    }

    public static void updateVersion(GridManager gridManager, int version) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge -> {
            databaseBridge.deleteObject("ssb_metadata", null);
            try (ObjectsPools.Wrapper<DBColumn> wrapper = ObjectsPools.DB_COLUMN.obtain()) {
                DBColumn column = wrapper.getHandle().withNameAndValue("version", version);
                databaseBridge.insertObject("ssb_metadata", column);
            }
        });
    }

    public static void insertGrid(GridManager gridManager) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge -> {
            try (ObjectsPools.Batch<DBColumn> pool = ObjectsPools.DB_COLUMN_BATCH.obtain()) {
                databaseBridge.insertObject("grid",
                        pool.obtain().withNameAndValue("last_island", Serializers.BLOCK_POSITION_SERIALIZER.serialize(gridManager.getLastIslandPosition())),
                        pool.obtain().withNameAndValue("max_island_size", plugin.getSettings().getMaxIslandSize()),
                        pool.obtain().withNameAndValue("world", plugin.getSettings().getWorlds().getDefaultWorldName())
                );
            }
        });
    }

    public static void deleteGrid(GridManager gridManager) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge ->
                databaseBridge.deleteObject("grid", null));
    }

    private static void runOperationIfRunning(DatabaseBridge databaseBridge, Consumer<DatabaseBridge> databaseBridgeConsumer) {
        if (databaseBridge.getDatabaseBridgeMode() == DatabaseBridgeMode.SAVE_DATA)
            databaseBridgeConsumer.accept(databaseBridge);
    }

}
