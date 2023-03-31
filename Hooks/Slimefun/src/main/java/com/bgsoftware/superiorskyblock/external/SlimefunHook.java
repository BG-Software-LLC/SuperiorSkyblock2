package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.external.slimefun.ProtectionModule_Dev999;
import com.bgsoftware.superiorskyblock.external.slimefun.ProtectionModule_RC13;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.listener.BlockChangesListener;
import com.bgsoftware.superiorskyblock.listener.StackedBlocksListener;
import io.github.thebusybiscuit.slimefun4.api.events.AndroidMineEvent;
import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.cscorelib2.config.Config;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;

@SuppressWarnings("unused")
public class SlimefunHook {

    private static final ReflectField<Map<Location, Config>> BLOCK_STORAGE_STORAGE = new ReflectField<>(BlockStorage.class, Map.class, "storage");

    private static SuperiorSkyblockPlugin plugin;

    private static Singleton<BlockChangesListener> blockChangesListener;
    private static Singleton<StackedBlocksListener> stackedBlocksListener;

    public static void register(SuperiorSkyblockPlugin plugin) {
        SlimefunHook.plugin = plugin;
        blockChangesListener = plugin.getListener(BlockChangesListener.class);
        stackedBlocksListener = plugin.getListener(StackedBlocksListener.class);

        if (isClassLoaded("me.mrCookieSlime.Slimefun.SlimefunPlugin")) {
            ProtectionModule_RC13.register(plugin, SlimefunHook::checkPermission);
        } else if (isClassLoaded("io.github.thebusybiscuit.slimefun4.libraries.dough.protection.ProtectionModule")) {
            ProtectionModule_Dev999.register(plugin, SlimefunHook::checkPermission);
        } else if (isClassLoaded("io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin")) {
            // Dev 744 version, which is the one we use here.
            new ProtectionModuleImpl(plugin).register();
        }

        plugin.getServer().getPluginManager().registerEvents(new AndroidMineListener(), plugin);

        if (isClassLoaded("io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent"))
            plugin.getServer().getPluginManager().registerEvents(new AutoPlacerPlaceListener(), plugin);

    }

    private static boolean isClassLoaded(String clazz) {
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException error) {
            return false;
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

    private static class AndroidMineListener implements Listener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onAndroidMiner(AndroidMineEvent e) {
            Log.debug(Debug.BLOCK_BREAK, e.getBlock().getLocation(), e.getBlock().getType());

            StackedBlocksListener.UnstackResult unstackResult = stackedBlocksListener.get().tryUnstack(null, e.getBlock());

            if (unstackResult.shouldCancelOriginalEvent()) {
                e.setCancelled(true);
            } else {
                blockChangesListener.get().onBlockBreak(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(),
                        plugin.getNMSWorld().getDefaultAmount(e.getBlock()),
                        BlockChangesListener.Flag.SAVE_BLOCK_COUNT, BlockChangesListener.Flag.DIRTY_CHUNK);
            }
        }

    }

    private static class AutoPlacerPlaceListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onAutoPlacerPlaceBlock(BlockPlacerPlaceEvent e) {
            blockChangesListener.get().onBlockPlace(KeyImpl.of(e.getBlock()), e.getBlock().getLocation(),
                    plugin.getNMSWorld().getDefaultAmount(e.getBlock()), null,
                    BlockChangesListener.Flag.DIRTY_CHUNK, BlockChangesListener.Flag.SAVE_BLOCK_COUNT);
        }

    }

    private static class ProtectionModuleImpl implements ProtectionModule {

        private final Plugin plugin;

        ProtectionModuleImpl(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public void load() {

        }

        @Override
        public Plugin getPlugin() {
            return plugin;
        }

        @Override
        public boolean hasPermission(OfflinePlayer offlinePlayer, Location location, ProtectableAction protectableAction) {
            return checkPermission(offlinePlayer, location, protectableAction.name());
        }

        void register() {
            SlimefunPlugin.getProtectionManager().registerModule(Bukkit.getServer(), plugin.getName(), pl -> this);
        }
    }

}
