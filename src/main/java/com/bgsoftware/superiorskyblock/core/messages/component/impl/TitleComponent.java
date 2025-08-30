
package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitleComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final MessageContent titleMessage;
    private final MessageContent subtitleMessage;
    private final int fadeIn;
    private final int duration;
    private final int fadeOut;

    public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                       int fadeIn, int duration, int fadeOut) {
        return duration <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
                EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, duration, fadeOut);
    }

    private TitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage, int fadeIn, int duration, int fadeOut) {
        this.titleMessage = Text.isBlank(titleMessage) ? MessageContent.EMPTY : MessageContent.parse(titleMessage);
        this.subtitleMessage = Text.isBlank(subtitleMessage) ? MessageContent.EMPTY : MessageContent.parse(subtitleMessage);
        this.fadeIn = fadeIn;
        this.duration = duration;
        this.fadeOut = fadeOut;
    }

    @Override
    public Type getType() {
        return Type.TITLE;
    }

    @Override
    public String getMessage() {
        return this.titleMessage.getContent(null).orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.titleMessage.getContent(null, args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        String titleMessage = this.titleMessage.getContent((Player) sender, args).orElse(null);
        String subtitleMessage = this.subtitleMessage.getContent((Player) sender, args).orElse(null);
        if (titleMessage != null && subtitleMessage != null)
            plugin.getNMSPlayers().sendTitle((Player) sender, titleMessage, subtitleMessage,
                    this.fadeIn, this.duration, this.fadeOut);
    }

}
