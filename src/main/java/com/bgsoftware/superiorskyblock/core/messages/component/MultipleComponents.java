package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ComplexMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.SoundComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;

public class MultipleComponents implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section) {
        List<IMessageComponent> messageComponents = new LinkedList<>();

        for (String key : section.getKeys(false)) {
            switch (key) {
                case "action-bar": {
                    String text = section.getString(key + ".text");

                    messageComponents.add(plugin.getProviders().getMessagesProvider().createActionBarComponent(text));
                    break;
                }
                case "bossbar": {
                    String message = section.getString(key + ".message");
                    String color = section.getString(key + ".color", "PINK").toUpperCase();
                    String overlay = section.getString(key + ".overlay", "PROGRESS").toUpperCase();
                    int ticks = section.getInt(key + ".ticks");

                    messageComponents.add(plugin.getProviders().getMessagesProvider().createBossBarComponent(message, color, overlay, ticks));
                    break;
                }
                case "sound":
                    messageComponents.add(SoundComponent.of(MenuParserImpl.getInstance().getSound(section.getConfigurationSection("sound"))));
                    break;
                case "title": {
                    String title = section.getString(key + ".title");
                    String subtitle = section.getString(key + ".sub-title");
                    int fadeIn = section.getInt(key + ".fade-in");
                    int duration = section.getInt(key + ".duration");
                    int fadeOut = section.getInt(key + ".fade-out");

                    messageComponents.add(plugin.getProviders().getMessagesProvider().createTitleComponent(title, subtitle, fadeIn, duration, fadeOut));
                    break;
                }
                default: {
                    String text = section.getString(key + ".text");
                    String command = section.getString(key + ".command");
                    String suggest = section.getString(key + ".suggest");
                    String tooltip = section.getString(key + ".tooltip");

                    if (command != null || suggest != null || tooltip != null) {
                        messageComponents.add(ComplexMessageComponent.of(Formatters.COLOR_FORMATTER.format(text), command, suggest, tooltip));
                    } else {
                        messageComponents.add(plugin.getProviders().getMessagesProvider().createRawMessageComponent(text));
                    }

                    break;
                }
            }
        }

        messageComponents.removeIf(component -> component.getType() == Type.EMPTY);

        return of(messageComponents);
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
