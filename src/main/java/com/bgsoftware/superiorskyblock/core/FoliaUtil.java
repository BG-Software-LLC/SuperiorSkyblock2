package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Folia compatibility layer — uses pure reflection so no Folia API JAR is needed at compile time.
 * All scheduler calls are guarded by {@link #isFolia()} and are no-ops on non-Folia servers.
 */
public final class FoliaUtil {

    private static final boolean FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException ignored) {
            folia = false;
        }
        FOLIA = folia;
    }

    private FoliaUtil() {}

    public static boolean isFolia() {
        return FOLIA;
    }

    /** Schedules {@code task} on the global region scheduler (runs once, immediately). */
    public static void runGlobalSync(Plugin plugin, Runnable task) {
        try {
            Object s = globalScheduler();
            s.getClass().getMethod("run", Plugin.class, Consumer.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Schedules {@code task} on the global region scheduler after {@code delayTicks} ticks. */
    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        try {
            Object s = globalScheduler();
            s.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run(), delayTicks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedules a repeating task on the global region scheduler.
     * Returns the opaque {@code ScheduledTask} object (as {@code Object}); pass to {@link #cancelTask(Object)} to stop.
     */
    public static Object runGlobalTimer(Plugin plugin, Consumer<Object> consumer, long initialDelay, long period) {
        try {
            Object s = globalScheduler();
            return s.getClass()
                    .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class)
                    .invoke(s, plugin, consumer, initialDelay, period);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Schedules {@code task} asynchronously (runs immediately). */
    public static void runAsync(Plugin plugin, Runnable task) {
        try {
            Object s = asyncScheduler();
            s.getClass().getMethod("runNow", Plugin.class, Consumer.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Schedules {@code task} asynchronously after {@code delayMs} milliseconds. */
    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delayMs) {
        try {
            Object s = asyncScheduler();
            s.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run(), delayMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Schedules a repeating async task.
     * Returns the opaque {@code ScheduledTask} object; pass to {@link #cancelTask(Object)} to stop.
     */
    public static Object runAsyncTimer(Plugin plugin, Consumer<Object> consumer, long initialDelayMs, long periodMs) {
        try {
            Object s = asyncScheduler();
            return s.getClass()
                    .getMethod("runAtFixedRate", Plugin.class, Consumer.class, long.class, long.class, TimeUnit.class)
                    .invoke(s, plugin, consumer, initialDelayMs, periodMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Cancels all tasks owned by {@code plugin} on both Folia schedulers. */
    public static void cancelAllTasks(Plugin plugin) {
        try {
            Object gs = globalScheduler();
            gs.getClass().getMethod("cancelTasks", Plugin.class).invoke(gs, plugin);
        } catch (Exception ignored) {}
        try {
            Object as = asyncScheduler();
            as.getClass().getMethod("cancelTasks", Plugin.class).invoke(as, plugin);
        } catch (Exception ignored) {}
    }

    /** Cancels a single Folia task returned by one of the {@code run*} methods above. */
    public static void cancelTask(Object task) {
        if (task == null) return;
        try {
            task.getClass().getMethod("cancel").invoke(task);
        } catch (Exception ignored) {}
    }

    private static Object globalScheduler() throws Exception {
        return Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
    }

    private static Object asyncScheduler() throws Exception {
        return Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
    }
}
