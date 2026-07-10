package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ComplexMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Locale;

public class MultipleComponents implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section) {
        MessagesService.Builder builder = plugin.getServices().getService(MessagesService.class).newBuilder();

        for (String key : section.getKeys(false)) {
            switch (key) {
                case "action-bar": {
                    String text = section.getString(key + ".text");

                    builder.addActionBar(text);
                    break;
                }
                case "bossbar": {
                    String message = section.getString(key + ".message");
                    String color = section.getString(key + ".color", "PINK").toUpperCase(Locale.ENGLISH);
                    String overlay = section.getString(key + ".overlay", "PROGRESS").toUpperCase(Locale.ENGLISH);
                    int ticks = section.getInt(key + ".ticks");

                    builder.addBossBar(message, BossBar.Color.getSafe(color), BossBar.Style.getSafe(overlay), ticks);
                    break;
                }
                case "sound":
                    builder.addSound(MenuParserImpl.getInstance().getSound(section.getConfigurationSection("sound")));
                    break;
                case "title": {
                    String title = section.getString(key + ".title");
                    String subtitle = section.getString(key + ".sub-title");
                    int fadeIn = section.getInt(key + ".fade-in");
                    int duration = section.getInt(key + ".duration");
                    int fadeOut = section.getInt(key + ".fade-out");

                    builder.addTitle(title, subtitle, fadeIn, duration, fadeOut);
                    break;
                }
                default: {
                    String text = section.getString(key + ".text");
                    String command = section.getString(key + ".command");
                    String suggest = section.getString(key + ".suggest");
                    String tooltip = section.getString(key + ".tooltip");

                    if (command != null || suggest != null || tooltip != null) {
                        builder.addComplexMessage(ComplexMessageComponent.parseBaseComponents(
                                Formatters.COLOR_FORMATTER.format(text), command, suggest, tooltip));
                    } else {
                        builder.addRawMessage(text);
                    }

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
