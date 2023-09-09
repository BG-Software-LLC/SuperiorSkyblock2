package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;
import org.bukkit.potion.PotionEffectType;

/**
 * IslandChangeEffectLevelEvent is called when an effect of an island is changed.
 */
public class IslandChangeEffectLevelEvent extends IslandEvent implements Cancellable {

    @Nullable
    private final SuperiorPlayer superiorPlayer;
    private final PotionEffectType effectType;

    private int effectLevel;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the effect level of an island.
     *                       If set to null, it means the level was changed via the console.
     * @param island         The island that the effect level was changed for.
     * @param effectType     The effect that the level was changed for.
     * @param effectLevel    The new level of the effect.
     */
    public IslandChangeEffectLevelEvent(@Nullable SuperiorPlayer superiorPlayer, Island island, PotionEffectType effectType, int effectLevel) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.effectType = effectType;
        this.effectLevel = effectLevel;
    }

    /**
     * Get the player that changed the effect level.
     * If null, it means the level was changed by console.
     */
    @Nullable
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the effect that the level was changed for.
     */
    public PotionEffectType getEffectType() {
        return effectType;
    }

    /**
     * Get the new level of the effect.
     */
    public int getEffectLevel() {
        return effectLevel;
    }

    /**
     * Set the new level of the effect.
     *
     * @param effectLevel The new effect level to set.
     */
    public void setEffectLevel(int effectLevel) {
        Preconditions.checkArgument(effectLevel >= 0, "Cannot set the effect level to a negative level.");
        this.effectLevel = effectLevel;
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
