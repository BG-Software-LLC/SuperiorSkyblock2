package com.bgsoftware.superiorskyblock.core.task;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CalcTask {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static Object calcTask;

    private CalcTask() {
        long calcInterval = plugin.getSettings().getCalcInterval();
        calcTask = plugin.getPlatform().getScheduler().runAsyncTimer(this::run, calcInterval, calcInterval);
    }

    public static void startTask() {
        cancelTask();
        if (plugin.getSettings().getCalcInterval() > 0)
            new CalcTask();
    }

    public static void cancelTask() {
        plugin.getPlatform().getScheduler().cancelTask(calcTask);
        calcTask = null;
    }

    private void run() {
        if (Bukkit.getOnlinePlayers().size() > 0) {
            announceToPlayers(false);
            announceToOps("&7&o[SuperiorSkyblock] Calculating islands...");
            plugin.getGrid().calcAllIslands(() -> {
                announceToPlayers(true);
                announceToOps("&7&o[SuperiorSkyblock] Calculating islands done!");
            });
        }
    }

    private void announceToOps(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.isOp())
                Message.CUSTOM.send(player, message, true);
        }
        Message.CUSTOM.send(Bukkit.getConsoleSender(), message, true);
    }

    private void announceToPlayers(boolean done) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (done) {
                Message.RECALC_ALL_ISLANDS_DONE.send(player);
            } else {
                Message.RECALC_ALL_ISLANDS.send(player);
            }
        }
    }

}
