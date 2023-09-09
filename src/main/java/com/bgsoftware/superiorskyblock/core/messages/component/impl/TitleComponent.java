
package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TitleComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String titleMessage;
    private final String subtitleMessage;
    private final int fadeIn;
    private final int duration;
    private final int fadeOut;

    public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                       int fadeIn, int duration, int fadeOut) {
        return duration <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
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
