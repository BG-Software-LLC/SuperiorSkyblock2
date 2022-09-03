package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * AttemptPlayerSendMessageEvent is called when a message is attempted to be sent to {@link SuperiorPlayer}
 * The event is called before it checks if {@link SuperiorPlayer} can get the message.
 * In this time there is no {@link IMessageComponent} yet, as it is created only once the message can be sent
 * to the receiver.
 */
public class AttemptPlayerSendMessageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final SuperiorPlayer receiver;
    private final String messageType;
    private final Object[] args;

    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param receiver    The receiver of the message.
     * @param messageType The name of the message, similar to the one from lang file.
     */
    public AttemptPlayerSendMessageEvent(SuperiorPlayer receiver, String messageType, Object[] args) {
        super(!Bukkit.isPrimaryThread());
        this.receiver = receiver;
        this.messageType = messageType;
        this.args = args;
    }

    /**
     * Get the receiver of the message.
     */
    public SuperiorPlayer getReceiver() {
        return receiver;
    }

    /**
     * Get the name of the message, similar to the one from lang file.
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * Get an argument of the message.
     *
     * @param index The index of the argument.
     * @throws ArrayIndexOutOfBoundsException If {@param index} is out of bounds.
     */
    public Object getArgument(int index) {
        return args[index];
    }

    /**
     * Set an argument for the message.
     *
     * @param index The index of the argument.
     * @param value The value to be set.
     * @throws ArrayIndexOutOfBoundsException If {@param index} is out of bounds.
     */
    public void setArgument(int index, Object value) {
        Preconditions.checkNotNull(value, "Argument's value cannot be null.");
        args[index] = value;
    }

    /**
     * Get the length of the arguments array.
     */
    public int getArgumentsLength() {
        return args.length;
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

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
