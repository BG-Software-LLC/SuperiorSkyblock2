package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ComplexMessageComponent implements IMessageComponent {

    private final BaseComponent[] baseComponents;
    private final TextComponent textComponent;

    public static IMessageComponent of(@Nullable BaseComponent[] baseComponents) {
        if (baseComponents == null || baseComponents.length == 0)
            return EmptyMessageComponent.getInstance();

        boolean isTextEmpty = true;

        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent && !Text.isBlank(((TextComponent) baseComponent).getText())) {
                isTextEmpty = false;
                break;
            }
        }

        return isTextEmpty ? EmptyMessageComponent.getInstance() : new ComplexMessageComponent(baseComponents);
    }

    private ComplexMessageComponent(BaseComponent[] baseComponents) {
        this.baseComponents = baseComponents;
        this.textComponent = findTextComponent(baseComponents);
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
            BaseComponent[] duplicate = replaceArgs(this.baseComponents, args);
            if (duplicate.length > 0)
                ((Player) sender).spigot().sendMessage(duplicate);
        }
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

    @Nullable
    private static TextComponent findTextComponent(BaseComponent[] baseComponents) {
        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent)
                return (TextComponent) baseComponent;
        }

        return null;
    }

}
