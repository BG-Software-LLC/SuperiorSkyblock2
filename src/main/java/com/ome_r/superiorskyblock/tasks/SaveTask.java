package com.ome_r.superiorskyblock.tasks;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SaveTask extends BukkitRunnable {

    private static SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();
    private static int id = -1;

    private SaveTask(){
        if(id != -1)
            id = runTaskTimerAsynchronously(plugin, plugin.getSettings().saveInterval, plugin.getSettings().saveInterval).getTaskId();
    }

    @Override
    public void run() {
        if(Bukkit.getOnlinePlayers().size() > 0){
            announceToOps("&7&o[SuperiorSkyblock] Saving database...");
            plugin.getDataHandler().saveDatabase(false);
            announceToPlayers();
            announceToOps("&7&o[SuperiorSkyblock] Saving database done!");
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
            Locale.ISLAND_SAVE_ANNOUNCEMENT.send(player);
    }

    public static void startTask(){
        cancelTask();
        if(plugin.getSettings().saveInterval > 0)
            new SaveTask();
    }

    public static void cancelTask(){
        if(id != -1)
            Bukkit.getScheduler().cancelTask(id);
    }

}
