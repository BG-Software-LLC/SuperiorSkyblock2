package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class ComplexMessageComponent implements IMessageComponent {

    private final TextComponent textComponent;

    public static IMessageComponent of(@Nullable TextComponent textComponent) {
        return textComponent == null || StringUtils.isBlank(textComponent.getText()) ?
                EmptyMessageComponent.getInstance() : new ComplexMessageComponent(textComponent);
    }

    private ComplexMessageComponent(TextComponent textComponent) {
        this.textComponent = textComponent;
    }

    @Override
    public Type getType() {
        return Type.COMPLEX_MESSAGE;
    }

    @Override
    public String getMessage() {
        return this.textComponent.getText();
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (!(sender instanceof Player)) {
            String rawMessage = getMessage();
            if (rawMessage != null && !rawMessage.isEmpty())
                sender.sendMessage(rawMessage);
        } else {
            BaseComponent[] duplicate = replaceArgs(this.textComponent, args);
            if (duplicate.length > 0)
                ((Player) sender).spigot().sendMessage(duplicate);
        }
    }

    private static BaseComponent[] replaceArgs(BaseComponent textComponent, Object... objects) {
        return replaceArgs(new BaseComponent[]{textComponent}, objects);
    }

    private static BaseComponent[] replaceArgs(BaseComponent[] textComponents, Object... objects) {
        BaseComponent[] duplicate = new BaseComponent[textComponents.length];

        for (int i = 0; i < textComponents.length; i++) {
            duplicate[i] = textComponents[i].duplicate();
            if (duplicate[i] instanceof TextComponent) {
                TextComponent textComponent = (TextComponent) duplicate[i];
                textComponent.setText(Message.replaceArgs(textComponent.getText(), objects).orElse(""));
            }
            HoverEvent hoverEvent = duplicate[i].getHoverEvent();
            if (hoverEvent != null)
                duplicate[i].setHoverEvent(new HoverEvent(hoverEvent.getAction(), replaceArgs(hoverEvent.getValue(), objects)));
        }

        return duplicate;
    }

}
