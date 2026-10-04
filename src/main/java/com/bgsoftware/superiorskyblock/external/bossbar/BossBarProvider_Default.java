package com.bgsoftware.superiorskyblock.external.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import org.bukkit.entity.Player;

public class BossBarProvider_Default implements BossBarProvider {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, BossBar.Style style, double ticksToRun) {
        return plugin.getNMSPlayers().createBossBar(player, message, color, mapBossBarStyle(style), ticksToRun);
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

}
