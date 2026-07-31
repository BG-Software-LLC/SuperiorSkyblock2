package com.bgsoftware.superiorskyblock.external.ui;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

public class UIProvider_Default extends BaseUIProvider {

    private final SuperiorSkyblockPlugin plugin;

    public UIProvider_Default(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public IMessageComponent createActionBarComponent(@Nullable String message) {
        return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new ActionBarComponent(message);
    }

    @Override
    public IMessageComponent createComplexMessageComponent(@Nullable String message, @Nullable String command,
                                                           @Nullable String suggest, @Nullable String tooltip) {
        if (command == null && suggest == null && tooltip == null) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
        } else {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() :
                    new ComplexMessageComponent(message, command, suggest, tooltip);
        }
    }

    @Override
    public IMessageComponent createComplexMessageComponent(@Nullable BaseComponent[] components) {
        if (components == null || components.length == 0)
            return EmptyMessageComponent.getInstance();

        String message = TextComponent.toLegacyText(components);
        String command = null;
        String suggest = null;
        String tooltip = null;

        for (BaseComponent component : components) {
            if (component.getClickEvent() != null && component.getClickEvent().getAction() == ClickEvent.Action.RUN_COMMAND) {
                command = component.getClickEvent().getValue();
            } else if (component.getClickEvent() != null && component.getClickEvent().getAction() == ClickEvent.Action.SUGGEST_COMMAND) {
                suggest = component.getClickEvent().getValue();
            }
            if (component.getHoverEvent() != null && component.getHoverEvent().getAction() == HoverEvent.Action.SHOW_TEXT) {
                tooltip = TextComponent.toLegacyText(component.getHoverEvent().getValue());
            }
        }

        return createComplexMessageComponent(message, command, suggest, tooltip);
    }

    @Override
    public IMessageComponent createTitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                                  int fadeIn, int stay, int fadeOut) {
        return stay <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
                EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
    }

    @Override
    public void setItemMetaDisplayName(ItemMeta itemMeta, String displayName) {
        itemMeta.setDisplayName(displayName);
    }

    @Override
    public void setItemMetaLore(ItemMeta itemMeta, List<String> lore) {
        itemMeta.setLore(lore);
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int size, String title) {
        return Bukkit.createInventory(inventoryHolder, size, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title) {
        return Bukkit.createInventory(inventoryHolder, inventoryType, title);
    }

    private class ActionBarComponent extends BaseMessageComponent {

        private ActionBarComponent(String message) {
            super(Type.ACTION_BAR, message);
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player)) {
                return;
            }

            Player player = (Player) sender;
            this.messageContent.getContent(player, args).ifPresent(message ->
                    plugin.getNMSPlayers().sendActionBar(player, message));
        }

    }

    private class RawMessageComponent extends BaseMessageComponent {

        private RawMessageComponent(String message) {
            super(Type.RAW_MESSAGE, message);
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            Player player = sender instanceof Player ? (Player) sender : null;
            this.messageContent.getContent(player, args).ifPresent(sender::sendMessage);
        }

    }

    private class ComplexMessageComponent extends BaseMessageComponent {

        private final Optional<MessageContent> hoverEvent;
        private final Optional<Pair<ClickEvent.Action, MessageContent>> clickEvent;

        private ComplexMessageComponent(String message, @Nullable String command, @Nullable String suggest,
                                        @Nullable String tooltip) {
            super(Type.COMPLEX_MESSAGE, message);

            if (command != null) {
                this.clickEvent = Optional.of(new Pair<>(ClickEvent.Action.RUN_COMMAND, MessageContent.parse(command)));
            } else if (suggest != null) {
                this.clickEvent = Optional.of(new Pair<>(ClickEvent.Action.SUGGEST_COMMAND, MessageContent.parse(suggest)));
            } else {
                this.clickEvent = Optional.empty();
            }

            if (tooltip != null) {
                this.hoverEvent = Optional.of(MessageContent.parse(tooltip));
            } else {
                this.hoverEvent = Optional.empty();
            }
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            Player player = sender instanceof Player ? (Player) sender : null;

            this.messageContent.getContent(player, args).ifPresent(message -> {
                BaseComponent[] components = TextComponent.fromLegacyText(message);

                this.clickEvent.ifPresent(clickEventData -> {
                    clickEventData.getValue().getContent(player, args).ifPresent(clickEventContent -> {
                        ClickEvent clickEvent = new ClickEvent(clickEventData.getKey(), clickEventContent);
                        for (BaseComponent component : components) {
                            component.setClickEvent(clickEvent);
                        }
                    });
                });

                this.hoverEvent.flatMap(hoverEventContent -> hoverEventContent.getContent(player, args))
                        .ifPresent(hoverEventContent -> {
                            HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hoverEventContent));
                            for (BaseComponent component : components) {
                                component.setHoverEvent(hoverEvent);
                            }
                        });

                sender.sendMessage(components);
            });
        }

    }

    private class TitleComponent extends BaseMessageComponent {

        private final MessageContent subtitleContent;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        private TitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage, int fadeIn, int stay, int fadeOut) {
            super(Type.TITLE, titleMessage);
            this.subtitleContent = Text.isBlank(subtitleMessage) ? MessageContent.EMPTY : MessageContent.parse(subtitleMessage);
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player)) {
                return;
            }

            Player player = (Player) sender;
            String titleMessage = this.messageContent.getContent(player, args).orElse(null);
            String subtitleMessage = this.subtitleContent.getContent(player, args).orElse(null);

            if (titleMessage != null && subtitleMessage != null) {
                plugin.getNMSPlayers().sendTitle(player, titleMessage, subtitleMessage,
                        this.fadeIn, this.stay, this.fadeOut);
            }
        }

    }

}
