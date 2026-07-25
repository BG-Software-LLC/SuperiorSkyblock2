package com.bgsoftware.superiorskyblock.external.bossbar;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BossBarProvider_MiniMessage implements BossBarProvider {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.builder().tags(StandardTags.defaults()).build();
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.legacySection();
    private static final EnumMap<BossBar.Color, net.kyori.adventure.bossbar.BossBar.Color> COLORS_MAP = new EnumMap<>(BossBar.Color.class);

    static {
        for (BossBar.Color color : BossBar.Color.values())
            COLORS_MAP.put(color, net.kyori.adventure.bossbar.BossBar.Color.valueOf(color.name()));
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, BossBar.Style style, double ticksToRun) {
        BossBarImpl bossBar = new BossBarImpl(net.kyori.adventure.bossbar.BossBar.bossBar(
                deserialize(message), 1.0f, mapBossBarColor(color), mapBossBarStyle(style)), ticksToRun);
        bossBar.addPlayer(player);
        return bossBar;
    }

    private static net.kyori.adventure.bossbar.BossBar.Overlay mapBossBarStyle(BossBar.Style style) {
        return switch (style) {
            case SEGMENTED_6, NOTCHED_6 -> net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_6;
            case SEGMENTED_10, NOTCHED_10 -> net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_10;
            case SEGMENTED_12, NOTCHED_12 -> net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_12;
            case SEGMENTED_20, NOTCHED_20 -> net.kyori.adventure.bossbar.BossBar.Overlay.NOTCHED_20;
            default -> net.kyori.adventure.bossbar.BossBar.Overlay.PROGRESS;
        };
    }

    private static net.kyori.adventure.bossbar.BossBar.Color mapBossBarColor(BossBar.Color color) {
        return Objects.requireNonNull(COLORS_MAP.get(color));
    }

    private static Component deserialize(String message) {
        try {
            return MINI_MESSAGE.deserialize(message);
        } catch (ParsingException exception) {
            return LEGACY_COMPONENT_SERIALIZER.deserialize(message);
        }
    }

    private static class BossBarImpl implements BossBar {

        private final net.kyori.adventure.bossbar.BossBar bossBar;
        private final BossBarTask bossBarTask;
        private final Set<Player> players = new HashSet<>();

        public BossBarImpl(net.kyori.adventure.bossbar.BossBar bossBar, double ticksToRun) {
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
