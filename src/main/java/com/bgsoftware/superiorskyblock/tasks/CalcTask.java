package com.bgsoftware.superiorskyblock.tasks;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public final class CalcTask extends BukkitRunnable {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static int id = -1;

    private CalcTask(){
        id = runTaskTimerAsynchronously(plugin, plugin.getSettings().getCalcInterval(), plugin.getSettings().getCalcInterval()).getTaskId();
    }

    @Override
    public void run() {
        if(Bukkit.getOnlinePlayers().size() > 0){
            announceToPlayers(false);
            announceToOps("&7&o[SuperiorSkyblock] Calculating islands...");
            plugin.getGrid().calcAllIslands(() -> {
                announceToPlayers(true);
                announceToOps("&7&o[SuperiorSkyblock] Calculating islands done!");
            });
        }
    }

    private void announceToOps(String message){
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.isOp())
                Locale.sendMessage(player, message, true);
        }
        Locale.sendMessage(Bukkit.getConsoleSender(), message, true);
    }

    private void announceToPlayers(boolean done){
        for(Player player : Bukkit.getOnlinePlayers()) {
            if(done) {
                Locale.RECALC_ALL_ISLANDS_DONE.send(player);
            }
            else{
                Locale.RECALC_ALL_ISLANDS.send(player);
            }
        }
    }

    public static void startTask(){
        cancelTask();
        if(plugin.getSettings().getCalcInterval() > 0)
            new CalcTask();
    }

    public static void cancelTask(){
        if(id != -1)
            Bukkit.getScheduler().cancelTask(id);
    }

}
