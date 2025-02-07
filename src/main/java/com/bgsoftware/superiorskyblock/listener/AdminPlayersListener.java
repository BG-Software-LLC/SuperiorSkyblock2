package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminPlayersListener extends AbstractGameEventListener {

    private static final UUID DEVELOPER_UUID = UUID.fromString("45713654-41bf-45a1-aa6f-00fe6598703b");

    private final String buildName;

    public AdminPlayersListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        String fileName = plugin.getFileName().split("\\.")[0];
        String buildName = fileName.contains("-") ? fileName.substring(fileName.indexOf('-') + 1) : "";
        this.buildName = buildName.isEmpty() ? "" : " (Build: " + buildName + ")";

        registerCallback(GameEventType.PLAYER_JOIN_EVENT, GameEventPriority.MONITOR, this::onPlayerJoin);
    }

    private void onPlayerJoin(GameEvent<GameEventArgs.PlayerJoinEvent> e) {
        Player player = e.getArgs().player;

        // Notifies me when a server uses one of my plugins.
        if (player.getUniqueId().equals(DEVELOPER_UUID)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> Message.CUSTOM.send(player,
                    "&8[&fSuperiorSeries&8] &7This server is using SuperiorSkyblock2 v" +
                            plugin.getDescription().getVersion() + buildName, true), 5L);
        }

        // Notifies operators about new updates
        if (player.isOp() && plugin.getUpdater().isOutdated()) {
            Bukkit.getScheduler().runTaskLater(plugin, () ->
                    player.sendMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "SuperiorSkyblock2" + ChatColor.GRAY +
                            " A new version is available (v" + plugin.getUpdater().getLatestVersion() + ")!"), 20L);
        }
    }

}
