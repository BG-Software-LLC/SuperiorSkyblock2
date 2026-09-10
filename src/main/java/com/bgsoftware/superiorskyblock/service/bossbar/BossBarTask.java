package com.bgsoftware.superiorskyblock.service.bossbar;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class BossBarTask extends BukkitRunnable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final BossBarTask EMPTY_TASK = new BossBarTask(EmptyBossBar.getInstance(), 0);

    private static final Map<UUID, Queue<BossBarTask>> PLAYERS_RUNNING_TASKS = new ConcurrentHashMap<>();

    private final BossBar bossBar;
    private final double progressToRemovePerTick;
    private boolean reachedEndTask = false;
    private final BukkitTask task;

    public static BossBarTask create(BossBar bossBar, double ticksToRun) {
        return ticksToRun <= 0 ? EMPTY_TASK : new BossBarTask(bossBar, ticksToRun);
    }

    private BossBarTask(BossBar bossBar, double ticksToRun) {
        this.bossBar = bossBar;
        this.progressToRemovePerTick = this.bossBar.getProgress() / ticksToRun;
        this.task = progressToRemovePerTick > 0 ? plugin.getTaskScheduler().global(this, 1L, 1L) : null;
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
        if (this.task != null)
            this.task.cancel();
        BukkitExecutor.ensureMain(this.bossBar::removeAll);
    }

    public void registerTask(Player player) {
        AtomicReference<BossBarTask> removedTask = new AtomicReference<>();
        PLAYERS_RUNNING_TASKS.compute(player.getUniqueId(), (uuid, tasks) -> {
            if (tasks == null)
                tasks = new LinkedList<>();
            if (tasks.size() >= plugin.getSettings().getBossbarLimit())
                removedTask.set(tasks.poll());
            tasks.add(this);
            return tasks;
        });
        BossBarTask previous = removedTask.get();
        if (previous != null)
            previous.cancel();
    }

    public void unregisterTask(Player player) {
        PLAYERS_RUNNING_TASKS.computeIfPresent(player.getUniqueId(), (uuid, tasks) -> {
            tasks.remove(this);
            return tasks.isEmpty() ? null : tasks;
        });
    }

}
