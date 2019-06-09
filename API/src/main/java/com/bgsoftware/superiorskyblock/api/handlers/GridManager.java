package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface GridManager {

    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome);

    void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus);

    void createIsland(SuperiorPlayer superiorPlayer, String schemName);

    void deleteIsland(Island island);

    Island getIsland(SuperiorPlayer superiorPlayer);

    Island getIsland(int index);

    Island getIsland(UUID uuid);

    Island getIslandAt(Location location);

    Island getSpawnIsland();

    World getIslandsWorld();

    Location getNextLocation();

    List<UUID> getAllIslands();

    int getBlockValue(Key key);

    int getBlockAmount(Block block);

    int getBlockAmount(Location location);

    void setBlockAmount(Block block, int amount);

    void calcAllIslands();
}
