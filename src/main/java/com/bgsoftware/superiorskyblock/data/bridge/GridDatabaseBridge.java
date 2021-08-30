package com.bgsoftware.superiorskyblock.data.bridge;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.data.DatabaseFilter;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.handlers.StackedBlocksHandler;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

import java.util.Arrays;

@SuppressWarnings("unchecked")
public final class GridDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GridDatabaseBridge(){
    }

    public static void saveLastIsland(GridManager gridManager, SBlockPosition lastIsland){
        gridManager.getDatabaseBridge().updateObject("grid", null, new Pair<>("last_island", lastIsland.toString()));
    }

    public static void saveStackedBlock(GridManager gridManager, StackedBlocksHandler.StackedBlock stackedBlock){
        SBlockPosition position = stackedBlock.getBlockPosition();
        gridManager.getDatabaseBridge().insertObject("stacked_blocks",
                new Pair<>("location", position.toString()),
                new Pair<>("amount", stackedBlock.getAmount()),
                new Pair<>("block_type", stackedBlock.getBlockKey().toString())
        );
    }

    public static void deleteStackedBlock(GridManager gridManager, StackedBlocksHandler.StackedBlock stackedBlock){
        gridManager.getDatabaseBridge().deleteObject("stacked_blocks",
                createFilter(new Pair<>("location", stackedBlock.getBlockPosition().toString())));
    }

    public static void deleteStackedBlocks(GridManager gridManager){
        gridManager.getDatabaseBridge().deleteObject("stacked_blocks", null);
    }

    public static void insertGrid(GridManager gridManager){
        gridManager.getDatabaseBridge().insertObject("grid",
                new Pair<>("last_island", SBlockPosition.of(gridManager.getLastIslandLocation()).toString()),
                new Pair<>("max_island_size", plugin.getSettings().maxIslandSize),
                new Pair<>("world", plugin.getSettings().islandWorldName)
        );
    }

    public static void deleteGrid(GridManager gridManager){
        gridManager.getDatabaseBridge().deleteObject("grid", null);
    }

    private static DatabaseFilter createFilter(Pair<String, Object>... others){
        return new DatabaseFilter(Arrays.asList(others));
    }

}
