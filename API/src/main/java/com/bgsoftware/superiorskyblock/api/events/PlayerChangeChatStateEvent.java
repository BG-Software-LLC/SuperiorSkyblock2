package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * PlayerChangeChatStateEvent is called when a player has his chat state changed.
 */
public class PlayerChangeChatStateEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer superiorPlayer;
    private final ChatState newChatState;

    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param superiorPlayer The player that had his chat state changed.
     * @param newChatState   The new chat state of the player.
     */
    public PlayerChangeChatStateEvent(SuperiorPlayer superiorPlayer, ChatState newChatState) {
        super(!Bukkit.isPrimaryThread());
        this.superiorPlayer = superiorPlayer;
        this.newChatState = newChatState;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Get the player that had his chat state changed.
     */
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    /**
     * Get the new chat state of the player.
     */
    public ChatState getNewChatState() {
        return newChatState;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

}
