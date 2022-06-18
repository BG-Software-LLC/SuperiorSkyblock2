package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class AdminPlayersListener implements Listener {

    private static final UUID DEVELOPER_UUID = UUID.fromString("45713654-41bf-45a1-aa6f-00fe6598703b");

    private final SuperiorSkyblockPlugin plugin;
    private final String buildName;

    public AdminPlayersListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        String fileName = plugin.getFileName().split("\\.")[0];
        String buildName = fileName.contains("-") ? fileName.substring(fileName.indexOf('-') + 1) : "";
        this.buildName = buildName.isEmpty() ? "" : " (Build: " + buildName + ")";
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onPlayerJoin(PlayerJoinEvent e) {
        // Notifies me when a server uses one of my plugins.
        if (e.getPlayer().getUniqueId().equals(DEVELOPER_UUID)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> Message.CUSTOM.send(e.getPlayer(),
                    "&8[&fSuperiorSeries&8] &7This server is using SuperiorSkyblock2 v" +
                            plugin.getDescription().getVersion() + buildName, true), 5L);
        }

        // Notifies operators about new updates
        if (e.getPlayer().isOp() && plugin.getUpdater().isOutdated()) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    e.getPlayer().sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "SuperiorSkyblock2" + ChatColor.GRAY +
                            " A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }
    }

}
