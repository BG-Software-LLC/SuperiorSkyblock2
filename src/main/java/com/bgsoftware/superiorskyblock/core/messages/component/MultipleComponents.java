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
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;

public class MultipleComponents implements IMessageComponent {

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section) {
        List<IMessageComponent> messageComponents = new LinkedList<>();

        for (String key : section.getKeys(false)) {
            if (key.equals("action-bar")) {
                messageComponents.add(ActionBarComponent.of(Formatters.COLOR_FORMATTER.format(section.getString(key + ".text"))));
            } else if (key.equals("title")) {
                messageComponents.add(TitleComponent.of(
                        Formatters.COLOR_FORMATTER.format(section.getString(key + ".title")),
                        Formatters.COLOR_FORMATTER.format(section.getString(key + ".sub-title")),
                        section.getInt(key + ".fade-in"),
                        section.getInt(key + ".duration"),
                        section.getInt(key + ".fade-out")
                ));
            } else if (key.equals("sound")) {
                messageComponents.add(SoundComponent.of(MenuParserImpl.getInstance().getSound(section.getConfigurationSection("sound"))));
            } else if (key.equals("bossbar")) {
                BossBar.Color color;

                try {
                    color = BossBar.Color.valueOf(section.getString(key + ".color").toUpperCase());
                } catch (Exception error) {
                    color = BossBar.Color.PINK;
                }

                messageComponents.add(BossBarComponent.of(
                        Formatters.COLOR_FORMATTER.format(section.getString(key + ".message")),
                        color, section.getInt(key + ".ticks")));
            } else {
                BaseComponent[] baseComponents = TextComponent.fromLegacyText(Formatters.COLOR_FORMATTER.format(section.getString(key + ".text")));

                String toolTipMessage = section.getString(key + ".tooltip");
                if (toolTipMessage != null) {
                    for (BaseComponent baseComponent : baseComponents)
                        baseComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new BaseComponent[]{new TextComponent(Formatters.COLOR_FORMATTER.format(toolTipMessage))}));
                }

                String commandMessage = section.getString(key + ".command");
                if (commandMessage != null) {
                    for (BaseComponent baseComponent : baseComponents)
                        baseComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandMessage));
                }

                messageComponents.add(ComplexMessageComponent.of(baseComponents));
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
    public String getMessage() {
        return messageComponents.isEmpty() ? "" : messageComponents.get(0).getMessage();
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        this.messageComponents.forEach(messageComponent -> messageComponent.sendMessage(sender, args));
    }

}
