package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.block.Biome;
import org.bukkit.event.Cancellable;

/**
 * IslandBiomeChangeEvent is called when the biome of the island is changed.
 */
public class IslandBiomeChangeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private final Dimension dimension;
    private Biome biome;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who changed the biome of the island.
     * @param island         The island object that was changed.
     * @param dimension      The dimension in which biome was changed.
     * @param biome          The new biome of the island.
     */
    public IslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Dimension dimension, Biome biome) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.dimension = dimension;
        this.biome = biome;
    }

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who changed the biome of the island.
     * @param island         The island object that was changed.
     * @param biome          The name of the new biome.
     * @deprecated See {@link #IslandBiomeChangeEvent(SuperiorPlayer, Island, Dimension, Biome)}
     */
    @Deprecated
    public IslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome) {
        this(superiorPlayer, island, SuperiorSkyblockAPI.getSettings().getWorlds().getDefaultWorldDimension(), biome);
    }

    /**
     * Get the player who changed the biome of the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new biome.
     */
    public Biome getBiome() {
        return biome;
    }

    /**
     * Get the dimension in which biome was changed.
     */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Set the new biome.
     */
    public void setBiome(Biome biome) {
        this.biome = biome;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

}
