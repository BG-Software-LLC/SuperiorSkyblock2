package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessageProvider;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.service.message.SpigotMessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RawMessageComponent implements IMessageComponent {

    private final String message;
    private MessageProvider messageProvider;

    private RawMessageComponent(String message) {
        this.message = message;
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            this.messageProvider = (MessageProvider) Class.forName("com.bgsoftware.superiorskyblock.external.minimessage.MiniMessageProvider").getConstructor().newInstance();
        } catch (Exception ignored) {
            this.messageProvider = new SpigotMessageProvider();
        }
    }

    public static IMessageComponent of(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
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
        return this.content.getContent().orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.content.getContent(args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        Message.replaceArgs(this.message, args).ifPresent(message -> {
            if (sender instanceof Player && this.messageProvider != null) {
                this.messageProvider.sendMessage((Player) sender, message);
            } else sender.sendMessage(message);
        });
        this.content.getContent(args).ifPresent(sender::sendMessage);
    }

}
