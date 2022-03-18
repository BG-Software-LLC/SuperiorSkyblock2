package com.bgsoftware.superiorskyblock.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

public final class BossBarTask extends BukkitRunnable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final BossBarTask EMPTY_TASK = new BossBarTask(EmptyBossBar.getInstance(), 0);

    private final BossBar bossBar;
    private final double progressToRemovePerTick;
    private boolean reachedEndTask = false;

    private BossBarTask(BossBar bossBar, double ticksToRun) {
        this.bossBar = bossBar;
        this.progressToRemovePerTick = this.bossBar.getProgress() / ticksToRun;
        if (progressToRemovePerTick > 0) {
            runTaskTimer(plugin, 1L, 1L);
        }
    }

    @Override
    public void run() {
        if (reachedEndTask) {
            this.bossBar.removeAll();
            cancel();
        } else {
            this.bossBar.setProgress(Math.max(0D, this.bossBar.getProgress() - progressToRemovePerTick));
            reachedEndTask = this.bossBar.getProgress() == 0D;
        }
    }

    public static BossBarTask of(BossBar bossBar, long time, TimeUnit timeUnit) {
        if (bossBar == EmptyBossBar.getInstance())
            return EMPTY_TASK;

        double ticksToRun = Math.ceil(timeUnit.toSeconds(time) * 20D);
        return ticksToRun <= 0 ? EMPTY_TASK : new BossBarTask(bossBar, ticksToRun);
    }

}
