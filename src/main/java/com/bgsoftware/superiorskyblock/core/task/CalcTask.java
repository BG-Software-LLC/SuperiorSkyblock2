package com.bgsoftware.superiorskyblock.core.task;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class CalcTask extends BukkitRunnable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static BukkitTask calcTask;

    private CalcTask() {
        calcTask = runTaskTimerAsynchronously(plugin, plugin.getSettings().getCalcInterval(), plugin.getSettings().getCalcInterval());
    }

    public static void startTask() {
        cancelTask();
        if (plugin.getSettings().getCalcInterval() > 0)
            new CalcTask();
    }

    public static void cancelTask() {
        if (calcTask != null) {
            calcTask.cancel();
            calcTask = null;
        }
    }

    @Override
    public void run() {
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
