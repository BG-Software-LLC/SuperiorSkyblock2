package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.island.container.IslandsContainer;
import com.bgsoftware.superiorskyblock.api.island.level.IslandLoadLevel;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public abstract class IslandsContainerAccess {

    private final IslandsContainer islandsContainer;

    protected IslandsContainerAccess(IslandsContainer islandsContainer) {
        this.islandsContainer = islandsContainer;
    }

    public void addIsland(IslandBase island) {
        this.islandsContainer.addIsland(island);
    }

    public void removeIsland(IslandBase island) {
        this.islandsContainer.removeIsland(island);
    }

    @Nullable
    public <T extends IslandBase> T getIslandByUUID(UUID uuid, IslandLoadLevel<T> loadLevel) {
        IslandBase islandBase = this.islandsContainer.getBaseIslandByUUID(uuid);
        return islandBase == null ? null : this.loadIsland(islandBase, loadLevel);
    }

    @Nullable
    public <T extends IslandBase> T getIslandAtPosition(int position, SortingType sortingType, IslandLoadLevel<T> loadLevel) {
        IslandBase islandBase = this.islandsContainer.getBaseIslandAtPosition(position, sortingType);
        return islandBase == null ? null : this.loadIsland(islandBase, loadLevel);
    }

    public int getIslandPosition(IslandBase island, SortingType sortingType) {
        return this.islandsContainer.getIslandPosition(island, sortingType);
    }

    public int getIslandsAmount() {
        return this.islandsContainer.getIslandsAmount();
    }

    @Nullable
    public <T extends IslandBase> T getIslandAt(Location location, IslandLoadLevel<T> loadLevel) {
        IslandBase islandBase = this.islandsContainer.getBaseIslandAt(location);
        return islandBase == null ? null : this.loadIsland(islandBase, loadLevel);
    }

    public void sortIslands(SortingType sortingType, @Nullable Runnable onFinish) {
        this.islandsContainer.sortIslands(sortingType, onFinish);
    }

    public void sortIslands(SortingType sortingType, boolean forceSort, @Nullable Runnable onFinish) {
        this.islandsContainer.sortIslands(sortingType, forceSort, onFinish);
    }

    public void notifyChange(SortingType sortingType, IslandBase island) {
        this.islandsContainer.notifyChange(sortingType, island);
    }

    @Deprecated
    public List<Island> getSortedIslands(SortingType sortingType) {
        return this.islandsContainer.getSortedIslands(sortingType);
    }

    public List<IslandBase> getSortedBaseIslands(SortingType sortingType) {
        return this.islandsContainer.getSortedBaseIslands(sortingType);
    }

    @Deprecated
    public List<Island> getIslandsUnsorted() {
        return this.islandsContainer.getIslandsUnsorted();
    }

    public List<IslandBase> getBaseIslandsUnsorted() {
        return this.islandsContainer.getBaseIslandsUnsorted();
    }

    public void addSortingType(SortingType sortingType, boolean sort) {
        this.islandsContainer.addSortingType(sortingType, sort);
    }

    public IslandsContainer getContainer() {
        return islandsContainer;
    }

    protected abstract <T extends IslandBase> T loadIsland(IslandBase islandBase, IslandLoadLevel<T> loadLevel);

}
