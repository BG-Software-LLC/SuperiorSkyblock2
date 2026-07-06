package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.superiorskyblock.api.hooks.MessagesProvider;
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
    public IMessageComponent createBossBarComponent(String name, String color, String style, int duration) {
        BossBar.Color bossBarColor;
        try {
            bossBarColor = BossBar.Color.valueOf(color);
        } catch (Exception error) {
            bossBarColor = BossBar.Color.PINK;
        }

        BossBar.Style bossBarStyle;
        try {
            bossBarStyle = BossBar.Style.valueOf(style);
        } catch (Exception error) {
            bossBarStyle = BossBar.Style.SOLID;
        }

        return BossBarComponent.of(Formatters.COLOR_FORMATTER.format(name), bossBarColor, bossBarStyle, duration);
    }

    @Override
    public IMessageComponent createRawMessageComponent(String message) {
        return RawMessageComponent.of(Formatters.COLOR_FORMATTER.format(message));
    }

    @Override
    public IMessageComponent createTitleComponent(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(Formatters.COLOR_FORMATTER.format(title),
                Formatters.COLOR_FORMATTER.format(subtitle), fadeIn, stay, fadeOut);
    }

}
