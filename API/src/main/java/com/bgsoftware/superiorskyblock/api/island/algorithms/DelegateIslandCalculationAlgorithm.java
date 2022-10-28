package com.bgsoftware.superiorskyblock.api.island.algorithms;

import com.bgsoftware.superiorskyblock.api.island.Island;

import java.util.concurrent.CompletableFuture;

public class DelegateIslandCalculationAlgorithm implements IslandCalculationAlgorithm {

    protected final IslandCalculationAlgorithm handle;

    protected DelegateIslandCalculationAlgorithm(IslandCalculationAlgorithm handle) {
        this.handle = handle;
    }

    @Override
    @Deprecated
    public CompletableFuture<IslandCalculationResult> calculateIsland() {
        return this.handle.calculateIsland();
    }

    @Override
    public CompletableFuture<IslandCalculationResult> calculateIsland(Island island) {
        return this.handle.calculateIsland(island);
    }

}
