package com.bgsoftware.superiorskyblock.lang.component;

import com.bgsoftware.superiorskyblock.lang.component.impl.ActionBarComponent;
import com.bgsoftware.superiorskyblock.lang.component.impl.ComplexMessageComponent;
import com.bgsoftware.superiorskyblock.lang.component.impl.TitleComponent;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public final class MultipleComponents implements IMessageComponent {

    private final List<IMessageComponent> messageComponents;

    public static IMessageComponent parseSection(ConfigurationSection section) {
        List<IMessageComponent> messageComponents = new ArrayList<>();

        for (String key : section.getKeys(false)) {
            if (key.equals("action-bar")) {
                messageComponents.add(ActionBarComponent.of(StringUtils.translateColors(section.getString(key + ".text"))));
            } else if (key.equals("title")) {
                messageComponents.add(TitleComponent.of(
                        StringUtils.translateColors(section.getString(key + ".title")),
                        StringUtils.translateColors(section.getString(key + ".sub-title")),
                        section.getInt(key + ".fade-in"),
                        section.getInt(key + ".duration"),
                        section.getInt(key + ".fade-out")
                ));
            } else {
                TextComponent textComponent = new TextComponent(StringUtils.translateColors(section.getString(key + ".text")));

                String toolTipMessage = section.getString(key + ".tooltip");
                if (toolTipMessage != null) {
                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new BaseComponent[]{new TextComponent(StringUtils.translateColors(toolTipMessage))}));
                }

                String commandMessage = section.getString(key + ".command");
                if (commandMessage != null)
                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandMessage));

                messageComponents.add(ComplexMessageComponent.of(textComponent));
            }
        }

        return messageComponents.isEmpty() ? EmptyMessageComponent.getInstance() :
                messageComponents.size() == 1 ? messageComponents.get(0) : new MultipleComponents(messageComponents);
    }

    private MultipleComponents(List<IMessageComponent> messageComponents) {
        this.messageComponents = messageComponents;
    }

    @Override
    public String getMessage() {
        return messageComponents.isEmpty() ? "" : messageComponents.get(0).getMessage();
    }

    @Override
    public void sendMessage(CommandSender sender, Object... objects) {
        this.messageComponents.forEach(messageComponent -> messageComponent.sendMessage(sender, objects));
    }

}
