package com.bgsoftware.superiorskyblock.external.ui;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class UIProvider_MiniMessage extends BaseUIProvider {

    // Adventure 5.x renamed the Title.Times factory method from 'of' to 'times',
    // breaking binary compatibility with Adventure 4.x. Resolve the method
    // reflectively so a single build works on both versions.
    private static final ReflectMethod<Title.Times> TITLE_TIMES_FACTORY = getTitleTimesFactory();
    private static final ClickEventCreator CLICK_EVENT_CREATE = initializeClickEventCreation();

    private static final ClickEvent.Action RUN_COMMAND_CLICK_EVENT = getClickEvent("RUN_COMMAND");
    private static final ClickEvent.Action SUGGEST_COMMAND_CLICK_EVENT = getClickEvent("SUGGEST_COMMAND");

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final LazyReference<BungeeComponentSerializer> BUNGEE_COMPONENT_SERIALIZER = new LazyReference<>() {
        @Override
        protected BungeeComponentSerializer create() {
            return ServerVersion.isAtLeast(ServerVersion.v1_16) ? BungeeComponentSerializer.get() : BungeeComponentSerializer.legacy();
        }
    };

    public UIProvider_MiniMessage(SuperiorSkyblockPlugin plugin) {
        Log.info("Using MiniMessage as a ui provider.");
    }

    @Override
    public IMessageComponent createActionBarComponent(@Nullable String message) {
        return ActionBarComponent.of(message);
    }

    @Override
    public IMessageComponent createComplexMessageComponent(@Nullable String message, @Nullable String command,
                                                           @Nullable String suggest, @Nullable String tooltip) {
        if (command == null && suggest == null && tooltip == null) {
            return RawMessageComponent.of(message);
        } else {
            return ComplexMessageComponent.of(message, command, suggest, tooltip);
        }
    }

    @Override
    public IMessageComponent createComplexMessageComponent(@Nullable BaseComponent[] components) {
        if (components == null || components.length == 0)
            return EmptyMessageComponent.getInstance();

        Component component = BUNGEE_COMPONENT_SERIALIZER.get().deserialize(components);
        ClickEvent clickEvent = component.clickEvent();
        HoverEvent<?> hoverEvent = component.hoverEvent();

        component = component.clickEvent(null).hoverEvent(null);

        String message = MINI_MESSAGE.serialize(component);
        String command = clickEvent != null && clickEvent.action() == RUN_COMMAND_CLICK_EVENT ?
                clickEvent.value() : null;
        String suggest = clickEvent != null && clickEvent.action() == SUGGEST_COMMAND_CLICK_EVENT ?
                clickEvent.value() : null;
        String tooltip = hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT ?
                MINI_MESSAGE.serialize((Component) hoverEvent.value()) : null;

        return ComplexMessageComponent.of(message, command, suggest, tooltip);
    }

    @Override
    public IMessageComponent createTitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                                  int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
    }

    @Override
    public void setItemMetaDisplayName(ItemMeta itemMeta, String displayName) {
        itemMeta.displayName(deserializeWithoutItalic(displayName));
    }

    @Override
    public void setItemMetaLore(ItemMeta itemMeta, List<String> lore) {
        itemMeta.lore(new SequentialListBuilder<Component>().build(lore, UIProvider_MiniMessage::deserializeWithoutItalic));
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, InventoryType inventoryType, String title) {
        return Bukkit.createInventory(inventoryHolder, inventoryType, deserializeWithoutItalic(title));
    }

    @Override
    public Inventory createInventory(InventoryHolder inventoryHolder, int size, String title) {
        return Bukkit.createInventory(inventoryHolder, size, deserializeWithoutItalic(title));
    }

    private static Component deserializeWithoutItalic(String message) {
        Component component = deserialize(message, message.indexOf(ChatColor.COLOR_CHAR) >= 0);
        if (component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET) {
            component = component.decoration(TextDecoration.ITALIC, false);
        }

        return component;
    }

    private static Component deserialize(String message, boolean legacyColorCodes) {
        if (legacyColorCodes) {
            return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        }

        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (ParsingException exception) {
            return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        }
    }

    private static ReflectMethod<Title.Times> getTitleTimesFactory() {
        ReflectMethod<Title.Times> method = new ReflectMethod<>(Title.Times.class, "times",
                Duration.class, Duration.class, Duration.class);

        if (method.isValid()) {
            return method;
        }

        method = new ReflectMethod<>(Title.Times.class, "of",
                Duration.class, Duration.class, Duration.class);

        if (method.isValid()) {
            return method;
        }

        throw new ExceptionInInitializerError("Couldn't find a method to create a Title for MiniMessage.");
    }

    private static Title.Times createTimes(int fadeIn, int stay, int fadeOut) {
        return TITLE_TIMES_FACTORY.invoke(null, Duration.ofMillis(fadeIn * 50L),
                Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L));
    }

    private static ClickEvent.Action getClickEvent(String name) {
        if (ClickEvent.Action.class.isAssignableFrom(Enum.class)) {
            return ClickEvent.Action.valueOf(name);
        } else {
            ReflectField<ClickEvent.Action> ACTION_FIELD = new ReflectField<>(
                    ClickEvent.Action.class, ClickEvent.Action.class, name);
            return ACTION_FIELD.get(null);
        }
    }

    private static ClickEventCreator initializeClickEventCreation() {
        ReflectMethod<ClickEvent> STRING_CREATE_METHOD = new ReflectMethod<>(
                ClickEvent.class, "clickEvent", ClickEvent.Action.class, String.class);

        if (STRING_CREATE_METHOD.isValid()) {
            return (action, payload) -> {
                return STRING_CREATE_METHOD.invoke(null, action, payload);
            };
        }

        ReflectMethod<ClickEvent> PAYLOAD_CREATE_METHOD = new ReflectMethod<>(
                ClickEvent.class, "clickEvent", new ClassInfo(ClickEvent.Action.class),
                new ClassInfo("net.kyori.adventure.text.event.ClickEvent$Payload", ClassInfo.PackageType.UNKNOWN));
        ReflectMethod<Object> CLICK_EVENT_PAYLOAD_TEXT = new ReflectMethod<>(
                new ClassInfo("net.kyori.adventure.text.event.ClickEvent$Payload", ClassInfo.PackageType.UNKNOWN),
                "string", String.class);

        if (PAYLOAD_CREATE_METHOD.isValid() && CLICK_EVENT_PAYLOAD_TEXT.isValid()) {
            return (action, payload) -> {
                Object textPayload = CLICK_EVENT_PAYLOAD_TEXT.invoke(null, payload);
                return PAYLOAD_CREATE_METHOD.invoke(null, action, textPayload);
            };
        }

        throw new IllegalStateException("Cannot find valid ClickEvent creator");
    }

    private interface ClickEventCreator {

        ClickEvent create(ClickEvent.Action action, String payload);

    }

    private static class ActionBarComponent extends BaseMessageComponent {

        public static IMessageComponent of(@Nullable String message) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new ActionBarComponent(message);
        }

        private ActionBarComponent(String message) {
            super(Type.ACTION_BAR, message);
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player player)) {
                return;
            }

            this.messageContent.getContent(player, args).ifPresent(message ->
                    sender.sendActionBar(deserialize(message, this.messageContent.hasLegacyColorCodes())));
        }

    }

    private static class RawMessageComponent extends BaseMessageComponent {

        public static IMessageComponent of(@Nullable String message) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
        }

        private RawMessageComponent(String message) {
            super(Type.RAW_MESSAGE, message);
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            Player player = sender instanceof Player ? (Player) sender : null;

            this.messageContent.getContent(player, args).ifPresent(message ->
                    sender.sendMessage(deserialize(message, this.messageContent.hasLegacyColorCodes())));
        }

    }

    private static class ComplexMessageComponent extends BaseMessageComponent {

        private final Optional<MessageContent> hoverEvent;
        private final Optional<Pair<ClickEvent.Action, MessageContent>> clickEvent;

        public static IMessageComponent of(@Nullable String message, @Nullable String command,
                                           @Nullable String suggest, @Nullable String tooltip) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() :
                    new ComplexMessageComponent(message, command, suggest, tooltip);
        }

        private ComplexMessageComponent(String message, @Nullable String command, @Nullable String suggest,
                                        @Nullable String tooltip) {
            super(Type.COMPLEX_MESSAGE, message);

            if (command != null) {
                this.clickEvent = Optional.of(new Pair<>(RUN_COMMAND_CLICK_EVENT, MessageContent.parse(command)));
            } else if (suggest != null) {
                this.clickEvent = Optional.of(new Pair<>(SUGGEST_COMMAND_CLICK_EVENT, MessageContent.parse(suggest)));
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
                Component component = deserialize(message, this.messageContent.hasLegacyColorCodes());

                if (this.clickEvent.isPresent()) {
                    Optional<String> clickEventDataOpt = this.clickEvent.get().getValue().getContent(player, args);
                    if (clickEventDataOpt.isPresent()) {
                        component = component.clickEvent(CLICK_EVENT_CREATE.create(this.clickEvent.get().getKey(), clickEventDataOpt.get()));
                    }
                }

                Optional<String> hoverEventDataOpt = this.hoverEvent.flatMap(
                        hoverEventData -> hoverEventData.getContent(player, args));
                if (hoverEventDataOpt.isPresent()) {
                    component = component.hoverEvent(HoverEvent.showText(
                            deserialize(hoverEventDataOpt.get(), this.hoverEvent.get().hasLegacyColorCodes())));
                }

                sender.sendMessage(component);
            });
        }

    }

    private static class TitleComponent extends BaseMessageComponent {

        private final MessageContent subtitleContent;
        private final Title.Times titleTimes;

        public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                           int fadeIn, int stay, int fadeOut) {
            return stay <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
                    EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
        }

        private TitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut) {
            super(Type.TITLE, titleMessage);
            this.subtitleContent = Text.isBlank(subtitleMessage) ? MessageContent.EMPTY : MessageContent.parse(subtitleMessage);
            this.titleTimes = createTimes(fadeIn, stay, fadeOut);
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player player)) {
                return;
            }

            String titleMessage = this.messageContent.getContent(player, args).orElse(null);
            String subtitleMessage = this.subtitleContent.getContent(player, args).orElse(null);

            if (titleMessage != null && subtitleMessage != null) {
                Component titleComponent = deserialize(titleMessage, this.messageContent.hasLegacyColorCodes());
                Component subtitleComponent = deserialize(subtitleMessage, this.subtitleContent.hasLegacyColorCodes());

                Title title = Title.title(titleComponent, subtitleComponent, this.titleTimes);
                sender.showTitle(title);
            }
        }

    }

}
