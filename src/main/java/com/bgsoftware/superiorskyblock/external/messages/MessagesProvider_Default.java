package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessagesProvider_Default implements MessagesProvider {

    @Override
    public IMessageComponent createActionBarComponent(String message) {
        return ActionBarComponent.of(message);
    }

    @Override
    public IMessageComponent createBossBarComponent(String message, BossBar.Color color, BossBar.Style style, int duration) {
        return BossBarComponent.of(message, color, mapBossBarStyle(style), duration);
    }

    @Override
    public IMessageComponent createRawMessageComponent(String message) {
        return RawMessageComponent.of(message);
    }

    @Override
    public IMessageComponent createTitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut) {
        return TitleComponent.of(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
    }

    private static BossBar.Style mapBossBarStyle(BossBar.Style style) {
        switch (style) {
            case SEGMENTED_6:
            case NOTCHED_6:
                return BossBar.Style.SEGMENTED_6;
            case SEGMENTED_10:
            case NOTCHED_10:
                return BossBar.Style.SEGMENTED_10;
            case SEGMENTED_12:
            case NOTCHED_12:
                return BossBar.Style.SEGMENTED_12;
            case SEGMENTED_20:
            case NOTCHED_20:
                return BossBar.Style.SEGMENTED_20;
            default:
                return BossBar.Style.SOLID;
        }
    }

    private static class ActionBarComponent implements IMessageComponent {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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

            Player player = (Player) sender;
            this.messageContent.getContent(player, args).ifPresent(message ->
                    plugin.getNMSPlayers().sendActionBar(player, message));
        }

    }

    private static class BossBarComponent implements IMessageComponent {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        private static final LazyReference<BossBarsService> bossBarsService = new LazyReference<BossBarsService>() {
            @Override
            protected BossBarsService create() {
                return plugin.getServices().getService(BossBarsService.class);
            }
        };

        private final MessageContent messageContent;
        private final BossBar.Color color;
        private final BossBar.Style style;
        private final int duration;

        public static IMessageComponent of(@Nullable String message, BossBar.Color color, BossBar.Style style, int duration) {
            return duration <= 0 || Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new BossBarComponent(message, color, style, duration);
        }

        private BossBarComponent(String message, BossBar.Color color, BossBar.Style style, int duration) {
            this.messageContent = MessageContent.parse(message);
            this.color = color;
            this.style = style;
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
            if (!(sender instanceof Player)) {
                return;
            }

            Player player = (Player) sender;
            this.messageContent.getContent(player, args).ifPresent(message ->
                    bossBarsService.get().createBossBar(player, message, this.color, this.style, this.duration));
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
            this.messageContent.getContent(player, args).ifPresent(sender::sendMessage);
        }

    }

    private static class TitleComponent implements IMessageComponent {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        private final MessageContent titleContent;
        private final MessageContent subtitleContent;
        private final int fadeIn;
        private final int stay;
        private final int fadeOut;

        public static IMessageComponent of(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                           int fadeIn, int stay, int fadeOut) {
            return stay <= 0 || (Text.isBlank(titleMessage) && Text.isBlank(subtitleMessage)) ?
                    EmptyMessageComponent.getInstance() : new TitleComponent(titleMessage, subtitleMessage, fadeIn, stay, fadeOut);
        }

        private TitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage, int fadeIn, int stay, int fadeOut) {
            this.titleContent = Text.isBlank(titleMessage) ? MessageContent.EMPTY : MessageContent.parse(titleMessage);
            this.subtitleContent = Text.isBlank(subtitleMessage) ? MessageContent.EMPTY : MessageContent.parse(subtitleMessage);
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
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
            if (!(sender instanceof Player)) {
                return;
            }

            Player player = (Player) sender;
            String titleMessage = this.titleContent.getContent(player, args).orElse(null);
            String subtitleMessage = this.subtitleContent.getContent(player, args).orElse(null);

            if (titleMessage != null && subtitleMessage != null) {
                plugin.getNMSPlayers().sendTitle(player, titleMessage, subtitleMessage,
                        this.fadeIn, this.stay, this.fadeOut);
            }
        }

    }

}
