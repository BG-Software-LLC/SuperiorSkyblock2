package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChunkResetEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.logic.BlocksLogic;
import com.bgsoftware.superiorskyblock.utils.logic.StackedBlocksLogic;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public final class SlimefunHook {

    private static final ReflectField<Map<Location, Config>> BLOCK_STORAGE_STORAGE = new ReflectField<>(BlockStorage.class, Map.class, "storage");

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        SlimefunHook.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(new Slimefun4Listener(), plugin);
        try {
            Class.forName("io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule");
            new Slimefun4RelocationsProtectionModule().register();
        } catch (ClassNotFoundException ex) {
            new Slimefun4ProtectionModule().register();
        }
    }

    private static boolean checkPermission(OfflinePlayer offlinePlayer, Location location, String protectableAction) {
        Island island = plugin.getGrid().getIslandAt(location);
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(offlinePlayer.getUniqueId());

        if (!plugin.getGrid().isIslandsWorld(location.getWorld()))
            return true;

        if (protectableAction.equals("PVP") || protectableAction.equals("ATTACK_PLAYER"))
            return island != null && island.hasSettingsEnabled(IslandFlags.PVP);

        IslandPrivilege islandPrivilege;

        switch (protectableAction) {
            case "BREAK_BLOCK":
                islandPrivilege = IslandPrivileges.BREAK;
                break;
            case "PLACE_BLOCK":
                islandPrivilege = IslandPrivileges.BUILD;
                break;
            case "ACCESS_INVENTORIES":
            case "INTERACT_BLOCK":
                islandPrivilege = IslandPrivileges.CHEST_ACCESS;
                break;
            default:
                islandPrivilege = IslandPrivileges.INTERACT;
                break;
        }

        return island != null && island.hasPermission(superiorPlayer, islandPrivilege);
    }

    @SuppressWarnings("unused")
    private static final class Slimefun4Listener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onAndroidMiner(AndroidMineEvent e) {
            SuperiorSkyblockPlugin.debug("Action: Android Break, Block: " + e.getBlock().getLocation() + ", Type: " + e.getBlock().getType());
            if (StackedBlocksLogic.tryUnstack(null, e.getBlock(), plugin))
                e.setCancelled(true);
            else
                BlocksLogic.handleBreak(e.getBlock());
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onChunkWipe(IslandChunkResetEvent e) {
            BlockStorage blockStorage = BlockStorage.getStorage(e.getWorld());

            if (blockStorage == null)
                return;

            Map<Location, Config> storageMap = BLOCK_STORAGE_STORAGE.get(blockStorage);

            if (storageMap == null)
                return;

            storageMap.keySet().stream().filter(location -> location.getBlockX() >> 4 == e.getChunkX() &&
                    location.getBlockZ() >> 4 == e.getChunkZ()).forEach(BlockStorage::clearBlockInfo);
        }

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onAutoPlacerPlaceBlock(BlockPlacerPlaceEvent e) {
            BlocksLogic.handlePlace(e.getBlock(), null);
        }

    }

    private static final class Slimefun4ProtectionModule implements
            me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule {

        @Override
        public void load() {

        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean hasPermission(OfflinePlayer offlinePlayer, Location location,
                                     me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction protectableAction) {
            return checkPermission(offlinePlayer, location, protectableAction.name());
        }

        public void register() {
            ProtectionManager protectionManager;
            try {
                protectionManager = me.mrCookieSlime.Slimefun.SlimefunPlugin.getProtectionManager();
            } catch (Throwable ex) {
                protectionManager = io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin.getProtectionManager();
            }
            protectionManager.registerModule(Bukkit.getServer(), plugin.getName(), pl -> this);
        }
    }

    private static final class Slimefun4RelocationsProtectionModule implements
            io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule {

        @Override
        public void load() {

        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean hasPermission(OfflinePlayer offlinePlayer, Location location,
                                     io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction interaction) {
            return checkPermission(offlinePlayer, location, interaction.name());
        }

        public void register() {
            Executor.sync(() -> {
                Slimefun.getProtectionManager().registerModule(Bukkit.getServer(), plugin.getName(), pl -> this);
            }, 2L);
        }

    }

}
