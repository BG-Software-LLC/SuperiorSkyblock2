package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

public class MessagesProvider_MiniMessage implements MessagesProvider {

    // Adventure 5.x renamed the Title.Times factory method from 'of' to 'times',
    // breaking binary compatibility with Adventure 4.x. Resolve the method
    // reflectively so a single build works on both versions.
    private static final ReflectMethod<Title.Times> TITLE_TIMES_FACTORY = getTitleTimesFactory();

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();

    public MessagesProvider_MiniMessage(SuperiorSkyblockPlugin plugin) {
        Log.info("Using MiniMessage as a messages provider.");
    }

    @Override
    public IMessageComponent createActionBarComponent(String message) {
        return ActionBarComponent.of(message);
    }

    @Override
    public IMessageComponent createBossBarComponent(String message, com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar.Color color,
                                                    com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar.Style style, int duration) {
        return BossBarComponent.of(message, BossBar.Color.valueOf(color.name()), mapBossBarStyle(style), duration);
    }

    @Override
    public IMessageComponent createRawMessageComponent(String message) {
        return RawMessageComponent.of(message);
    }

    @Override
    public IMessageComponent createTitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
    }

    private static BossBar.Overlay mapBossBarStyle(com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar.Style style) {
        return switch (style) {
            case SEGMENTED_6, NOTCHED_6 -> BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10, NOTCHED_10 -> BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12, NOTCHED_12 -> BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20, NOTCHED_20 -> BossBar.Overlay.NOTCHED_20;
            default -> BossBar.Overlay.PROGRESS;
        };
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

    private static class ActionBarComponent implements IMessageComponent {

        private final MessageContent messageContent;

        public static IMessageComponent of(@Nullable String message) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new ActionBarComponent(message);
        }

        private ActionBarComponent(String message) {
            this.messageContent = MessageContent.parse(message);
        }

        @Override
        public Type getType() {
            return Type.ACTION_BAR;
        }

        @Override
        public String getMessage() {
            return this.messageContent.getContent(null).orElse("");
        }

        @Override
        public String getMessage(Object... args) {
            return this.messageContent.getContent(null, args).orElse("");
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

    private static class BossBarComponent implements IMessageComponent {

        private final MessageContent messageContent;
        private final BossBar.Color color;
        private final BossBar.Overlay overlay;
        private final double duration;

        public static IMessageComponent of(@Nullable String name, BossBar.Color color, BossBar.Overlay overlay, int duration) {
            return duration <= 0 || Text.isBlank(name) ? EmptyMessageComponent.getInstance() : new BossBarComponent(name, color, overlay, duration);
        }

        private BossBarComponent(String name, BossBar.Color color, BossBar.Overlay overlay, double duration) {
            this.messageContent = MessageContent.parse(name);
            this.color = color;
            this.overlay = overlay;
            this.duration = duration;
        }

        @Override
        public Type getType() {
            return Type.BOSS_BAR;
        }

        @Override
        public String getMessage() {
            return this.messageContent.getContent(null).orElse("");
        }

        @Override
        public String getMessage(Object... args) {
            return this.messageContent.getContent(null, args).orElse("");
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player player)) {
                return;
            }

            this.messageContent.getContent(player, args).ifPresent(message -> {
                BossBar bossBar = BossBar.bossBar(deserialize(message,
                        this.messageContent.hasLegacyColorCodes()), 1.0f, color, overlay);
                sender.showBossBar(bossBar);
                new BossBarImpl(bossBar, duration).addPlayer(player);
            });
        }

    }

    private static class RawMessageComponent implements IMessageComponent {

        private final MessageContent messageContent;

        public static IMessageComponent of(@Nullable String message) {
            return Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new RawMessageComponent(message);
        }

        private RawMessageComponent(String message) {
            this.messageContent = MessageContent.parse(message);
        }

        @Override
        public Type getType() {
            return Type.RAW_MESSAGE;
        }

        @Override
        public String getMessage() {
            return this.messageContent.getContent(null).orElse("");
        }

        @Override
        public String getMessage(Object... args) {
            return this.messageContent.getContent(null, args).orElse("");
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            Player player = sender instanceof Player ? (Player) sender : null;

            this.messageContent.getContent(player, args).ifPresent(message ->
                sender.sendMessage(deserialize(message, this.messageContent.hasLegacyColorCodes())));
        }

    }

    private static class TitleComponent implements IMessageComponent {

        private final MessageContent titleContent;
        private final MessageContent subtitleContent;
        private final Title.Times titleTimes;

        public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                           int fadeIn, int stay, int fadeOut) {
            return stay <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
                    EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
        }

        private TitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut) {
            this.titleContent = Text.isBlank(titleMessage) ? MessageContent.EMPTY : MessageContent.parse(titleMessage);
            this.subtitleContent = Text.isBlank(subtitleMessage) ? MessageContent.EMPTY : MessageContent.parse(subtitleMessage);
            this.titleTimes = createTimes(fadeIn, stay, fadeOut);
        }

        @Override
        public Type getType() {
            return Type.TITLE;
        }

        @Override
        public String getMessage() {
            return this.titleContent.getContent(null).orElse("");
        }

        @Override
        public String getMessage(Object... args) {
            return this.titleContent.getContent(null, args).orElse("");
        }

        @Override
        public void sendMessage(CommandSender sender, Object... args) {
            if (!(sender instanceof Player player)) {
                return;
            }

            String titleMessage = this.titleContent.getContent(player, args).orElse(null);
            String subtitleMessage = this.subtitleContent.getContent(player, args).orElse(null);

            if (titleMessage != null && subtitleMessage != null) {
                Component titleComponent = deserialize(titleMessage, this.titleContent.hasLegacyColorCodes());
                Component subtitleComponent = deserialize(subtitleMessage, this.subtitleContent.hasLegacyColorCodes());

                Title title = Title.title(titleComponent, subtitleComponent, this.titleTimes);
                sender.showTitle(title);
            }
        }

    }

    private static class BossBarImpl implements com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar {

        private final BossBar bossBar;
        private final BossBarTask bossBarTask;
        private final Set<Player> players = new HashSet<>();

        public BossBarImpl(BossBar bossBar, double ticksToRun) {
            this.bossBar = bossBar;
            this.bossBarTask = BossBarTask.create(this, ticksToRun);
        }

        @Override
        public void addPlayer(Player player) {
            if (this.players.add(player)) {
                player.showBossBar(this.bossBar);
                this.bossBarTask.registerTask(player);
            }
        }

        @Override
        public void removeAll() {
            for (Player player : this.players) {
                player.hideBossBar(this.bossBar);
                this.bossBarTask.unregisterTask(player);
            }
            this.players.clear();
        }

        @Override
        public void setProgress(double progress) {
            this.bossBar.progress((float) Math.max(0.0, Math.min(1.0, progress)));
        }

        @Override
        public double getProgress() {
            return this.bossBar.progress();
        }
    }

}
