package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.plugin.Plugin;

@SuppressWarnings({"deprecation", "unused"})
public class CoreProtectHook {

    private static SuperiorSkyblockPlugin plugin;
    private static Plugin coreProtect;

    public static void register(SuperiorSkyblockPlugin plugin) {
        CoreProtectHook.plugin = plugin;
        coreProtect = Bukkit.getPluginManager().getPlugin("CoreProtect");
        plugin.getProviders().registerStackedBlocksListener(CoreProtectHook::recordBlockAction);
    }

    private static void recordBlockAction(OfflinePlayer offlinePlayer, Block block,
                                          IStackedBlocksListener.Action action) {
        if (!Bukkit.isPrimaryThread()) {
            BukkitExecutor.sync(() -> recordBlockAction(offlinePlayer, block, action));
            return;
        }

        CoreProtectAPI coreProtectAPI = ((CoreProtect) coreProtect).getAPI();

        if (coreProtectAPI.APIVersion() == 5) {
            switch (action) {
                case BLOCK_BREAK:
                    coreProtectAPI.logRemoval(offlinePlayer.getName(), block.getLocation(), block.getType(), block.getData());
                    break;
                case BLOCK_PLACE:
                    coreProtectAPI.logPlacement(offlinePlayer.getName(), block.getLocation(), block.getType(), block.getData());
                    break;
            }
        } else if (coreProtectAPI.APIVersion() <= 9) {
            switch (action) {
                case BLOCK_BREAK:
                    coreProtectAPI.logRemoval(offlinePlayer.getName(), block.getLocation(), block.getType(),
                            (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(block));
                    break;
                case BLOCK_PLACE:
                    coreProtectAPI.logPlacement(offlinePlayer.getName(), block.getLocation(), block.getType(),
                            (org.bukkit.block.data.BlockData) plugin.getNMSWorld().getBlockData(block));
                    break;
            }
        }
    }

}
