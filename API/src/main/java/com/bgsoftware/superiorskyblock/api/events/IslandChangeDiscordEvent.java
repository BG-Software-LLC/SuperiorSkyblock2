package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChangeDiscordEvent is called when the discord of the island is changed.
 */
public class IslandChangeDiscordEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;

    private String discord;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that changed the discord of the island.
     * @param island         The island that the discord was changed for.
     * @param discord        The new discord of the island
     */
    public IslandChangeDiscordEvent(SuperiorPlayer superiorPlayer, Island island, String discord) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.discord = discord;
    }

    /**
     * Get the player that changed the discord of the island.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new discord of the island.
     */
    public String getDiscord() {
        return discord;
    }

    /**
     * Set the new discord for the island.
     *
     * @param discord The new discord to set.
     */
    public void setDiscord(String discord) {
        Preconditions.checkNotNull(discord, "Cannot set the discord of the island to null.");
        this.discord = discord;
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
