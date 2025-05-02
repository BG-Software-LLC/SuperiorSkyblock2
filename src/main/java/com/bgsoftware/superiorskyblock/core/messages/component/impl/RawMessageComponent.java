package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RawMessageComponent implements IMessageComponent {

    private final MessageContent content;

    public static IMessageComponent of(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
    }

    private RawMessageComponent(String message) {
        this.content = MessageContent.parse(message);
    }

    @Override
    public Type getType() {
        return Type.RAW_MESSAGE;
    }

    @Override
    public String getMessage() {
        return this.content.getContent(null).orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.content.getContent(null, args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        Player player = sender instanceof Player ? (Player) sender : null;
        this.content.getContent(player, args).ifPresent(sender::sendMessage);
    }

}
