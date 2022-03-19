package com.bgsoftware.superiorskyblock.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public final class BossBarTask extends BukkitRunnable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<UUID, Queue<BossBarTask>> PLAYERS_RUNNING_TASKS = new HashMap<>();

    private final BossBar bossBar;
    private final double progressToRemovePerTick;
    private boolean reachedEndTask = false;

    public BossBarTask(BossBar bossBar, double ticksToRun) {
        this.bossBar = bossBar;
        this.progressToRemovePerTick = this.bossBar.getProgress() / ticksToRun;
        if (progressToRemovePerTick > 0) {
            runTaskTimer(plugin, 1L, 1L);
        }
    }

    @Override
    public void run() {
        if (reachedEndTask) {
            cancel();
        } else {
            this.bossBar.setProgress(Math.max(0D, this.bossBar.getProgress() - progressToRemovePerTick));
            reachedEndTask = this.bossBar.getProgress() == 0D;
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        this.bossBar.removeAll();
        super.cancel();
    }

    public void registerTask(Player player) {
        Queue<BossBarTask> bossBarTasks = PLAYERS_RUNNING_TASKS.computeIfAbsent(player.getUniqueId(), s -> new LinkedList<>());

        if (bossBarTasks.size() >= plugin.getSettings().getBossbarLimit()) {
            BossBarTask lastRunningTask = bossBarTasks.poll();
            if (lastRunningTask != null)
                lastRunningTask.cancel();
        }

        bossBarTasks.add(this);
    }

    public void unregisterTask(Player player) {
        Queue<BossBarTask> bossBarTasks = PLAYERS_RUNNING_TASKS.get(player.getUniqueId());

        if (bossBarTasks == null)
            return;

        bossBarTasks.remove(this);
    }

}
