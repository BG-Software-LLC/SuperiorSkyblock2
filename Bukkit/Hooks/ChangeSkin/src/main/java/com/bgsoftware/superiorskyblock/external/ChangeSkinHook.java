package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

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

    public static void register(SuperiorSkyblockPlugin plugin, JavaPlugin javaPlugin) {
        Bukkit.getPluginManager().registerEvents(new PlayerChangeSkinListener(plugin), javaPlugin);
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
