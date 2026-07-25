package com.bgsoftware.superiorskyblock.external.bossbar;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import org.bukkit.entity.Player;

public interface BossBarProvider {

    BossBar createBossBar(Player player, String message, BossBar.Color color, BossBar.Style style, double ticksToRun);

}
