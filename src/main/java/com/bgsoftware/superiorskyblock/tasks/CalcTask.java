package com.bgsoftware.superiorskyblock.tasks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class CalcTask extends BukkitRunnable {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static int id = -1;

    private CalcTask(){
        id = runTaskTimerAsynchronously(plugin, plugin.getSettings().calcInterval, plugin.getSettings().calcInterval).getTaskId();
    }

    @Override
    public void run() {
        if(Bukkit.getOnlinePlayers().size() > 0){
            announceToOps("&7&o[SuperiorSkyblock] Calculating islands...");
            plugin.getGrid().calcAllIslands(() -> {
                announceToPlayers();
                announceToOps("&7&o[SuperiorSkyblock] Calculating islands done!");
            });
        }
    }

    private void announceToOps(String message){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.isOp())
                Locale.sendMessage(player, message);
        }
        Locale.sendMessage(Bukkit.getConsoleSender(), message);
    }

    private void announceToPlayers(){
        for(Player player : Bukkit.getOnlinePlayers())
            Locale.RECALC_ALL_ISLANDS_DONE.send(player);
    }

    public static void startTask(){
        cancelTask();
        if(plugin.getSettings().calcInterval > 0)
            new CalcTask();
    }

    public static void cancelTask(){
        if(id != -1)
            Bukkit.getScheduler().cancelTask(id);
    }

}
