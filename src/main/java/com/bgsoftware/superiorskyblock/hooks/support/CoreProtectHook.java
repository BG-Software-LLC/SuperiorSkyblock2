package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public final class CoreProtectHook {

    private static SuperiorSkyblockPlugin plugin;
    private static Plugin coreProtect;

    public static void register(SuperiorSkyblockPlugin plugin){
        CoreProtectHook.plugin = plugin;
        coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
    }

    public static void recordBlockChange(OfflinePlayer offlinePlayer, Block block, boolean place) {
        if(coreProtect == null)
            return;

        if(!Bukkit.isPrimaryThread()){
            Executor.sync(() -> recordBlockChange(offlinePlayer, block, place));
            return;
        }

        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();

        if(coreProtectAPI.APIVersion() == 5) {
            if(!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), block.getLocation(), block.getType(), block.getData());
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), block.getLocation(), block.getType(), block.getData());
        }
        else if(coreProtectAPI.APIVersion() == 6) {
            if(!place)
                coreProtectAPI.logRemoval(offlinePlayer.getName(), block.getLocation(), block.getType(),
                        (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(block));
            else
                coreProtectAPI.logPlacement(offlinePlayer.getName(), block.getLocation(), block.getType(),
                        (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(block));
        }
    }

}
