package com.bgsoftware.superiorskyblock.missions.common;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

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

    public static void runGlobalDelayed(Plugin plugin, Runnable task, long delayTicks) {
        try {
            Object s = globalScheduler();
            s.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run(), delayTicks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void runAsyncDelayed(Plugin plugin, Runnable task, long delayMs) {
        try {
            Object s = asyncScheduler();
            s.getClass().getMethod("runDelayed", Plugin.class, Consumer.class, long.class, TimeUnit.class)
                    .invoke(s, plugin, (Consumer<Object>) t -> task.run(), delayMs, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object globalScheduler() throws Exception {
        return Bukkit.getServer().getClass().getMethod("getGlobalRegionScheduler").invoke(Bukkit.getServer());
    }

    private static Object asyncScheduler() throws Exception {
        return Bukkit.getServer().getClass().getMethod("getAsyncScheduler").invoke(Bukkit.getServer());
    }
}
