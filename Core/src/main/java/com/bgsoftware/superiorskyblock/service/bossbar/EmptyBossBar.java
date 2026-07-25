package com.bgsoftware.superiorskyblock.service.bossbar;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import org.bukkit.entity.Player;

public class EmptyBossBar implements BossBar {

    private static final EmptyBossBar INSTANCE = new EmptyBossBar();

    public static EmptyBossBar getInstance() {
        return INSTANCE;
    }

    private EmptyBossBar() {

    }

    @Override
    public void addPlayer(Player player) {

    }

    @Override
    public void removeAll() {

    }

    @Override
    public void setProgress(double progress) {

    }

    @Override
    public double getProgress() {
        return 0;
    }

}
