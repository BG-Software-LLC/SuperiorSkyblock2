package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandFlags;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import me.mrCookieSlime.Slimefun.SlimefunPlugin;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectableAction;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionManager;
import me.mrCookieSlime.Slimefun.cscorelib2.protection.ProtectionModule;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

public final class SlimefunHook implements ProtectionModule {

    private final SuperiorSkyblockPlugin plugin;

    private SlimefunHook(SuperiorSkyblockPlugin plugin){
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
        Island island = plugin.getGrid().getIslandAt(location);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(offlinePlayer.getUniqueId());

        if(!plugin.getGrid().isIslandsWorld(location.getWorld()))
            return true;

        if(protectableAction == ProtectableAction.PVP)
            return island != null && island.hasSettingsEnabled(IslandFlags.PVP);

        IslandPrivilege islandPrivilege = null;

        switch (protectableAction){
            case BREAK_BLOCK:
                islandPrivilege = IslandPrivileges.BREAK;
                break;
            case PLACE_BLOCK:
                islandPrivilege = IslandPrivileges.BUILD;
                break;
            case ACCESS_INVENTORIES:
                islandPrivilege = IslandPrivileges.CHEST_ACCESS;
                break;
        }

        return islandPrivilege != null && island != null && island.hasPermission(superiorPlayer, islandPrivilege);
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        SlimefunHook slimefunHook = new SlimefunHook(plugin);
        ProtectionManager protectionManager;
        try{
            protectionManager = SlimefunPlugin.getProtectionManager();
        }catch (Throwable ex){
            protectionManager = io.github.thebusybiscuit.slimefun4.implementation.SlimefunPlugin.getProtectionManager();
        }
        protectionManager.registerModule(Bukkit.getServer(), plugin.getName(), pl -> slimefunHook);
    }

}
