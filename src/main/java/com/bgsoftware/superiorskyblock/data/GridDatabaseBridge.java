package com.bgsoftware.superiorskyblock.data;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.handlers.StackedBlocksHandler;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;

@SuppressWarnings("unchecked")
public final class GridDatabaseBridge {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GridDatabaseBridge(){
    }

    public static void saveLastIsland(GridManager gridManager, SBlockPosition lastIsland){
        gridManager.getDatabaseBridge().updateObject("grid", new Pair<>("lastIsland", lastIsland.toString()));
    }

    public static void saveStackedBlock(GridManager gridManager, StackedBlocksHandler.StackedBlock stackedBlock){
        SBlockPosition position = stackedBlock.getBlockPosition();
        gridManager.getDatabaseBridge().insertObject("stackedBlocks",
                new Pair<>("world", position.getWorldName()),
                new Pair<>("x", position.getX()),
                new Pair<>("y", position.getY()),
                new Pair<>("z", position.getZ()),
                new Pair<>("amount", stackedBlock.getAmount()),
                new Pair<>("item", stackedBlock.getBlockKey())
        );
    }

    public static void deleteStackedBlock(GridManager gridManager, StackedBlocksHandler.StackedBlock stackedBlock){
        gridManager.getDatabaseBridge().deleteObject("stacked_blocks");
        // TODO
    }

    public static void deleteStackedBlocks(GridManager gridManager){
        gridManager.getDatabaseBridge().deleteObject("stacked_blocks");
    }

    public static void insertGrid(GridManager gridManager){
        gridManager.getDatabaseBridge().insertObject("grid",
                new Pair<>("lastIsland", SBlockPosition.of(gridManager.getLastIslandLocation()).toString()),
                new Pair<>("stackedBlocks", ""),
                new Pair<>("maxIslandSize", plugin.getSettings().maxIslandSize),
                new Pair<>("world", plugin.getSettings().islandWorldName),
                new Pair<>("dirtyChunks", "")
        );
    }

    public static void deleteGrid(GridManager gridManager){
        gridManager.getDatabaseBridge().deleteObject("grid");
    }


}
