package com.bgsoftware.superiorskyblock.api.world.algorithm;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DelegateIslandCreationAlgorithm implements IslandCreationAlgorithm {

    protected final IslandCreationAlgorithm handle;

    protected DelegateIslandCreationAlgorithm(IslandCreationAlgorithm handle) {
        this.handle = handle;
    }

    @Override
    @Deprecated
    public CompletableFuture<IslandCreationResult> createIsland(UUID islandUUID, SuperiorPlayer owner,
                                                                BlockPosition lastIsland, String islandName,
                                                                Schematic schematic) {
        return this.handle.createIsland(islandUUID, owner, lastIsland, islandName, schematic);
    }

    @Override
    public CompletableFuture<IslandCreationResult> createIsland(Island.Builder builder, BlockPosition lastIsland) {
        return this.handle.createIsland(builder, lastIsland);
    }

}
