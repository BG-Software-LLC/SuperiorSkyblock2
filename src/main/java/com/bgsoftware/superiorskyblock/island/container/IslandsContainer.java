package com.bgsoftware.superiorskyblock.island.container;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public interface IslandsContainer {

    void addIsland(Island island);

    void removeIsland(Island island);

    @Nullable
    Island getIslandByUUID(UUID uuid);

    @Nullable
    Island getIslandByOwner(UUID uuid);

    @Nullable
    Island getIslandAtPosition(int position, SortingType sortingType);

    int getIslandPosition(Island island, SortingType sortingType);

    int getIslandsAmount();

    @Nullable
    Island getIslandAt(Location location);

    void transferIsland(UUID oldOwner, UUID newOwner);

    void sortIslands(SortingType sortingType, Runnable onFinish);

    List<Island> getSortedIslands(SortingType sortingType);

    List<Island> getIslandsUnsorted();

    void addSortingType(SortingType sortingType, boolean sort);

}
