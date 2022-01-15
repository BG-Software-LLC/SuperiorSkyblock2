package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChatEvent is called when a player talks in islands chat.
 */
public class IslandChatEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private String message;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island that the player talks in.
     * @param superiorPlayer The player who sent the message.
     * @param message        The message that was sent.
     */
    public IslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.message = message;
    }

    /**
     * Get the player who banned the other player.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the message that the player sent.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set a new message that will be sent.
     *
     * @param message The new message to send.
     */
    public void setMessage(String message) {
        Preconditions.checkNotNull(message, "message parameter cannot be null.");
        Preconditions.checkArgument(!message.isEmpty(), "message cannot be empty.");
        this.message = message;
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
