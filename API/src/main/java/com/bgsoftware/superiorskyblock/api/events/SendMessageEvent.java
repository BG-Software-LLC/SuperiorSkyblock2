package com.bgsoftware.superiorskyblock.api.events;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * SendMessageEvent is called when a message is sent to {@link CommandSender}
 */
public class SendMessageEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final CommandSender receiver;
    private final String messageType;
    private final Object[] args;
    private IMessageComponent messageComponent;

    private boolean cancelled = false;

    /**
     * The constructor for the event.
     *
     * @param receiver         The receiver of the message.
     * @param messageType      The name of the message, similar to the one from lang file.
     * @param messageComponent The message component that is being sent.
     */
    public SendMessageEvent(CommandSender receiver, String messageType, IMessageComponent messageComponent, Object[] args) {
        super(!Bukkit.isPrimaryThread());
        this.receiver = receiver;
        this.messageType = messageType;
        this.messageComponent = messageComponent;
        this.args = args;
    }

    /**
     * Get the receiver of the message.
     */
    public CommandSender getReceiver() {
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

    /**
     * Get the message component that is sent.
     */
    public IMessageComponent getMessageComponent() {
        return messageComponent;
    }

    /**
     * Set the message component to be sent.
     *
     * @param messageComponent The new component to be sent.
     */
    public void setMessageComponent(IMessageComponent messageComponent) {
        Preconditions.checkNotNull(messageComponent, "Message components cannot be null.");
        this.messageComponent = messageComponent;
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
