package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ActionBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.BossBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.RawMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.TitleComponent;

public class MessagesProvider_Default implements MessagesProvider {

    @Override
    public IMessageComponent createActionBarComponent(String message) {
        return ActionBarComponent.of(Formatters.COLOR_FORMATTER.format(message));
    }

    @Override
    public IMessageComponent createBossBarComponent(String message, BossBar.Color color, BossBar.Style style, int duration) {
        return BossBarComponent.of(Formatters.COLOR_FORMATTER.format(message), color, style, duration);
    }

    @Override
    public IMessageComponent createRawMessageComponent(String message) {
        return RawMessageComponent.of(Formatters.COLOR_FORMATTER.format(message));
    }

    @Override
    public IMessageComponent createTitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(Formatters.COLOR_FORMATTER.format(titleMessage),
                Formatters.COLOR_FORMATTER.format(subtitleMessage), fadeIn, stay, fadeOut);
    }

}
