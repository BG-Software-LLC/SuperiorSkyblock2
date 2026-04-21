package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.event.Cancellable;

/**
 * IslandChatEvent is called when a player talks in island chat.
 */
public class IslandChatEvent extends IslandEvent implements Cancellable {

    private final SuperiorPlayer superiorPlayer;
    private String message;
    private final ChatState chatState;
    private boolean cancelled = false;

    /**
     * The constructor of the event.
     *
     * @param island         The island from which the message was sent.
     * @param superiorPlayer The player who sent the message.
     * @param message        The message that was sent.
     * @deprecated See {@link #IslandChatEvent(Island, SuperiorPlayer, String, ChatState)}
     */
    @Deprecated
    public IslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message) {
        this(island, superiorPlayer, message, null);
    }

    /**
     * The constructor of the event.
     *
     * @param island         The island from which the message was sent.
     * @param superiorPlayer The player who sent the message.
     * @param message        The message that was sent.
     * @param chatState      The ChatState in which the message was sent.
     */
    public IslandChatEvent(Island island, SuperiorPlayer superiorPlayer, String message, ChatState chatState) {
        super(island);
        this.superiorPlayer = superiorPlayer;
        this.message = message;
        this.chatState = chatState;
    }

    /**
     * Get the player who sent the message.
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
     * Set a new message that will be sent instead.
     *
     * @param message The new message to send.
     */
    public void setMessage(String message) {
        Preconditions.checkNotNull(message, "message parameter cannot be null.");
        Preconditions.checkArgument(!message.isEmpty(), "message cannot be empty.");
        this.message = message;
    }

    /**
     * Get the ChatState in which the message was sent.
     */
    public ChatState getChatState() {
        return chatState;
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
