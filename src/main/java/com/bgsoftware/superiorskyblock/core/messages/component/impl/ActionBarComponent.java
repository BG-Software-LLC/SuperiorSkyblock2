package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionBarComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final MessageContent content;

    public static IMessageComponent of(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new ActionBarComponent(message);
    }

    private ActionBarComponent(String content) {
        this.content = MessageContent.parse(content);
    }

    @Override
    public Type getType() {
        return Type.ACTION_BAR;
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
        this.content.getContent((Player) sender, args).ifPresent(message ->
                plugin.getNMSPlayers().sendActionBar((Player) sender, message));
    }

}
