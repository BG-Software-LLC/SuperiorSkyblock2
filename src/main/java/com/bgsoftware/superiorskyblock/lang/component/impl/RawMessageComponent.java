package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;

import javax.annotation.Nullable;

public final class RawMessageComponent implements IMessageComponent {

    private final String message;

    public static IMessageComponent of(@Nullable String message) {
        return StringUtils.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
    }

    private RawMessageComponent(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        Message.replaceArgs(this.message, args).ifPresent(sender::sendMessage);
    }

}
