package com.bgsoftware.superiorskyblock.api.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import org.bukkit.block.Biome;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Configuration for creation of new islands.
 * The values from this configuration are used in {@link GridManager#createIsland}
 */
public interface MenuIslandCreationConfig {

    /**
     * Get the schematic that will be used to create the island.
     */
    Schematic getSchematic();

    /**
     * Get the sound to play when the island is successfully created.
     */
    @Nullable
    GameSound getSound();

    /**
     * Get the commands to execute when island is successfully created.
     */
    Collection<String> getCommands();

    /**
     * Get whether the island-values (worth & level) should be offset to 0 when the island is created.
     */
    boolean shouldOffsetIslandValue();

    /**
     * Custom worth bonus when the island is created.
     */
    BigDecimal getBonusWorth();

    /**
     * Custom level bonus when the island is created.
     */
    BigDecimal getBonusLevel();

    /**
     * Get the spawn offset of the island's home location from where the schematic was placed.
     */
    @Nullable
    BlockOffset getSpawnOffset();

    /**
     * Get the biome to set to the new island.
     */
    Biome getBiome();

}
