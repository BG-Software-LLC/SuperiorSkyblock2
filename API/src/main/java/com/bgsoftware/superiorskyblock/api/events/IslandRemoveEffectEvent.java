package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.Cancellable;
import org.bukkit.potion.PotionEffectType;

/**
 * IslandRemoveEffectLevelEvent is called when an effect of an island is removed.
 */
public class IslandRemoveEffectEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final PotionEffectType effectType;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that removed the effect level of an island.
     *                       If set to null, it means the effect was removed via the console.
     * @param island         The island that the effect level was removed for.
     * @param effectType     The effect that was removed from the island.
     */
    public IslandRemoveEffectEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, PotionEffectType effectType) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.effectType = effectType;
    }

    /**
     * Get the player that removed the effect level.
     * If null, it means the effect was removed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the effect that was removed from the island.
     */
    public PotionEffectType getEffectType() {
        return effectType;
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
