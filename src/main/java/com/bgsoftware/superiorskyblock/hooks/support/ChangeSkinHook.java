package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.github.games647.changeskin.bukkit.events.PlayerChangeSkinEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public final class ChangeSkinHook implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    ChangeSkinHook(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChangeSkin(PlayerChangeSkinEvent e){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        superiorPlayer.setTextureValue(e.getSkinModel().getEncodedValue());
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        Bukkit.getPluginManager().registerEvents(new ChangeSkinHook(plugin), plugin);
    }

}
