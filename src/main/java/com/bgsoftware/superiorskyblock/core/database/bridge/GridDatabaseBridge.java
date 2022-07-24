package com.bgsoftware.superiorskyblock.core.database.bridge;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridge;
import com.bgsoftware.superiorskyblock.api.data.DatabaseBridgeMode;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;

import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public class GridDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GridDatabaseBridge() {
    }

    public static void saveLastIsland(GridManager gridManager, SBlockPosition lastIsland) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge ->
                databaseBridge.updateObject("grid", null, new Pair<>("last_island", lastIsland.toString())));
    }

    public static void insertGrid(GridManager gridManager) {
        runOperationIfRunning(gridManager.getDatabaseBridge(), databaseBridge -> databaseBridge.insertObject("grid",
                new Pair<>("last_island", Serializers.LOCATION_SPACED_SERIALIZER.serialize(gridManager.getLastIslandLocation())),
                new Pair<>("max_island_size", plugin.getSettings().getMaxIslandSize()),
                new Pair<>("world", plugin.getSettings().getWorlds().getDefaultWorldName())
        ));
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
