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
    private final MessageProvider messageProvider;

    public static IMessageComponent of(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
    }

    private RawMessageComponent(String message) {
        this.content = MessageContent.parse(message);
        MessageProvider messageProviderLocal;
        try {
            Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            messageProviderLocal = (MessageProvider) Class.forName("com.bgsoftware.superiorskyblock.external.minimessage.MiniMessageProvider").getConstructor().newInstance();
        } catch (Exception ignored) {
            messageProviderLocal = new SpigotMessageProvider();
        }
        this.messageProvider = messageProviderLocal;
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
    public void sendMessage(CommandSender sender, Object... args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            this.content.getContent(args).ifPresent(string -> messageProvider.sendMessage(player, string));
        } else {
            this.content.getContent(args).ifPresent(sender::sendMessage);
        }
    }
}