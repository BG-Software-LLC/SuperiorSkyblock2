package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class ComplexMessageComponent implements IMessageComponent {

    private final IWrappedComponent[] components;
    private final MessageContent content;

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
        this.components = parseComponents(baseComponents);
        this.content = findTextComponentContent(baseComponents);
    }

    private static IWrappedComponent[] parseComponents(BaseComponent[] baseComponents) {
        IWrappedComponent[] components = new IWrappedComponent[baseComponents.length];
        for (int i = 0; i < components.length; ++i) {
            BaseComponent curr = baseComponents[i];
            IWrappedComponent wrappedComponent;
            if (curr instanceof TextComponent) {
                wrappedComponent = new ContentComponent((TextComponent) curr);
            } else if (curr.getHoverEvent() != null) {
                wrappedComponent = new HoverEventComponent(curr);
            } else {
                wrappedComponent = new ComponentHolder(curr);
            }
            components[i] = wrappedComponent;
        }
        return components;
    }

    private static MessageContent findTextComponentContent(BaseComponent[] baseComponents) {
        for (BaseComponent baseComponent : baseComponents) {
            if (baseComponent instanceof TextComponent)
                return MessageContent.parse(baseComponent.toLegacyText());
        }

        return MessageContent.EMPTY;
    }

    @Override
    public Type getType() {
        return Type.COMPLEX_MESSAGE;
    }

    @Override
    public String getMessage() {
        return this.content.getContent(null).orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.content.getContent(null, args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (!(sender instanceof Player)) {
            this.content.getContent(null, args).ifPresent(sender::sendMessage);
        } else if (components.length > 0) {
            BaseComponent[] components = new BaseComponent[this.components.length];
            for (int i = 0; i < this.components.length; ++i)
                components[i] = this.components[i].parse((Player) sender, args);
            ((Player) sender).spigot().sendMessage(components);
        }
    }

    private interface IWrappedComponent {

        BaseComponent parse(OfflinePlayer offlinePlayer, Object... args);

    }

    private static class ComponentHolder implements IWrappedComponent {

        private final BaseComponent handle;

        ComponentHolder(BaseComponent handle) {
            this.handle = handle;
        }

        @Override
        public BaseComponent parse(OfflinePlayer offlinePlayer, Object... args) {
            return this.handle;
        }

    }

    private static class ContentComponent implements IWrappedComponent {

        private final MessageContent content;
        @Nullable
        private final HoverEventContents hoverEventContents;
        @Nullable
        private final ClickEvent.Action clickEventAction;
        @Nullable
        private final MessageContent clickEventContent;

        ContentComponent(TextComponent textComponent) {
            this.content = MessageContent.parse(textComponent.toLegacyText());
            this.hoverEventContents = textComponent.getHoverEvent() == null ? null :
                    new HoverEventContents(textComponent.getHoverEvent());
            ClickEvent clickEvent = textComponent.getClickEvent();
            if (clickEvent != null) {
                this.clickEventAction = clickEvent.getAction();
                this.clickEventContent = MessageContent.parse(clickEvent.getValue());
            } else {
                this.clickEventAction = null;
                this.clickEventContent = null;
            }
        }

        @Override
        public BaseComponent parse(OfflinePlayer offlinePlayer, Object... args) {
            TextComponent textComponent = (TextComponent) TextComponent.fromLegacyText(
                    this.content.getContent(offlinePlayer, args).orElse(""))[0];
            if (this.hoverEventContents != null)
                textComponent.setHoverEvent(this.hoverEventContents.parse(offlinePlayer, args));
            if (this.clickEventAction != null && this.clickEventContent != null) {
                textComponent.setClickEvent(new ClickEvent(this.clickEventAction,
                        this.clickEventContent.getContent(offlinePlayer, args).orElse("")));
            }

            return textComponent;
        }
    }

    private static class HoverEventComponent implements IWrappedComponent {

        private final BaseComponent baseComponent;
        private final HoverEventContents hoverEventContents;

        HoverEventComponent(BaseComponent baseComponent) {
            this.baseComponent = baseComponent;
            this.hoverEventContents = new HoverEventContents(baseComponent.getHoverEvent());
        }

        @Override
        public BaseComponent parse(OfflinePlayer offlinePlayer, Object... args) {
            BaseComponent newComponent = this.baseComponent.duplicate();
            newComponent.setHoverEvent(this.hoverEventContents.parse(offlinePlayer, args));
            return newComponent;
        }

    }

    private static class HoverEventContents {

        private final HoverEvent.Action action;
        private final IWrappedComponent[] components;

        HoverEventContents(HoverEvent hoverEvent) {
            this.action = hoverEvent.getAction();
            this.components = parseComponents(hoverEvent.getValue());
        }

        HoverEvent parse(OfflinePlayer offlinePlayer, Object... args) {
            BaseComponent[] components = new BaseComponent[this.components.length];
            for (int i = 0; i < components.length; ++i)
                components[i] = this.components[i].parse(offlinePlayer, args);
            return new HoverEvent(this.action, components);
        }

    }

}
