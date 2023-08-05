package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChangeSkinHook implements Listener {

    public static boolean isCompatible() {
        try {
            Class.forName("com.github.games647.changeskin.bukkit.events.PlayerChangeSkinEvent");
            return true;
        } catch (ClassNotFoundException error) {
            return false;
        }
    }

    private ChangeSkinHook() {
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new PlayerChangeSkinListener(plugin), plugin);
    }

    private static class PlayerChangeSkinListener implements Listener {

        private final SuperiorSkyblockPlugin plugin;

        PlayerChangeSkinListener(SuperiorSkyblockPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onPlayerChangeSkin(com.github.games647.changeskin.bukkit.events.PlayerChangeSkinEvent e) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            superiorPlayer.setTextureValue(e.getSkinModel().getEncodedValue());
        }

    }

}
