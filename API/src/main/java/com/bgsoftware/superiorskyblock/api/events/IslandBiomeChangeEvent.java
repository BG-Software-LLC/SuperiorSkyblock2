package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.block.Biome;
import org.bukkit.event.Cancellable;

/**
 * IslandCreateEvent is called when a new island is created.
 */
public class IslandBiomeChangeEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private Biome biome;
    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param superiorPlayer The player who changed the biome of the island.
     * @param island         The island object that was changed.
     * @param biome          The name of the new biome.
     */
    public IslandBiomeChangeEvent(SuperiorPlayer superiorPlayer, Island island, Biome biome) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.biome = biome;
    }

    /**
     * Get the player who upgraded the island.
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
