
package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class TitleComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String titleMessage;
    private final String subtitleMessage;
    private final int fadeIn;
    private final int duration;
    private final int fadeOut;

    public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                       int fadeIn, int duration, int fadeOut) {
        return duration <= 0 || (StringUtils.isBlank(titleMessage) && StringUtils.isBlank(subtitleMessage)) ?
                EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, duration, fadeOut);
    }

    private TitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int duration, int fadeOut) {
        this.titleMessage = titleMessage;
        this.subtitleMessage = subtitleMessage;
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
        return this.titleMessage;
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        String titleMessage = Message.replaceArgs(this.titleMessage, args).orElse(null);
        String subtitleMessage = Message.replaceArgs(this.subtitleMessage, args).orElse(null);

        if (titleMessage != null || subtitleMessage != null) {
            plugin.getNMSPlayers().sendTitle((Player) sender, titleMessage, subtitleMessage,
                    this.fadeIn, this.duration, this.fadeOut);
        }
    }

}
