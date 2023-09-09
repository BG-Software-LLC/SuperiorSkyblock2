package com.bgsoftware.superiorskyblock.api.island.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public class DelegateIslandsContainer implements IslandsContainer {

    protected final IslandsContainer handle;

    protected DelegateIslandsContainer(IslandsContainer handle) {
        this.handle = handle;
    }

    @Override
    public void addIsland(Island island) {
        this.handle.addIsland(island);
    }

    @Override
    public void removeIsland(Island island) {
        this.handle.removeIsland(island);
    }

    @Nullable
    @Override
    public Island getIslandByUUID(UUID uuid) {
        return this.handle.getIslandByUUID(uuid);
    }

    @Nullable
    @Override
    @Deprecated
    public Island getIslandByLeader(UUID uuid) {
        return this.handle.getIslandByLeader(uuid);
    }

    @Nullable
    @Override
    public Island getIslandAtPosition(int position, SortingType sortingType) {
        return this.handle.getIslandAtPosition(position, sortingType);
    }

    @Override
    public int getIslandPosition(Island island, SortingType sortingType) {
        return this.handle.getIslandPosition(island, sortingType);
    }

    @Override
    public int getIslandsAmount() {
        return this.handle.getIslandsAmount();
    }

    @Nullable
    @Override
    public Island getIslandAt(Location location) {
        return this.handle.getIslandAt(location);
    }

    @Override
    @Deprecated
    public void transferIsland(UUID oldLeader, UUID newLeader) {
        this.handle.transferIsland(oldLeader, newLeader);
    }

    @Override
    public void sortIslands(SortingType sortingType, @Nullable Runnable onFinish) {
        this.handle.sortIslands(sortingType, onFinish);
    }

    @Override
    public void sortIslands(SortingType sortingType, boolean forceSort, @Nullable Runnable onFinish) {
        this.handle.sortIslands(sortingType, forceSort, onFinish);
    }

    @Override
    public void notifyChange(SortingType sortingType, Island island) {
        this.handle.notifyChange(sortingType, island);
    }

    @Override
    public List<Island> getSortedIslands(SortingType sortingType) {
        return this.handle.getSortedIslands(sortingType);
    }

    @Override
    public List<Island> getIslandsUnsorted() {
        return this.handle.getIslandsUnsorted();
    }

    @Override
    public void addSortingType(SortingType sortingType, boolean sort) {
        this.handle.addSortingType(sortingType, sort);
    }

}
