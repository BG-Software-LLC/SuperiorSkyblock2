package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerChangeNameEvent is called when a player has his name changed.
 */
public class PlayerChangeNameEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final String newName;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that had his name changed.
     * @param newName        The new name of the player.
     */
    public PlayerChangeNameEvent(SuperiorPlayer superiorPlayer, String newName) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.newName = newName;
    }

    /**
     * Get the player that had his name changed.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new name of the player.
     */
    public String getNewName() {
        return newName;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
