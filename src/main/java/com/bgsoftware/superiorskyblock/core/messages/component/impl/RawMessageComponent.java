package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;

public class RawMessageComponent implements IMessageComponent {

    private final String message;

    public static IMessageComponent of(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
    }

    private RawMessageComponent(String message) {
        this.message = message;
    }

    @Override
    public Type getType() {
        return Type.RAW_MESSAGE;
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
