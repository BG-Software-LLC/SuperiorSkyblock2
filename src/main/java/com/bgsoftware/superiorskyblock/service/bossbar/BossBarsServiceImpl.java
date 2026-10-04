package com.bgsoftware.superiorskyblock.service.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import com.bgsoftware.superiorskyblock.service.IService;
import org.bukkit.entity.Player;

public class BossBarsServiceImpl implements BossBarsService, IService {

    private final SuperiorSkyblockPlugin plugin;

    public BossBarsServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return BossBarsService.class;
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun) {
        return createBossBar(player, message, color, BossBar.Style.SOLID, ticksToRun);
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, BossBar.Style style, double ticksToRun) {
        return plugin.getProviders().getBossBarProvider().createBossBar(player, message, color, style, ticksToRun);
    }

}
