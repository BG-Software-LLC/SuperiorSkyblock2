package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarTask;
import com.bgsoftware.superiorskyblock.service.message.MessagesServiceImpl;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MiniMessageHook {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();

    private static boolean registered = false;

    private static final MessagesServiceImpl.CustomComponentParser PARSER = new MessagesServiceImpl.CustomComponentParser() {
        @Override
        public Optional<IMessageComponent> parse(YamlConfiguration config, String path) {
            if (!config.isString(path))
                return Optional.empty();

            String content = config.getString(path);
            if (Text.isBlank(content))
                return Optional.empty();

            return parseRawMessage(content);
        }

        @Override
        public Optional<IMessageComponent> parseActionBar(@Nullable String message) {
            if (Text.isBlank(message)) {
                return Optional.empty();
            }

            try {
                deserialize(message);

                return Optional.of(new MiniMessageActionBarComponent(message));
            } catch (ParsingException error) {
                return Optional.empty();
            }
        }

        @Override
        public Optional<IMessageComponent> parseBossBar(@Nullable String name, String color,
                                                        String overlay, int ticksToRun) {
            if (ticksToRun <= 0 || Text.isBlank(name)) {
                return Optional.empty();
            }

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

            try {
                deserialize(name);

                return Optional.of(new MiniMessageBossBarComponent(name, bossBarColor, bossBarOverlay, ticksToRun));
            } catch (ParsingException error) {
                return Optional.empty();
            }
        }

        @Override
        public Optional<IMessageComponent> parseRawMessage(@Nullable String content) {
            if (Text.isBlank(content)) {
                return Optional.of(EmptyMessageComponent.getInstance());
            }

            try {
                deserialize(content);

                return Optional.of(new MiniMessageRawMessageComponent(content));
            } catch (ParsingException error) {
                return Optional.empty();
            }
        }

        @Override
        public Optional<IMessageComponent> parseTitle(@Nullable String title, @Nullable String subtitle,
                                                      int fadeIn, int duration, int fadeOut) {
            if (duration <= 0 || (Text.isBlank(title) && Text.isBlank(subtitle))) {
                return Optional.empty();
            }

            try {
                deserialize(title);
                deserialize(subtitle);

                return Optional.of(new MiniMessageTitleComponent(title, subtitle, fadeIn, duration, fadeOut));
            } catch (ParsingException error) {
                return Optional.empty();
            }
        }
    };

    public static void register(SuperiorSkyblockPlugin plugin) {
        if (!registered) {
            MessagesServiceImpl messagesService = (MessagesServiceImpl) plugin.getServices().getService(MessagesService.class);
            messagesService.registerCustomComponentParser(PARSER);
            registered = true;
        }
    }

    private static Component deserialize(String message) {
        return MINI_MESSAGE.deserialize(Formatters.COLOR_FORMATTER.format(message));
    }

    private static class MiniMessageActionBarComponent implements IMessageComponent {

        private final MessageContent messageContent;

        MiniMessageActionBarComponent(String messageContent) {
            this.messageContent = MessageContent.parse(messageContent);
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

            this.messageContent.getContent((Player) sender, args).ifPresent(message -> {
                sender.sendActionBar(deserialize(message));
            });
        }

    }

    private static class MiniMessageBossBarComponent implements IMessageComponent {

        private final MessageContent messageContent;
        private final BossBar.Color color;
        private final BossBar.Overlay overlay;
        private final int duration;

        MiniMessageBossBarComponent(String messageContent, BossBar.Color color, BossBar.Overlay overlay, int duration) {
            this.messageContent = MessageContent.parse(messageContent);
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
                new MiniMessageBossBar(bossBar, duration).addPlayer(player);
            });
        }

    }

    private static class MiniMessageRawMessageComponent implements IMessageComponent {

        private final MessageContent messageContent;

        MiniMessageRawMessageComponent(String content) {
            this.messageContent = MessageContent.parse(content);
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

            this.messageContent.getContent(player, args).ifPresent(message -> {
                sender.sendMessage(deserialize(message));
            });
        }

    }

    private static class MiniMessageTitleComponent implements IMessageComponent {

        private final MessageContent titleContent;
        private final MessageContent subtitleContent;
        private final long fadeIn;
        private final long stay;
        private final long fadeOut;

        MiniMessageTitleComponent(String titleContent, String subtitleContent, int fadeIn, int stay, int fadeOut) {
            this.titleContent = MessageContent.parse(titleContent);
            this.subtitleContent = MessageContent.parse(subtitleContent);
            this.fadeIn = fadeIn * 50L;
            this.stay = stay * 50L;
            this.fadeOut = fadeOut * 50L;
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

                    Title.Times titleTimes = Title.Times.of(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut));
                    Title title = Title.title(titleComponent, subtitleComponent, titleTimes);
                    sender.showTitle(title);
                });
            });
        }

    }

    private static class MiniMessageBossBar implements com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar {

        private final BossBar bossBar;
        private final BossBarTask bossBarTask;
        private final Set<Player> players = new HashSet<>();

        public MiniMessageBossBar(BossBar bossBar, double ticksToRun) {
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
