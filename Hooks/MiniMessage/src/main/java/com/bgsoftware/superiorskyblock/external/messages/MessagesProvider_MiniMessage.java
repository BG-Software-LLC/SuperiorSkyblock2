package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.MessagesProvider;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
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

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class MessagesProvider_MiniMessage implements MessagesProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    public MessagesProvider_MiniMessage(SuperiorSkyblockPlugin plugin) {
        Log.info("Using MiniMessage as a messages provider.");
    }

    @Override
    public IMessageComponent createActionBarComponent(String message) {
        return ActionBarComponent.of(message);
    }

    @Override
    public IMessageComponent createBossBarComponent(String name, String color, String overlay, int duration) {
        BossBar.Color bossBarColor;
        try {
            bossBarColor = BossBar.Color.valueOf(color);
        } catch (IllegalArgumentException error) {
            bossBarColor = BossBar.Color.PINK;
        }

        BossBar.Overlay bossBarOverlay;
        try {
            bossBarOverlay = BossBar.Overlay.valueOf(overlay);
        } catch (IllegalArgumentException error) {
            bossBarOverlay = BossBar.Overlay.PROGRESS;
        }

        return BossBarComponent.of(name, bossBarColor, bossBarOverlay, duration);
    }

    @Override
    public IMessageComponent createRawMessageComponent(String message) {
        return RawMessageComponent.of(message);
    }

    @Override
    public IMessageComponent createTitleComponent(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(title, subtitle, fadeIn, stay, fadeOut);
    }

    private static Component deserialize(String message) {
        String formattedMessage = Formatters.COLOR_FORMATTER.format(message);

        try {
            return MINI_MESSAGE.deserialize(formattedMessage);
        } catch (ParsingException exception) {
            return LegacyComponentSerializer.legacySection().deserialize(formattedMessage);
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
            if (!(sender instanceof Player)) {
                return;
            }

            this.messageContent.getContent((Player) sender, args).ifPresent(message ->
                    sender.sendActionBar(deserialize(message)));
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
                BossBar bossBar = BossBar.bossBar(deserialize(message), 1.0f, color, overlay);
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
                sender.sendMessage(deserialize(message)));
        }

    }

    // Adventure 5.x renamed the Title.Times factory method from 'of' to 'times',
    // breaking binary compatibility with Adventure 4.x. Resolve the method
    // reflectively so a single build works on both versions.
    private static final Method TITLE_TIMES_FACTORY = getTitleTimesFactory();

    private static Method getTitleTimesFactory() {
        try {
            return Title.Times.class.getMethod("times", Duration.class, Duration.class, Duration.class);
        } catch (NoSuchMethodException exception) {
            try {
                return Title.Times.class.getMethod("of", Duration.class, Duration.class, Duration.class);
            } catch (NoSuchMethodException exception1) {
                throw new ExceptionInInitializerError(exception1);
            }
        }
    }

    private static Title.Times createTimes(int fadeIn, int stay, int fadeOut) {
        try {
            return (Title.Times) TITLE_TIMES_FACTORY.invoke(null, Duration.ofMillis(fadeIn * 50L),
                    Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L));
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException(ex);
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
            this.titleContent = MessageContent.parse(titleMessage);
            this.subtitleContent = MessageContent.parse(subtitleMessage);
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

            this.titleContent.getContent(player, args).ifPresent(titleMessage -> {
                Component titleComponent = deserialize(titleMessage);

                this.subtitleContent.getContent(player, args).ifPresent(subtitleMessage -> {
                    Component subtitleComponent  = deserialize(subtitleMessage);

                    Title title = Title.title(titleComponent, subtitleComponent, titleTimes);
                    sender.showTitle(title);
                });
            });
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
