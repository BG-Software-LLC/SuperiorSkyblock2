package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;


/**
 * IslandChangeSpawnerRatesEvent is called when the spawner-rates multiplier of the island is changed.
 */
public class IslandChangeSpawnerRatesEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;

    private double spawnerRates;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the spawner-rates multiplier of the island.
     *                       If set to null, it means the spawner-rates multiplier was changed via the console.
     * @param island         The island that the spawner-rates multiplier was changed for.
     * @param spawnerRates   The new spawner-rates multiplier of the island
     */
    public IslandChangeSpawnerRatesEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, double spawnerRates) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.spawnerRates = spawnerRates;
    }

    /**
     * Get the player that changed the spawner-rates multiplier.
     * If null, it means the spawner-rates multiplier was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new spawner-rates multiplier of the island.
     */
    public double getSpawnerRates() {
        return spawnerRates;
    }

    /**
     * Set the new spawner-rates multiplier for the island.
     *
     * @param spawnerRates The spawner-rates multiplier to set.
     */
    public void setSpawnerRates(double spawnerRates) {
        Preconditions.checkArgument(spawnerRates >= 0, "Cannot set the spawner rate to a negative multiplier.");
        this.spawnerRates = spawnerRates;
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
