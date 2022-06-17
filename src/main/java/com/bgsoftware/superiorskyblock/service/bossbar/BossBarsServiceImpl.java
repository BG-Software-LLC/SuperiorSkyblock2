package com.bgsoftware.superiorskyblock.service.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import org.bukkit.entity.Player;

public class BossBarsServiceImpl implements BossBarsService {

    private final SuperiorSkyblockPlugin plugin;

    public BossBarsServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun) {
        return plugin.getNMSPlayers().createBossBar(player, message, color, ticksToRun);
    }

}
