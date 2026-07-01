package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ActionBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.BossBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ComplexMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.SoundComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.TitleComponent;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MultipleComponents implements IMessageComponent {

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section, List<MessagesServiceImpl.CustomComponentParser> customComponentParsers) {
        List<IMessageComponent> messageComponents = new LinkedList<>();

        for (String key : section.getKeys(false)) {
            switch (key) {
                case "action-bar": {
                    String text = section.getString(key + ".text");
                    boolean parsed = false;

                    for (MessagesServiceImpl.CustomComponentParser parser : customComponentParsers) {
                        Optional<IMessageComponent> res = parser.parseActionBar(text);
                        if (res.isPresent()) {
                            messageComponents.add(res.get());
                            parsed = true;
                            break;
                        }
                    }

                    if (!parsed) {
                        messageComponents.add(ActionBarComponent.of(Formatters.COLOR_FORMATTER.format(text)));
                    }
                    break;
                }
                case "bossbar": {
                    String message = section.getString(key + ".message");
                    String color = section.getString(key + ".color", "PINK").toUpperCase();
                    String overlay = section.getString(key + ".overlay", "PROGRESS").toUpperCase();
                    int ticks = section.getInt(key + ".ticks");
                    boolean parsed = false;

                    for (MessagesServiceImpl.CustomComponentParser parser : customComponentParsers) {
                        Optional<IMessageComponent> res = parser.parseBossBar(message, color, overlay, ticks);
                        if (res.isPresent()) {
                            messageComponents.add(res.get());
                            parsed = true;
                            break;
                        }
                    }

                    if (!parsed) {
                        BossBar.Color bossBarColor;
                        try {
                            bossBarColor = BossBar.Color.valueOf(color);
                        } catch (Exception error) {
                            bossBarColor = BossBar.Color.PINK;
                        }

                        messageComponents.add(BossBarComponent.of(Formatters.COLOR_FORMATTER.format(message), bossBarColor, BossBar.Style.SOLID, ticks));
                    }
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
                    boolean parsed = false;

                    for (MessagesServiceImpl.CustomComponentParser parser : customComponentParsers) {
                        Optional<IMessageComponent> res = parser.parseTitle(title, subtitle, fadeIn, duration, fadeOut);
                        if (res.isPresent()) {
                            messageComponents.add(res.get());
                            parsed = true;
                            break;
                        }
                    }

                    if (!parsed) {
                        messageComponents.add(TitleComponent.of(Formatters.COLOR_FORMATTER.format(title),
                                Formatters.COLOR_FORMATTER.format(subtitle), fadeIn, duration, fadeOut));
                    }
                    break;
                }
                default: {
                    String text = section.getString(key + ".text");

                    for (MessagesServiceImpl.CustomComponentParser parser : customComponentParsers) {
                        Optional<IMessageComponent> res = parser.parseRawMessage(text);
                        if (res.isPresent()) {
                            messageComponents.add(res.get());
                            break;
                        }
                    }

                    BaseComponent[] baseComponents = TextComponent.fromLegacyText(Formatters.COLOR_FORMATTER.format(text));

                    String tooltip = section.getString(key + ".tooltip");
                    if (tooltip != null) {
                        for (BaseComponent baseComponent : baseComponents)
                            baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new BaseComponent[]{new TextComponent(Formatters.COLOR_FORMATTER.format(tooltip))}));
                    }

                    String command = section.getString(key + ".command");
                    if (command != null) {
                        for (BaseComponent baseComponent : baseComponents)
                            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
                    }

                    String suggest = section.getString(key + ".suggest");
                    if (suggest != null) {
                        for (BaseComponent baseComponent : baseComponents)
                            baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));
                    }

                    messageComponents.add(ComplexMessageComponent.of(baseComponents));
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
