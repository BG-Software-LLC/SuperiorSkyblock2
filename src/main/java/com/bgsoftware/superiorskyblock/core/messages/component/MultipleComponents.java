package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;

public class MultipleComponents implements IMessageComponent {

    private static final LazyReference<MessagesService> messagesService = new LazyReference<MessagesService>() {
        @Override
        protected MessagesService create() {
            return SuperiorSkyblockPlugin.getPlugin().getServices().getService(MessagesService.class);
        }
    };

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section) {
        MessagesService.Builder builder = messagesService.get().newBuilder();

        for (String key : section.getKeys(false)) {
            switch (key) {
                case "action-bar": {
                    String text = section.getString(key + ".text");

                    builder.addActionBar(Formatters.COLOR_FORMATTER.format(text));
                    break;
                }
                case "bossbar": {
                    String message = section.getString(key + ".message");
                    String color = section.getString(key + ".color", "PINK").toUpperCase(Locale.ENGLISH);
                    String style = section.getString(key + ".style", "PROGRESS").toUpperCase(Locale.ENGLISH);
                    int ticks = section.getInt(key + ".ticks");

                    BossBar.Color bossBarColor = EnumHelper.getEnum(BossBar.Color.class, color);
                    if (bossBarColor == null) {
                        bossBarColor = BossBar.Color.PINK;
                    }

                    BossBar.Style bossBarStyle = EnumHelper.getEnum(BossBar.Style.class, style);
                    if (bossBarStyle == null) {
                        bossBarStyle = BossBar.Style.SOLID;
                    }

                    builder.addBossBar(Formatters.COLOR_FORMATTER.format(message), bossBarColor, bossBarStyle, ticks);
                    break;
                }
                case "sound":
                    builder.addSound(MenuParserUtils.getSound(section.getConfigurationSection("sound")));
                    break;
                case "title": {
                    String title = section.getString(key + ".title");
                    String subtitle = section.getString(key + ".sub-title");
                    int fadeIn = section.getInt(key + ".fade-in");
                    int duration = section.getInt(key + ".duration");
                    int fadeOut = section.getInt(key + ".fade-out");

                    builder.addTitle(Formatters.COLOR_FORMATTER.format(title),
                            Formatters.COLOR_FORMATTER.format(subtitle), fadeIn, duration, fadeOut);
                    break;
                }
                default: {
                    String text = section.getString(key + ".text");
                    String command = section.getString(key + ".command");
                    String suggest = section.getString(key + ".suggest");
                    String tooltip = section.getString(key + ".tooltip");
                    builder.addComplexMessage(Formatters.COLOR_FORMATTER.format(text), command, suggest,
                            Formatters.COLOR_FORMATTER.format(tooltip));
                    break;
                }
            }
        }

        return builder.build();
    }

    public static IMessageComponent of(List<IMessageComponent> messageComponents) {
        return messageComponents.isEmpty() ? EmptyMessageComponent.getInstance() :
                messageComponents.size() == 1 ? messageComponents.get(0) : new MultipleComponents(messageComponents);
    }

    private MultipleComponents(List<IMessageComponent> messageComponents) {
        this.messageComponents = messageComponents;
    }

    @Override
    public Type getType() {
        return Type.MULTIPLE;
    }

    @Override
    @Deprecated
    public String getMessage() {
        return messageComponents.isEmpty() ? "" : messageComponents.get(0).getMessage();
    }

    @Override
    public String getMessage(Object... args) {
        return messageComponents.isEmpty() ? "" : messageComponents.get(0).getMessage(args);
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        this.messageComponents.forEach(messageComponent -> messageComponent.sendMessage(sender, args));
    }

}
