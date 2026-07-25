package com.bgsoftware.superiorskyblock.platform.server;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.mojang.authlib.properties.Property;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public interface IServerManager {

    void shutdown();

    int getOnlinePlayersCount();

    List<SuperiorPlayer> getOnlinePlayers();

    void dispatchConsoleCommand(String command);

    boolean isPlayerOnline(UUID uuid);

    void registerCommand(String label);

    double getCurrentTps();

    int getDataVersion();

    int getMaxWorldSize();

    // Absorbed from NMSPlayers (per-player identity/session, not HUD rendering)
    void setSkinTexture(SuperiorPlayer superiorPlayer);

    void setSkinTexture(SuperiorPlayer superiorPlayer, Property property);

    boolean wasThrownByPlayer(Item item, SuperiorPlayer superiorPlayer);

    // Opaque handle — internal NMS type (nms.player.OfflinePlayerData), never re-typed by callers
    Object createOfflinePlayerData(OfflinePlayer offlinePlayer);

    @Nullable
    Locale getPlayerLocale(Player player);

}
