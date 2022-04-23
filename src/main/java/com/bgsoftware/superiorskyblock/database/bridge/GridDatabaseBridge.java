package com.bgsoftware.superiorskyblock.database.bridge;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.serialization.Serializers;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

@SuppressWarnings("unchecked")
public final class GridDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GridDatabaseBridge() {
    }

    public static void saveLastIsland(GridManager gridManager, SBlockPosition lastIsland) {
        gridManager.getDatabaseBridge().updateObject("grid", null, new Pair<>("last_island", lastIsland.toString()));
    }

    public static void insertGrid(GridManager gridManager) {
        gridManager.getDatabaseBridge().insertObject("grid",
                new Pair<>("last_island", Serializers.LOCATION_SPACED_SERIALIZER.serialize(gridManager.getLastIslandLocation())),
                new Pair<>("max_island_size", plugin.getSettings().getMaxIslandSize()),
                new Pair<>("world", plugin.getSettings().getWorlds().getDefaultWorldName())
        );
    }

    public static void deleteGrid(GridManager gridManager) {
        gridManager.getDatabaseBridge().deleteObject("grid", null);
    }

}
