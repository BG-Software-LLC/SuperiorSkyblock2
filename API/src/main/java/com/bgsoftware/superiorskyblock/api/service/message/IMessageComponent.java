package com.bgsoftware.superiorskyblock.api.service.message;

import org.bukkit.command.CommandSender;

public interface IMessageComponent {

    /**
     * Get the raw message of this component.
     */
    String getMessage();

    /**
     * Send this message to a {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to send the message to.
     * @param args   The arguments of the message.
     */
    void sendMessage(CommandSender sender, Object... args);

}
